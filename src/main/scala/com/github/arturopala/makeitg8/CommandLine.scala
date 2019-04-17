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

  version("MakeItG8 - convert your project into giter8 template")
  banner(
    """Usage: sbt "run --source {PATH} [--target {PATH}] [--name {STRING}] [--package {STRING}] [--description {STRINGURLENCODED}] [-K key=patternUrlEncoded]"
      |
      |Options:
      |""".stripMargin)

  mainOptions = Seq(sourcePath, targetPath)

  verify()
  validatePathIsDirectory(sourcePath)
  validatePathExists(sourcePath)

  override def onError(e: Throwable): Unit = e match {
    case _: RequiredOptionNotFound => printHelp()
    case _: UnknownOption          => printHelp()
    case NonFatal(ex)              => super.onError(ex)
  }
}
