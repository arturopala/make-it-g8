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

import java.net.URLDecoder

import better.files._
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try
import java.nio.charset.StandardCharsets
import scala.io.StdIn
import scala.annotation.tailrec
import java.nio.file.Paths
import java.net.URI
import java.nio.file.Path
import java.net.URLEncoder

object MakeItG8 extends App with MakeItG8Creator with AskUser with EscapeCodes {

  readConfig().fold(
    {
      case e: CommandLineException =>
        System.exit(-1)
      case e =>
        println()
        println(s"$ANSI_RED\u2716 Fatal, ${e.getMessage}!$ANSI_RESET")
        System.exit(-1)
    },
    config =>
      createG8Template(config).fold(
        e => {
          println()
          println(s"$ANSI_RED\u2716 Fatal, ${e.getMessage}!$ANSI_RESET")
          System.exit(-1)
        },
        _ => {
          println()
          println(s"$ANSI_GREEN\u2714 Done.$ANSI_RESET")
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

      val isInteractive = commandLine.interactiveMode.getOrElse(false)

      if (isInteractive) {
        println()
        println(
          s"${ANSI_YELLOW}MakeItG8$ANSI_RESET $ANSI_BLUE - convert your project into giter8 template$ANSI_RESET"
        )
        println()
      }

      val currentDir = File.currentWorkingDirectory.path.toAbsolutePath()

      def resolveAndCheck(currentDir: Path, other: Path): Option[Path] = {
        val path = currentDir.resolve(other)
        if (path.toFile().exists()) Some(path)
        else throw new Exception(s"source folder $path does not exist")
      }

      def askSourceFolder: Path =
        ask[Path](
          s"""${ANSI_GREEN}Select source project path, absolute or relative [$ANSI_PURPLE$currentDir$ANSI_GREEN]: $ANSI_RESET""",
          s =>
            if (s.isEmpty) None
            else {
              val path = currentDir.resolve(s)
              if (path.toFile().exists()) Some(path)
              else {
                print(CLEAR_PREVIOUS_LINE)
                println(s"$ANSI_RED\u2716 Source folder $path does not exist.$ANSI_RESET")
                val path2 = askSourceFolder
                print(CLEAR_PREVIOUS_LINE)
                Some(path2)
              }
            },
          defaultValue = Some(currentDir)
        )

      val sourceFolder = File(
        commandLine.sourcePath.toOption.flatMap(resolveAndCheck(currentDir, _)).getOrElse {
          if (isInteractive)
            askSourceFolder
          else
            currentDir
        }
      )

      if (isInteractive) {
        print(CLEAR_PREVIOUS_LINE)
        println(s"$CHECK_MARK Selected source folder: $ANSI_YELLOW${sourceFolder.pathAsString}$ANSI_RESET")
      }

      val defaultTarget =
        sourceFolder.path.resolveSibling(sourceFolder.path.getFileName() + ".g8")

      val targetFolder = File(
        commandLine.targetPath.map(currentDir.resolve).getOrElse {
          if (isInteractive)
            ask[Path](
              s"""${ANSI_GREEN}Select target template path, absolute or relative [$ANSI_PURPLE$defaultTarget$ANSI_GREEN]: $ANSI_RESET""",
              s =>
                if (s.isEmpty) None
                else {
                  val s1 = if (s.endsWith(".g8")) s else s"$s.g8"
                  val path = sourceFolder.path.resolve(s1)
                  if (path.toFile().exists && !commandLine.forceOverwrite.getOrElse(false))
                    if (
                      askYesNo(
                        s"${ANSI_GREEN}Target folder $ANSI_YELLOW${path.toString}$ANSI_GREEN exists, are you happy to overwrite it? (y/n): $ANSI_RESET"
                      )
                    )
                      Some(path)
                    else
                      None
                  else
                    Some(path)
                },
              defaultValue = Some(defaultTarget)
            )
          else defaultTarget
        }
      )

      if (isInteractive) {
        print(CLEAR_PREVIOUS_LINE)
        println(s"$CHECK_MARK Selected target folder: $ANSI_YELLOW${targetFolder.pathAsString}$ANSI_RESET")
      }
      else if (targetFolder.exists && !commandLine.forceOverwrite.getOrElse(false)) {
        if (
          !askYesNo(
            s"${ANSI_GREEN}Target folder $ANSI_YELLOW${targetFolder.toString}$ANSI_GREEN exists, are you happy to overwrite it? (y/n): $ANSI_RESET"
          )
        ) {
          throw new Exception("cancelled by the user")
        }
      }

      val templateName: String =
        commandLine.templateName.getOrElse {
          val defaultName = targetFolder.path.getFileName.toString
          if (isInteractive)
            askString(
              s"${ANSI_GREEN}What should be the name of the template? [$defaultName]: $ANSI_RESET",
              Some(defaultName)
            )
          else
            defaultName
        }

      if (isInteractive) {
        print(CLEAR_PREVIOUS_LINE)
        println(s"$CHECK_MARK Selected template name: $ANSI_YELLOW$templateName$ANSI_RESET")
      }

      val packageName: Option[String] = commandLine.packageName.toOption
        .flatMap(p => if (p.trim.isEmpty) None else Some(p))
        .orElse(
          if (isInteractive)
            askOptional(s"${ANSI_GREEN}What is your root package name? [optional] $ANSI_RESET")
          else {
            println(
              s"[${ANSI_YELLOW}warn$ANSI_RESET] No --package option, package name will stay not parametrized"
            )
            None
          }
        )

      if (isInteractive) {
        print(CLEAR_PREVIOUS_LINE)
        if (packageName.isDefined)
          println(s"$CHECK_MARK Selected root package name: $ANSI_YELLOW${packageName.get}$ANSI_RESET")
        else
          println(s"$CHECK_MARK Selected no root package name$ANSI_RESET")
      }

      @tailrec
      def askNextKeyword(map: Map[String, String]): Map[String, String] = {
        val word =
          askOptional(s"${ANSI_GREEN}Input a phrase which should be parametrized, or leave empty to skip: $ANSI_RESET")
        print(CLEAR_PREVIOUS_LINE)
        if (word.nonEmpty) {
          if (map.exists(_._2 == word.get))
            askNextKeyword(map)
          else {
            val defaultKey =
              TemplateUtils.decapitalize(TemplateUtils.parseKeyword(word.get).map(TemplateUtils.capitalize).mkString)
            val key =
              askString(
                s"""${ANSI_GREEN}Select the key for "$ANSI_PURPLE${word.get}$ANSI_RESET" phrase, default [$defaultKey]: $ANSI_RESET""",
                Some(defaultKey)
              )
            print(CLEAR_PREVIOUS_LINE)
            println(s"""\t$ANSI_CYAN$$$key$$$ANSI_GREEN \u2192 $ANSI_PURPLE${word.get}$ANSI_RESET"""")
            askNextKeyword(map.updated(key, URLEncoder.encode(word.get, "utf-8")))
          }
        }
        else map
      }

      val keywordValueMap: Map[String, String] = {
        val defaultKeywords = commandLine.keywords
        if (isInteractive) {
          println(s"""$ANSI_RESET  Define parametrized phrases:$ANSI_RESET""")
          if (defaultKeywords.nonEmpty) {
            defaultKeywords.foreach { case (k, v) =>
              println(
                s"""\t$ANSI_CYAN$$$k$$$ANSI_GREEN \u2192 $ANSI_PURPLE${URLDecoder.decode(v, "utf-8")}$ANSI_RESET"""
              )
            }
          }
          askNextKeyword(defaultKeywords.m)
        }
        else defaultKeywords
      }

      val g8BuildTemplateSource = config.getString("build.source")
      val g8BuildTemplateResources = config.getStringList("build.resources").asScala.toList

      val scriptTestTarget = config.getString("build.test.folder")

      val scriptTestCommand = {
        val defaultCommand = config.getString("build.test.command")
        if (isInteractive) {
          askString(
            s"${ANSI_GREEN}What is the build & test command? [$defaultCommand]: $ANSI_RESET",
            Some(defaultCommand)
          )
        }
        else defaultCommand
      }

      if (isInteractive) {
        print(CLEAR_PREVIOUS_LINE)
        println(s"$CHECK_MARK Selected build & test command: $ANSI_YELLOW$scriptTestCommand$ANSI_RESET")
      }

      val ignoredPaths: List[String] = {
        val gitignore: File = sourceFolder / ".gitignore"
        ".git/" ::
          (if (gitignore.exists) {
             GitIgnore.parseGitIgnore(gitignore.contentAsString(StandardCharsets.UTF_8))
           }
           else {
             println(
               s"$ANSI_YELLOW\u2757 No .gitignore file found, processing all nested files and folders.$ANSI_RESET"
             )
             Nil
           })
      }

      if (isInteractive && ignoredPaths.size > 1) {
        println(
          s"$CHECK_MARK Ignore files and folders matching the following patterns:$ANSI_RESET"
        )
        ignoredPaths.foreach(pattern => println(s"\t$ANSI_YELLOW$pattern$ANSI_RESET"))
        println()
      }

      val proceed =
        if (isInteractive)
          askYesNo("Proceed (y/n):")
        else true

      if (proceed)
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
      else {
        throw new Exception("cancelled by the user")
      }
    }.toEither
}
