/*
 * Copyright 2020 Artur Opala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.arturopala.makeitg8

import java.net.URLEncoder
import java.nio.file.Path

import better.files.{File, Resource}
import java.nio.file.attribute.PosixFilePermission

import scala.util.Try

trait MakeItG8Creator extends EscapeCodes {

  def createG8Template(config: MakeItG8Config): Either[Throwable, Unit] =
    Try {

      println()
      println(
        s"Processing $ANSI_YELLOW${config.sourceFolder}$ANSI_RESET to giter8 template $ANSI_CYAN${config.targetFolder}$ANSI_RESET ..."
      )
      if (config.targetFolder.exists && config.clearTargetFolder) {
        println(
          s"[${ANSI_YELLOW}warn$ANSI_RESET] Target folder exists, clearing $ANSI_YELLOW${config.targetFolder}$ANSI_RESET to make space for the new template"
        )
        config.targetFolder.clear()
      }
      else {
        config.targetFolder.createDirectoryIfNotExists()
      }

      val targetG8Folder = (config.targetFolder / "src" / "main" / "g8").createDirectoryIfNotExists()
      if (!config.clearTargetFolder) {
        println(
          s"[${ANSI_YELLOW}warn$ANSI_RESET] Clearing $ANSI_YELLOW${targetG8Folder.path}$ANSI_RESET to make space for the new template"
        )
        targetG8Folder.clear()
      }

      //---------------------------------------
      // PREPARE CONTENT REPLACEMENT KEYWORDS
      //---------------------------------------

      val keywords: Seq[String] = config.keywordValueMap.toSeq.sortBy(p => -p._2.length).map(_._1)

      val contentFilesReplacements: Seq[(String, String)] =
        config.packageName
          .map(packageName =>
            Seq(
              packageName.replaceAllLiterally(".", "/") -> "$packaged$",
              packageName                               -> "$package$"
            )
          )
          .getOrElse(Seq.empty) ++ TemplateUtils.prepareKeywordsReplacements(keywords, config.keywordValueMap)

      println()

      if (contentFilesReplacements.nonEmpty) {
        println("Source file content replacements:")
        println(
          contentFilesReplacements
            .map(r => s"\t$ANSI_PURPLE${r._1}$ANSI_RESET \u2192 $ANSI_CYAN${r._2}$ANSI_RESET")
            .mkString("\n")
        )
      }

      println()

      //---------------------------------------
      // COPY PARAMETRISED PROJECT FILES TO G8
      //---------------------------------------

      import scala.collection.JavaConverters.asScalaIterator

      val gitIgnore = GitIgnore(config.ignoredPaths)

      val sourcePaths: Iterator[Path] = config.sourceFolder.listRecursively
        .map { source =>
          val sourcePath: Path = config.sourceFolder.relativize(source)
          if (gitIgnore.isAllowed(sourcePath)) {
            val targetPath = TemplateUtils.templatePathFor(sourcePath, contentFilesReplacements)
            val target = File(targetG8Folder.path.resolve(targetPath))
            if (sourcePath == targetPath)
              println(s"Processing $ANSI_YELLOW$sourcePath$ANSI_RESET")
            else
              println(s"Processing $ANSI_YELLOW$sourcePath$ANSI_RESET as $ANSI_CYAN$targetPath$ANSI_RESET")
            if (source.isDirectory) {
              config.targetFolder.createDirectoryIfNotExists()
              None
            }
            else {
              target.createFileIfNotExists(createParents = true)
              target.write(
                TemplateUtils.replace(TemplateUtils.escape(source.contentAsString), contentFilesReplacements)
              )
              Some(sourcePath)
            }
          }
          else None
        }
        .collect { case Some(path) => path }

      //---------------------------------------
      // COPY OR CREATE STATIC PROJECT FILES
      //---------------------------------------

      val defaultPropertiesFile = targetG8Folder.createChild("default.properties")
      defaultPropertiesFile.write(
        TemplateUtils.prepareDefaultProperties(
          config.sourceFolder.path.getFileName.toString,
          config.packageName,
          keywords,
          config.keywordValueMap
        )
      )

      //---------------------------------------
      // COPY PARAMETRISED BUILD FILES
      //---------------------------------------

      val buildFilesReplacements = {
        val testTemplateName = config.sourceFolder.name

        val customReadmeHeader: Option[String] = config.customReadmeHeaderPath.flatMap { path =>
          val file = File(config.sourceFolder.path.resolve(path))
          if (file.exists && file.isRegularFile) Option(file.contentAsString)
          else None
        }

        val customReadmeHeaderPathOpt: String =
          config.customReadmeHeaderPath.map(path => s"""--custom-readme-header-path "$path"""").getOrElse("")

        Seq(
          "$templateName$"        -> config.templateName,
          "$templateDescription$" -> config.templateDescription,
          "$gitRepositoryName$"   -> config.templateName,
          "$placeholders$"        -> contentFilesReplacements.map { case (k, v) => s"$v -> $k" }.mkString("\n\t"),
          "$exampleTargetTree$"   -> FileTree.draw(FileTree.compute(sourcePaths)).lines.mkString("\n\t"),
          "$g8CommandLineArgs$" -> s"""${(config.keywordValueMap.toSeq ++ config.packageName
            .map(p => Seq("package" -> p))
            .getOrElse(Seq.empty))
            .map { case (k, v) => s"""--$k="$v"""" }
            .mkString(" ")} -o $testTemplateName""",
          "$testTargetFolder$" -> config.scriptTestTarget,
          "$testTemplateName$" -> testTemplateName,
          "$testCommand$"      -> config.scriptTestCommand,
          "$beforeTest$"       -> config.scriptBeforeTest.mkString("\n\t"),
          "$makeItG8CommandLine$" ->
            (s"""sbt "run --noclear --source ../../${config.scriptTestTarget}/$testTemplateName --target ../.. --name ${config.templateName} """ ++ config.packageName
              .map(p => s""" --package $p """)
              .getOrElse("") ++ s"""--description ${URLEncoder
              .encode(config.templateDescription, "utf-8")} $customReadmeHeaderPathOpt -K ${config.keywordValueMap
              .map { case (k, v) =>
                s"""$k=${URLEncoder.encode(v, "utf-8")}"""
              }
              .mkString(" ")}" -Dbuild.test.command="${config.scriptTestCommand}" """),
          "$customReadmeHeader$" -> customReadmeHeader.getOrElse("")
        )
      }

      println()
      println("Build file content replacements:")

      println(
        buildFilesReplacements
          .map(r => s"\t$ANSI_PURPLE${r._1}$ANSI_RESET \u2192 $ANSI_CYAN${r._2}$ANSI_RESET")
          .mkString("\n")
      )

      println()

      config.g8BuildTemplateResources.foreach { path =>
        if (config.createReadme || path != "README.md") {
          Try(Resource.my.getAsString(s"/${config.g8BuildTemplateSource}/$path"))
            .map { content =>
              val targetFile = File(
                config.targetFolder.path
                  .resolve(path.replace("__", "."))
              )
              println(s"Creating build file $ANSI_YELLOW${path.replace("__", ".")}$ANSI_RESET")
              targetFile.createFileIfNotExists(createParents = true)
              if (targetFile.name.endsWith(".sh")) {
                targetFile.addPermission(PosixFilePermission.OWNER_EXECUTE)
              }
              targetFile
                .clear()
                .write(
                  TemplateUtils
                    .replace(content, buildFilesReplacements)
                )
            } orElse Try {
            println(s"[${ANSI_RED}error$ANSI_RESET] Failed to create build file $ANSI_YELLOW$path$ANSI_RESET")
          }
        }
        else {
          println(s"Skipping $ANSI_YELLOW$path$ANSI_RESET")
        }
      }
    }.toEither
}

object MakeItG8Creator extends MakeItG8Creator
