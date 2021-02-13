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

import java.nio.file.{Path, Paths}

import scala.util.Try

object TemplateUtils {

  //---------------------------------------
  // UTILITY AND HELPER FUNCTIONS
  //---------------------------------------

  final def templatePathFor(
    path: Path,
    replacements: Seq[(String, String)],
    inputStats: Map[String, Int] = Map.empty
  ): (Path, Map[String, Int]) = {
    val (s, outputStats) = replace(path.toString, replacements, inputStats)
    (Paths.get(s), outputStats)
  }

  final def escape(text: String): String =
    text
      .replaceAllLiterally("\\", "\\\\")
      .replaceAllLiterally("$", "\\$")

  sealed trait Part {
    val value: String
    val isReplacement: Boolean
  }

  final case class Text(value: String) extends Part {
    val isReplacement: Boolean = false
    def replace(from: String, to: String): (Seq[Part], Int) = {
      val i0 = value.indexOf(from)
      if (i0 < 0) (Seq(this), 0)
      else {
        val (seq, count) =
          Text(value.substring(i0 + from.length))
            .replace(from, to)
        (
          Seq(
            Text(value.substring(0, i0)),
            Replacement(to)
          ) ++ seq,
          count + 1
        )
      }
    }
  }

  final case class Replacement(value: String) extends Part {
    val isReplacement: Boolean = true
  }

  final def replace(
    text: String,
    replacements: Seq[(String, String)],
    placeholderStats: Map[String, Int] = Map.empty
  ): (String, Map[String, Int]) = {
    val initial: Seq[Part] = Seq(Text(text))
    val (parts, outputStats) = replacements
      .sortBy { case (f, _) => -f.length }
      .foldLeft[(Seq[Part], Map[String, Int])]((initial, placeholderStats)) { case ((seq, stats), (from, to)) =>
        val (seq1, count) =
          seq.foldLeft((Seq.empty[Part], stats.getOrElse(to, 0))) {
            case ((s, c), r: Replacement) => (s :+ r, c)
            case ((s, c), t: Text) =>
              val (s1, c1) =
                t.replace(from, to)
              (s ++ s1, c + c1)
          }
        (seq1, stats.updated(to, count))
      }
    (
      parts
        .map(_.value)
        .mkString,
      outputStats
    )
  }

  final def prepareKeywordsReplacements(
    keywords: Seq[String],
    keywordValueMap: Map[String, String]
  ): Seq[(String, String)] =
    keywords.flatMap(k => prepareKeywordReplacement(k, keywordValueMap.getOrElse(k, k)))

  final def prepareKeywordReplacement(keyword: String, value: String): Seq[(String, String)] = {
    val parts = parseWord(value)
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
    }
    else
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

  final def computePlaceholders(
    name: String,
    packageName: Option[String],
    keywords: Seq[String],
    keywordValueMap: Map[String, String],
    placeholderStats: Map[String, Int]
  ): Seq[(String, String)] = {

    val namePropertyValue: Option[String] =
      if (keywords.nonEmpty) Some(s"${keywords.minBy(_.length)}Hyphen") else None

    val placeholderProperties: Seq[(String, String)] =
      keywords.map { keyword =>
        val placeholders = Seq(
          s"""$keyword"""                    -> s"""${keywordValueMap(keyword)}""",
          s"""${keyword}Camel"""             -> s"""$$$keyword;format="Camel"$$""",
          s"""${keyword}camel"""             -> s"""$$$keyword;format="camel"$$""",
          s"""${keyword}NoSpaceLowercase"""  -> s"""$$$keyword;format="camel,lowercase"$$""",
          s"""${keyword}NoSpaceUppercase"""  -> s"""$$$keyword;format="Camel,uppercase"$$""",
          s"""${keyword}Snake"""             -> s"""$$$keyword;format="snake,uppercase"$$""",
          s"""${keyword}snake"""             -> s"""$$$keyword;format="snake,lowercase"$$""",
          s"""${keyword}Package"""           -> s"""$$$keyword;format="package"$$""",
          s"""${keyword}PackageLowercase"""  -> s"""$$$keyword;format="lowercase,package"$$""",
          s"""${keyword}Packaged"""          -> s"""$$$keyword;format="packaged"$$""",
          s"""${keyword}PackagedLowercase""" -> s"""$$$keyword;format="packaged,lowercase"$$""",
          s"""${keyword}Hyphen"""            -> s"""$$$keyword;format="normalize"$$""",
          s"""${keyword}Uppercase"""         -> s"""$$$keyword;format="uppercase"$$""",
          s"""${keyword}Lowercase"""         -> s"""$$$keyword;format="lowercase"$$"""
        )
        val required =
          placeholders.filter { case (key, value) =>
            placeholderStats.get(s"$$$key$$").exists(_ > 0) ||
              namePropertyValue.contains(key)
          }
        if (required.nonEmpty && !required.exists(_._1 == keyword))
          (placeholders(0) +: required)
        else
          required
      }.flatten

    val packageProperties: Seq[(String, String)] =
      if (packageName.isDefined)
        Seq("package" -> packageName.get, "packaged" -> """$package;format="packaged"$""")
      else
        Seq.empty

    val nameProperty: Seq[(String, String)] =
      Seq("name" -> namePropertyValue.map(p => s"$$$p$$").getOrElse(name))

    packageProperties ++ placeholderProperties ++ nameProperty
  }

  final def parseWord(word: String): List[String] =
    word
      .foldLeft((List.empty[String], false)) { case ((list, split), ch) =>
        if (ch == ' ' || ch == '-') (list, true)
        else
          (
            list match {
              case Nil => s"$ch" :: Nil
              case head :: tail =>
                if (split || (head.nonEmpty && shouldSplitAt(head, ch)))
                  s"$ch" :: list
                else
                  s"$ch$head" :: tail
            },
            false
          )
      }
      ._1
      .foldLeft(List.empty[String]) {
        case (Nil, part) => List(part.reverse)
        case (list @ (head :: tail), part) =>
          if (head.length <= 1)
            (part.reverse + head) :: tail
          else
            part.reverse :: list
      }

  import Character._
  final def shouldSplitAt(current: String, next: Char): Boolean = {
    val head = current.head
    (isUpperCase(next) && (!isUpperCase(head) || isDigit(head))) ||
    (isDigit(next) && (current.length > 1) && (isUpperCase(head) || !(isDigit(head) || isPunctuation(head))))
  }

  final def isPunctuation(ch: Char): Boolean =
    ch.getType >= 20 && ch.getType <= 30

  final def uppercase(keyword: String): String = keyword.toUpperCase
  final def lowercase(keyword: String): String = keyword.toLowerCase

  final def capitalize(keyword: String): String =
    keyword.take(1).toUpperCase + keyword.drop(1)

  final def decapitalize(keyword: String): String =
    keyword.take(1).toLowerCase + keyword.drop(1)
}
