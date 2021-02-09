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

import java.nio.file.Path

import org.rogach.scallop.ScallopConf
import org.rogach.scallop.exceptions.{RequiredOptionNotFound, UnknownOption}

import scala.util.control.NonFatal
import scala.util.Try

class CommandLine(arguments: Seq[String]) extends ScallopConf(arguments) with EscapeCodes {

  val sourcePath =
    opt[Path](name = "source", short = 's', descr = "Source code path, absolute or relative")

  val packageName =
    opt[String](name = "package", short = 'p', descr = "Source code base package name")

  val targetPath =
    opt[Path](name = "target", short = 't', descr = "Template target path, absolute or relative")

  val templateName =
    opt[String](name = "name", short = 'n', descr = "Template name")

  val keywords =
    props[String](name = 'K', keyName = "placeholder", valueName = "text", descr = "Text chunks to parametrize")

  val templateDescription =
    opt[String](name = "description", short = 'd', descr = "Template description")

  val customReadmeHeaderPath =
    opt[String](
      name = "custom-readme-header-path",
      short = 'x',
      descr = "Custom README.md header path",
      argName = "path"
    )

  val clearBuildFiles =
    toggle(
      name = "clear",
      short = 'c',
      descrYes = "Clear target folder",
      descrNo = "Do not clear whole target folder, only src/main/g8 subfolder",
      default = Some(true)
    )
  val createReadme =
    toggle(
      name = "readme",
      short = 'r',
      descrYes = "Create readme",
      descrNo = "Do not create/update readme",
      default = Some(true)
    )

  val interactiveMode =
    toggle(
      name = "interactive",
      short = 'i',
      noshort = false,
      descrYes = "Interactive mode",
      default = Some(Option(System.getProperty("makeitg8.interactive")).map(_.toBoolean).getOrElse(false))
    )

  val forceOverwrite =
    toggle(
      name = "force",
      short = 'f',
      noshort = false,
      descrYes = "Force overwriting target folder",
      default = Some(false)
    )

  version(s"\r\n${ANSI_YELLOW}MakeItG8$ANSI_RESET $ANSI_BLUE - convert your project into a giter8 template$ANSI_RESET")

  banner(
    s"""
      |${ANSI_BLUE}Usage:$ANSI_RESET sbt "run [--source {PATH}] [--target {PATH}] [--force] [--name {STRING}] [--package {STRING}] [--description {STRINGURLENCODED}] [--custom-readme-header-path {PATH}] [-K placeholder=textURLEncoded]"
      |
      |${ANSI_BLUE}Interactive mode:$ANSI_RESET sbt "run --interactive ..."
      |
      |${ANSI_BLUE}Options:$ANSI_RESET
      |""".stripMargin
  )

  mainOptions = Seq(sourcePath, packageName)

  override def onError(e: Throwable): Unit = e match {
    case NonFatal(ex) =>
      printHelp()
      throw new CommandLineException()
  }

  verify()
}

class CommandLineException extends Exception
