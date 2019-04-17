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

import java.nio.file.Path

import org.rogach.scallop.ScallopConf
import org.rogach.scallop.exceptions.{RequiredOptionNotFound, UnknownOption}

import scala.util.control.NonFatal

class CommandLine(arguments: Seq[String]) extends ScallopConf(arguments) {

  val sourcePath = opt[Path](name = "source", short = 's', required = true, descr = "Source code path")
  val targetPath = opt[Path](name = "target", short = 't', descr = "Template target path")
  val templateName = opt[String](name = "name", short = 'n', descr = "Template name")
  val packageName =
    opt[String](name = "package", short = 'p', descr = "Source code base package name", required = true)
  val keywords =
    props[String](name = 'K', keyName = "variable", valueName = "text", descr = "Text chunks to parametrize")
  val templateDescription = opt[String](name = "description", short = 'd', descr = "Template description")

  val clearBuildFiles = toggle(
    name = "clear",
    short = 'c',
    descrYes = "Clear target folder",
    descrNo = "Do not clear whole target folder, only src/main/g8 subfolder",
    default = Some(true)
  )
  val createReadme = toggle(
    name = "readme",
    short = 'r',
    descrYes = "Create readme",
    descrNo = "Do not create/update readme",
    default = Some(true)
  )

  version("MakeItG8 - convert your project into giter8 template")
  banner(
    """Usage: sbt "run --source {PATH} [--target {PATH}] [--name {STRING}] [--package {STRING}] [--description {STRINGURLENCODED}] [-K key=patternUrlEncoded]"
      |
      |Options:
      |""".stripMargin)

  mainOptions = Seq(sourcePath, targetPath)

  override def onError(e: Throwable): Unit = e match {
    case _: RequiredOptionNotFound => printHelp()
    case _: UnknownOption          => printHelp()
    case NonFatal(ex)              => super.onError(ex)
  }

  validatePathIsDirectory(sourcePath)
  validatePathExists(sourcePath)

  verify()
}
