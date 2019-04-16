package com.github.arturopala.makeitg8

import java.nio.file.{Path, Paths}

import scala.util.Try

object TemplateUtils {

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
    if (parts.size == 1) {
      if (Try(parts.head.toInt).isSuccess)
        Seq(value -> s"$$$keyword$$")
      else
        Seq(
          parts.map(lowercase).map(capitalize).mkString("")               -> s"$$${keyword}Camel$$",
          decapitalize(parts.map(lowercase).map(capitalize).mkString("")) -> s"$$${keyword}camel$$",
          value                                                           -> s"$$$keyword$$"
        )
    } else
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
