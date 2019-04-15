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

import better.files._
import com.typesafe.config.{Config, ConfigFactory}
import org.rogach.scallop.exceptions.RequiredOptionNotFound

import scala.util.control.NonFatal

object MakeItG8 extends App with MakeItG8Creator {

  createG8Template(readConfig())

  def readConfig(): MakeItG8Config = {

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
    val templateName = commandLine.templateName.getOrElse(sourceFolder.name)
    val scriptTestTarget = config.getString("build.test.folder")
    val scriptTestCommand = config.getString("build.test.command")

    MakeItG8Config(
      sourceFolder,
      targetFolder,
      ignoredPaths,
      templateName,
      packageName,
      keywordValueMap,
      g8BuildTemplateSource,
      g8BuildTemplateResources,
      scriptTestTarget,
      scriptTestCommand,
      createBuildFiles = true
    )
  }
}

import org.rogach.scallop._

class CommandLine(arguments: Seq[String]) extends ScallopConf(arguments) {

  val sourcePath = opt[Path](name = "source", short = 's', required = true, descr = "Source code path")
  val targetPath = opt[Path](name = "target", short = 't', descr = "Template target path")
  val templateName = opt[String](name = "name", short = 'n', descr = "Template name")
  val packageName =
    opt[String](name = "package", short = 'p', descr = "Source code base package name", required = true)
  val keywords =
    props[String](name = 'K', keyName = "variable", valueName = "text", descr = "Text chunks to parametrize")

  version("MakeItG8 - convert your project into gitter8 template")
  banner("""Usage: sbt "run --source {PATH} [--target {PATH}] [--name {STRING}] [--package {STRING}] [-Kkey="pattern"]"
           |
           |Options:
           |""".stripMargin)
  footer("\nFor all other tricks, consult the README!")

  mainOptions = Seq(sourcePath, targetPath)

  verify()
  validatePathIsDirectory(sourcePath)
  validatePathExists(sourcePath)

  override def onError(e: Throwable): Unit = e match {
    case _: RequiredOptionNotFound => printHelp()
    case NonFatal(e)               => super.onError(e)
  }
}
