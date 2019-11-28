/*
 * Copyright 2019 Artur Opala
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

import scala.util.Try

trait MakeItG8Creator {

  def createG8Template(config: MakeItG8Config): Either[Throwable, Unit] =
    Try {

      println(s"Processing ${config.sourceFolder} into giter8 template ${config.targetFolder} ...")
      if (config.targetFolder.exists && config.clearTargetFolder) {
        println(s"Target folder exists, clearing ${config.targetFolder.path} to make space for a new template project")
        config.targetFolder.clear()
      } else {
        config.targetFolder.createDirectoryIfNotExists()

      }

      val targetG8Folder = (config.targetFolder / "src" / "main" / "g8").createDirectoryIfNotExists()
      if (!config.clearTargetFolder) {
        println(s"Clearing ${targetG8Folder.path} to make space for a new template")
        targetG8Folder.clear()
      }

      //---------------------------------------
      // PREPARE CONTENT REPLACEMENT KEYWORDS
      //---------------------------------------

      val keywords: Seq[String] = config.keywordValueMap.toSeq.sortBy(p => -p._2.length).map(_._1)
      val contentFilesReplacements: Seq[(String, String)] = Seq(
        config.packageName.replaceAllLiterally(".", "/") -> "$packaged$",
        config.packageName                               -> "$package$"
      ) ++ TemplateUtils.prepareKeywordsReplacements(keywords, config.keywordValueMap)

      println()

      if (contentFilesReplacements.nonEmpty) {
        println("Content file replacements:")
        println(
          contentFilesReplacements
            .map(r => s"${r._1} -> ${r._2}")
            .mkString("\n"))
      }

      println()

      //---------------------------------------
      // COPY PARAMETRISED PROJECT FILES TO G8
      //---------------------------------------

      val sourcePaths: Iterator[Path] = config.sourceFolder.listRecursively
        .map { source =>
          val sourcePath = config.sourceFolder.relativize(source)
          if (!config.ignoredPaths.exists(
                path => sourcePath.startsWith(path) || sourcePath.getFileName.toString == path)) {
            val targetPath = TemplateUtils.templatePathFor(sourcePath, contentFilesReplacements)
            val target = File(targetG8Folder.path.resolve(targetPath))
            println(s"Processing $sourcePath to $targetPath")
            if (source.isDirectory) {
              config.targetFolder.createDirectoryIfNotExists()
              None
            } else {
              target.createFileIfNotExists(createParents = true)
              target.write(
                TemplateUtils.replace(TemplateUtils.escape(source.contentAsString), contentFilesReplacements))
              Some(sourcePath)
            }
          } else None
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
          config.keywordValueMap))

      //---------------------------------------
      // COPY PARAMETRISED BUILD FILES
      //---------------------------------------

      val buildFilesReplacements = {
        val testTemplateName = config.templateName
          .replace(".g8", "")
          .replaceFirst("template", "test")

        Seq(
          "$templateName$"        -> config.templateName,
          "$templateDescription$" -> config.templateDescription,
          "$gitRepositoryName$"   -> config.templateName,
          "$placeholders$"        -> contentFilesReplacements.map { case (k, v) => s"$v -> $k" }.mkString("\n\t"),
          "$exampleTargetTree$"   -> FileTree.draw(FileTree.compute(sourcePaths)).lines.mkString("\n\t"),
          "$g8CommandLineArgs$" -> s"""${config.keywordValueMap
            .map { case (k, v) => s"""--$k="$v"""" }
            .mkString(" ")}""",
          "$testTargetFolder$" -> config.scriptTestTarget,
          "$testTemplateName$" -> testTemplateName,
          "$testCommand$"      -> config.scriptTestCommand,
          "$beforeTest$"       -> config.scriptBeforeTest.mkString("\n\t"),
          "$makeItG8CommandLine$" ->
            s"""sbt "run --noclear --source ../../${config.scriptTestTarget}/$testTemplateName --target ../.. --name ${config.templateName} --package ${config.packageName} --description ${URLEncoder
              .encode(config.templateDescription, "utf-8")} -K ${config.keywordValueMap
              .map {
                case (k, v) => s"""$k=${URLEncoder.encode(v, "utf-8")}"""
              }
              .mkString(" ")}" """
        )
      }

      println()
      println("Build file replacements:")

      println(
        buildFilesReplacements
          .map(r => s"${r._1} -> ${r._2}")
          .mkString("\n"))

      println()

      config.g8BuildTemplateResources.foreach { path =>
        if (config.createReadme || path != "README.md") {
          Try(Resource.my.getAsString(s"/${config.g8BuildTemplateSource}/$path"))
            .map { content =>
              println(s"Adding build file $path")
              val targetFile = File(config.targetFolder.path.resolve(path))
              targetFile.createFileIfNotExists(createParents = true)
              targetFile
                .clear()
                .write(TemplateUtils
                  .replace(content, buildFilesReplacements))
            }
        } else {
          println(s"Skipping $path")
        }
      }
    }.toEither
}

object MakeItG8Creator extends MakeItG8Creator
