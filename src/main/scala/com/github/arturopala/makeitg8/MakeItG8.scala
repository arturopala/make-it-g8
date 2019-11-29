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

import java.net.URLDecoder

import better.files._
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try

object MakeItG8 extends App with MakeItG8Creator {

  readConfig().fold(
    e => {
      println()
      println(s"Sorry, your command is missing something, ${e.getMessage}!")
      System.exit(-1)
    },
    config =>
      createG8Template(config).fold(
        e => {
          println()
          println(s"Sorry, something went wrong, ${e.getMessage}!")
          System.exit(-1)
        },
        _ => {
          println("Done.")
          System.exit(0)
        }
    )
  )

  def readConfig(): Either[Throwable, MakeItG8Config] =
    Try {

      import scala.collection.JavaConverters._

      //---------------------------------------
      // READ CONFIGURATION AND COMMAND LINE
      //---------------------------------------

      val commandLine = new CommandLine(args)
      val config: Config = ConfigFactory.load()
      val ignoredPaths: List[String] = config.getStringList("source.ignore").asScala.toList
      val sourceFolder = File(commandLine.sourcePath())
      val targetFolder = File(
        commandLine.targetPath
          .map(_.toString)
          .getOrElse(s"${sourceFolder.pathAsString}.g8"))
      val packageName: String = commandLine.packageName()
      val keywordValueMap: Map[String, String] = commandLine.keywords

      val g8BuildTemplateSource = config.getString("build.source")
      val g8BuildTemplateResources = config.getStringList("build.resources").asScala.toList
      val templateName =
        commandLine.templateName.getOrElse(
          commandLine.targetPath
            .map(_.getFileName.toString)
            .getOrElse(sourceFolder.name))
      val scriptTestTarget = config.getString("build.test.folder")
      val scriptTestCommand = config.getString("build.test.command")

      MakeItG8Config(
        sourceFolder,
        targetFolder,
        ignoredPaths,
        templateName,
        packageName,
        keywordValueMap.mapValues(URLDecoder.decode(_, "utf-8")),
        g8BuildTemplateSource,
        g8BuildTemplateResources,
        scriptTestTarget,
        scriptTestCommand,
        config.getStringList("build.test.before").asScala.toList,
        commandLine.clearBuildFiles(),
        commandLine.createReadme(),
        commandLine.templateDescription
          .map(URLDecoder.decode(_, "utf-8"))
          .getOrElse(templateName),
        commandLine.customReadmeHeaderPath.toOption
      )
    }.toEither
}
