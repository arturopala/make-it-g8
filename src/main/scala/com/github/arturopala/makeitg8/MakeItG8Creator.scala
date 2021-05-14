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

import scala.util.{Failure, Try}

trait MakeItG8Creator {

  import EscapeCodes._

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
        println("Proposed content replacements:")
        println(
          contentFilesReplacements
            .map { case (from, to) => s"\t$ANSI_PURPLE$to$ANSI_RESET \u2192 $ANSI_CYAN$from$ANSI_RESET" }
            .mkString("\n")
        )
      }

      println()

      //------------------------------------------------------
      // COPY PARAMETRISED PROJECT FILES TO TEMPLATE G8 FOLDER
      //------------------------------------------------------

      import scala.collection.JavaConverters.asScalaIterator

      val gitIgnore = GitIgnore(config.ignoredPaths)

      val (sourcePaths, placeholderStats) = config.sourceFolder.listRecursively
        .foldLeft((Seq.empty[Path], Map.empty[String, Int])) { case ((paths, inputStats), source) =>
          val sourcePath: Path =
            config.sourceFolder.relativize(source)
          if (gitIgnore.isAllowed(sourcePath)) {
            val (targetPath, pathReplacementsStats) =
              TemplateUtils.templatePathFor(sourcePath, contentFilesReplacements, inputStats)
            val target =
              File(targetG8Folder.path.resolve(targetPath))
            if (sourcePath == targetPath)
              println(s"Processing $ANSI_YELLOW$sourcePath$ANSI_RESET")
            else
              println(s"Processing $ANSI_YELLOW$sourcePath$ANSI_RESET as $ANSI_CYAN$targetPath$ANSI_RESET")
            if (source.isDirectory) {
              config.targetFolder.createDirectoryIfNotExists()
              (paths, inputStats)
            }
            else {
              target.createFileIfNotExists(createParents = true)
              val (template, outputStats) =
                TemplateUtils.replace(
                  TemplateUtils.escape(source.contentAsString),
                  contentFilesReplacements,
                  pathReplacementsStats
                )
              target.write(template)
              (paths :+ sourcePath, outputStats)
            }
          }
          else (paths, inputStats)
        }

      println()
      println("Template placeholder stats:")

      placeholderStats.foreach {
        case (keyword, count) if count > 0 =>
          println(s"\t$ANSI_PURPLE$keyword$ANSI_RESET : $ANSI_GREEN$count$ANSI_RESET")
        case _ =>
      }

      //----------------------------------------------------
      // COPY PARAMETRISED BUILD FILES TO TEMPLATE G8 FOLDER
      //----------------------------------------------------

      val placeholders: Seq[(String, String)] =
        TemplateUtils
          .computePlaceholders(
            config.sourceFolder.path.getFileName.toString,
            config.packageName,
            keywords,
            config.keywordValueMap,
            placeholderStats
          )

      val buildFilesReplacements = {
        val testTemplateName = config.sourceFolder.name

        val customReadmeHeader: Option[String] =
          config.customReadmeHeaderPath.flatMap { path =>
            val file = File(config.sourceFolder.path.resolve(path))
            if (file.exists && file.isRegularFile) Option(file.contentAsString)
            else None
          }

        val customReadmeHeaderPathOpt: String =
          config.customReadmeHeaderPath.map(path => s"""--custom-readme-header-path "$path"""").getOrElse("")

        val templateGithubUser: String = config.keywordValueMap
          .get("templateGithubUser")
          .orElse(GitUtils.remoteGithubUser(config.sourceFolder.toJava))
          .orElse(GitUtils.remoteGithubUser(config.targetFolder.toJava))
          .getOrElse("{GITHUB_USER}")

        val keywordValueMap =
          Map("templateGithubUser" -> templateGithubUser) ++ config.keywordValueMap

        Seq(
          "$templateName$"        -> config.templateName,
          "$templateDescription$" -> config.templateDescription,
          "$gitRepositoryName$"   -> config.templateName,
          "$placeholders$" -> contentFilesReplacements
            .collect {
              case (value, key) if placeholders.exists { case (k, v) => s"$$$k$$" == key } => s"$key -> $value"
            }
            .mkString("\n\t"),
          "$exampleTargetTree$" -> FileTree.draw(FileTree.compute(sourcePaths)).lines.mkString("\n\t"),
          "$g8CommandLineArgs$" -> s"""${(keywordValueMap.toSeq ++ config.packageName
            .map(p => Seq("package" -> p))
            .getOrElse(Seq.empty))
            .map { case (k, v) => s"""--$k="$v"""" }
            .mkString(" ")} -o $testTemplateName""",
          "$testTargetFolder$" -> config.scriptTestTarget,
          "$testTemplateName$" -> testTemplateName,
          "$testCommand$"      -> config.scriptTestCommand,
          "$beforeTest$"       -> config.scriptBeforeTest.mkString("\n\t"),
          "$makeItG8CommandLine$" ->
            (s"""sbt "run --noclear --force --source ../../${config.scriptTestTarget}/$testTemplateName --target ../.. --name ${config.templateName} """ ++ config.packageName
              .map(p => s""" --package $p """)
              .getOrElse("") ++ s"""--description ${URLEncoder
              .encode(config.templateDescription, "utf-8")} $customReadmeHeaderPathOpt -K ${keywordValueMap
              .map { case (k, v) =>
                s"""$k=${URLEncoder.encode(v, "utf-8")}"""
              }
              .mkString(" ")}" -Dbuild.test.command="${config.scriptTestCommand}" """),
          "$customReadmeHeader$" -> customReadmeHeader.getOrElse(""),
          "$templateGithubUser$" -> templateGithubUser
        )
      }

      println()
      println("Build files replacements:")

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
              val (template, stats) =
                TemplateUtils.replace(content, buildFilesReplacements, Map.empty)
              targetFile
                .clear()
                .write(template)
            }
            .orElse {
              Failure(new Exception(s"Failed to create build file $path"))
            }
        }
        else {
          println(s"Skipping $ANSI_YELLOW$path$ANSI_RESET")
        }
      }

      //----------------------------------------------------------
      // COPY OR CREATE STATIC PROJECT FILES IN TEMPLATE G8 FOLDER
      //----------------------------------------------------------

      val defaultPropertiesFile =
        targetG8Folder.createChild("default.properties")

      defaultPropertiesFile
        .write(
          placeholders
            .map { case (key, value) => s"$key=$value" }
            .mkString("\n")
        )

      ()
    }.toEither
}

object MakeItG8Creator extends MakeItG8Creator
