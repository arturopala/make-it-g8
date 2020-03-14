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

import scala.util.Try

object TemplateUtils {

  //---------------------------------------
  // UTILITY AND HELPER FUNCTIONS
  //---------------------------------------

  def templatePathFor(path: Path, replacements: Seq[(String, String)]): Path =
    Paths.get(
      replacements
        .foldLeft(path.toString) { case (a, (f, t)) => a.replaceAllLiterally(f, t) })

  def escape(text: String): String =
    text
      .replaceAllLiterally("\\", "\\\\")
      .replaceAllLiterally("$", "\\$")

  sealed trait Part {
    val value: String
  }

  case class Text(value: String) extends Part {
    def replace(from: String, to: String): Seq[Part] = {
      val i0 = value.indexOf(from)
      if (i0 < 0) Seq(this)
      else
        Seq(
          Text(value.substring(0, i0)),
          Replacement(to)
        ) ++ Text(value.substring(i0 + from.length)).replace(from, to)
    }
  }

  case class Replacement(value: String) extends Part

  def replace(text: String, replacements: Seq[(String, String)]): String = {
    val initial: Seq[Part] = Seq(Text(text))
    replacements
      .sortBy { case (f, _) => -f.length }
      .foldLeft(initial) {
        case (seq, (from, to)) =>
          seq.flatMap {
            case r: Replacement => Seq(r)
            case t: Text        => t.replace(from, to)
          }
      }
      .map(_.value)
      .mkString
  }

  def prepareKeywordsReplacements(keywords: Seq[String], keywordValueMap: Map[String, String]): Seq[(String, String)] =
    keywords.flatMap(k => prepareKeywordReplacement(k, keywordValueMap.getOrElse(k, k)))

  def prepareKeywordReplacement(keyword: String, value: String): Seq[(String, String)] = {
    val parts = parseKeyword(value)
    val lowercaseParts = parts.map(lowercase)
    val uppercaseParts = parts.map(uppercase)

    if (parts.size == 1) {
      if (Try(parts.head.toInt).isSuccess)
        Seq(value -> s"$$$keyword$$")
      else
        Seq(
          lowercaseParts.map(capitalize).mkString("")               -> s"$$${keyword}Camel$$",
          decapitalize(lowercaseParts.map(capitalize).mkString("")) -> s"$$${keyword}camel$$",
          lowercaseParts.mkString(" ")                              -> s"$$${keyword}Lowercase$$",
          uppercaseParts.mkString(" ")                              -> s"$$${keyword}Uppercase$$",
          value                                                     -> s"$$$keyword$$"
        )
    } else
      Seq(
        lowercaseParts.map(capitalize).mkString("")               -> s"$$${keyword}Camel$$",
        decapitalize(lowercaseParts.map(capitalize).mkString("")) -> s"$$${keyword}camel$$",
        lowercaseParts.mkString("")                               -> s"$$${keyword}NoSpaceLowercase$$",
        uppercaseParts.mkString("")                               -> s"$$${keyword}NoSpaceUppercase$$",
        lowercaseParts.mkString("_")                              -> s"$$${keyword}snake$$",
        uppercaseParts.mkString("_")                              -> s"$$${keyword}Snake$$",
        parts.mkString(".")                                       -> s"$$${keyword}Package$$",
        lowercaseParts.mkString(".")                              -> s"$$${keyword}PackageLowercase$$",
        parts.mkString("/")                                       -> s"$$${keyword}Packaged$$",
        lowercaseParts.mkString("/")                              -> s"$$${keyword}PackagedLowercase$$",
        lowercaseParts.mkString("-")                              -> s"$$${keyword}Hyphen$$",
        lowercaseParts.mkString(" ")                              -> s"$$${keyword}Lowercase$$",
        uppercaseParts.mkString(" ")                              -> s"$$${keyword}Uppercase$$",
        value                                                     -> s"$$$keyword$$"
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
          s"""${keyword}NoSpaceLowercase=$$$keyword;format="camel,lowercase"$$""",
          s"""${keyword}NoSpaceUppercase=$$$keyword;format="Camel,uppercase"$$""",
          s"""${keyword}Snake=$$$keyword;format="snake,uppercase"$$""",
          s"""${keyword}snake=$$$keyword;format="snake,lowercase"$$""",
          s"""${keyword}Package=$$$keyword;format="package"$$""",
          s"""${keyword}PackageLowercase=$$$keyword;format="lowercase,package"$$""",
          s"""${keyword}Packaged=$$$keyword;format="packaged"$$""",
          s"""${keyword}PackagedLowercase=$$$keyword;format="packaged,lowercase"$$""",
          s"""${keyword}Hyphen=$$$keyword;format="normalize"$$""",
          s"""${keyword}Uppercase=$$$keyword;format="uppercase"$$""",
          s"""${keyword}Lowercase=$$$keyword;format="lowercase"$$"""
        )
      }
      .mkString("\n")
    s"""$keywordsMapping
       |package=$packageName
       |packaged=$$package;format="packaged"$$
       |name=${if (keywords.nonEmpty) s"""$$${keywords.minBy(_.length)}Hyphen$$""" else name}
     """.stripMargin
  }

  def parseKeyword(keyword: String): List[String] =
    keyword
      .foldLeft((List.empty[String], false)) {
        case ((list, split), ch) =>
          if (ch == ' ' || ch == '-') (list, true)
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
