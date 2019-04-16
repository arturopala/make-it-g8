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

import java.nio.file.{Path, Paths}

import better.files.{File, Resource}

trait MakeItG8Creator {

  def createG8Template(config: MakeItG8Config): Unit = {

    println(s"Processing ${config.sourceFolder} into giter8 template ${config.targetFolder} ...")
    if (config.targetFolder.exists && config.createBuildFiles) {
      println(s"Target folder exists, clearing ${config.targetFolder.path} to make space for a new template project")
      config.targetFolder.clear()
    } else {
      config.targetFolder.createDirectoryIfNotExists()

    }

    val targetG8Folder = (config.targetFolder / "src" / "main" / "g8").createDirectoryIfNotExists()
    if (!config.createBuildFiles) {
      println(s"Clearing ${targetG8Folder.path} to make space for a new template")
      targetG8Folder.clear()
    }

    //---------------------------------------
    // PREPARE CONTENT REPLACEMENT KEYWORDS
    //---------------------------------------

    val keywords: Seq[String] = config.keywordValueMap.keys.toSeq
    val contentFilesReplacements: Seq[(String, String)] = Seq(
      config.packageName.replaceAllLiterally(".", "/") -> "$packaged$",
      config.packageName                               -> "$package$"
    ) ++ prepareKeywordsReplacements(keywords, config.keywordValueMap)

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
        if (!config.ignoredPaths.exists(path => sourcePath.startsWith(path) || sourcePath.getFileName.toString == path)) {
          val targetPath = templatePathFor(sourcePath, contentFilesReplacements)
          val target = File(targetG8Folder.path.resolve(targetPath))
          println(s"Processing $sourcePath to $targetPath")
          if (source.isDirectory) {
            config.targetFolder.createDirectoryIfNotExists()
            None
          } else {
            target.createFileIfNotExists(createParents = true)
            target.write(replace(source.contentAsString, contentFilesReplacements))
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
      prepareDefaultProperties(
        config.sourceFolder.path.getFileName.toString,
        config.packageName,
        keywords,
        config.keywordValueMap))

    //---------------------------------------
    // COPY PARAMETRISED BUILD FILES
    //---------------------------------------

    val buildFilesReplacements = Seq(
      "$templateName$"        -> config.templateName,
      "$templateDescription$" -> config.templateDescription,
      "$gitRepositoryName$"   -> config.templateName,
      "$placeholders$"        -> contentFilesReplacements.map { case (k, v) => s"$v -> $k" }.mkString("\n\t"),
      "$exampleTargetTree$"   -> PathsTree.draw(PathsTree.compute(sourcePaths)),
      "$g8CommandLineArgs$"   -> s"""${config.keywordValueMap.map { case (k, v) => s"""--$k="$v"""" }.mkString(" ")}""",
      "$testTargetFolder$"    -> config.scriptTestTarget,
      "$testTemplateName$" -> contentFilesReplacements
        .find(_._2 == s"$$${keywords.min}Hyphen$$")
        .map(_._1)
        .getOrElse(config.sourceFolder.path.getFileName.toString),
      "$testCommand$" -> config.scriptTestCommand
    )

    println()
    println("Build file replacements:")

    println(
      buildFilesReplacements
        .map(r => s"${r._1} -> ${r._2}")
        .mkString("\n"))

    println()

    config.g8BuildTemplateResources.foreach { path =>
      val targetFile = File(config.targetFolder.path.resolve(path))
      val content = Resource.my.getAsString(s"/${config.g8BuildTemplateSource}/$path")
      println(s"Adding build file $path")
      targetFile.createFileIfNotExists(createParents = true)
      val lines = content.lines
      targetFile
        .clear()
        .printLines(lines.map(line =>
          buildFilesReplacements
            .foldLeft(line) { case (a, (f, t)) => a.replaceAllLiterally(f, t) }))
    }
  }

  //---------------------------------------
  // UTILITY AND HELPER FUNCTIONS
  //---------------------------------------

  def templatePathFor(path: Path, replacements: Seq[(String, String)]): Path =
    Paths.get(
      replacements
        .foldLeft(path.toString) { case (a, (f, t)) => a.replaceAllLiterally(f, t) })

  def replace(text: String, replacements: Seq[(String, String)]): String =
    replacements
      .foldLeft(text.replaceAllLiterally("\\", "\\\\").replaceAllLiterally("$", "\\$")) {
        case (a, (f, t)) => a.replaceAllLiterally(f, t)
      }

  def prepareKeywordsReplacements(keywords: Seq[String], keywordValueMap: Map[String, String]): Seq[(String, String)] =
    keywords.flatMap(prepareKeywordReplacement(_, keywordValueMap))

  def prepareKeywordReplacement(keyword: String, keywordValueMap: Map[String, String]): Seq[(String, String)] = {
    val value = keywordValueMap(keyword)
    val parts = parseKeyword(value)
    Seq(
      parts.map(lowercase).map(capitalize).mkString("")               -> s"$$${keyword}Camel$$",
      decapitalize(parts.map(lowercase).map(capitalize).mkString("")) -> s"$$${keyword}camel$$",
      parts.map(uppercase).mkString("_")                              -> s"$$${keyword}Snake$$",
      parts.mkString(".")                                             -> s"$$${keyword}Package$$",
      parts.map(lowercase).mkString(".")                              -> s"$$${keyword}PackageLowercase$$",
      parts.mkString("/")                                             -> s"$$${keyword}Packaged$$",
      parts.map(lowercase).mkString("/")                              -> s"$$${keyword}PackagedLowercase$$",
      parts.map(lowercase).mkString("-")                              -> s"$$${keyword}Hyphen$$",
      value                                                           -> s"$$$keyword$$"
    )
  }

  def prepareDefaultProperties(
    name: String,
    packageName: String,
    keywords: Seq[String],
    keywordValueMap: Map[String, String]): String = {
    val keywordsMapping = keywords
      .flatMap { keyword =>
        Seq(
          s"""$keyword=${keywordValueMap(keyword)}""",
          s"""${keyword}Camel=$$$keyword;format="Camel"$$""",
          s"""${keyword}camel=$$$keyword;format="camel"$$""",
          s"""${keyword}Snake=$$$keyword;format="snake"$$""",
          s"""${keyword}Package=$$$keyword;format="package"$$""",
          s"""${keyword}PackageLowercase=$$$keyword;format="lowercase,package"$$""",
          s"""${keyword}Packaged=$$$keyword;format="packaged"$$""",
          s"""${keyword}PackagedLowercase=$$$keyword;format="packaged,lowercase"$$""",
          s"""${keyword}Hyphen=$$$keyword;format="normalize"$$"""
        )
      }
      .mkString("\n")
    s"""$keywordsMapping
       |package=$packageName
       |packaged=$$package;format="packaged"$$
       |name=${if (keywords.nonEmpty) s"""$$${keywords.min}Hyphen$$""" else name}
     """.stripMargin
  }

  def parseKeyword(keyword: String): List[String] =
    keyword
      .foldLeft((List.empty[String], false)) {
        case ((list, split), ch) =>
          if (ch == ' ') (list, true)
          else
            (list match {
              case Nil => s"$ch" :: Nil
              case head :: tail =>
                if (split || splitAt(head.head, ch))
                  s"$ch" :: list
                else
                  s"$ch$head" :: tail
            }, false)
      }
      ._1
      .map(_.reverse)
      .reverse

  import Character._
  def splitAt(prev: Char, ch: Char): Boolean =
    (isUpperCase(ch) && (!isUpperCase(prev) || isDigit(prev))) ||
      (isDigit(ch) && (!isDigit(prev) || isUpperCase(prev)))

  def uppercase(keyword: String): String = keyword.toUpperCase
  def lowercase(keyword: String): String = keyword.toLowerCase

  def capitalize(keyword: String): String =
    keyword.take(1).toUpperCase + keyword.drop(1)

  def decapitalize(keyword: String): String =
    keyword.take(1).toLowerCase + keyword.drop(1)
}

object MakeItG8Creator extends MakeItG8Creator
