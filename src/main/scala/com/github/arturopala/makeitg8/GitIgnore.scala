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
import scala.collection.JavaConverters.asScalaIterator
import scala.util.matching.Regex
import scala.annotation.tailrec

/** Filter paths using .gitignore patterns.
  *
  * See:
  *  - https://git-scm.com/docs/gitignore
  *  - https://www.man7.org/linux/man-pages/man7/glob.7.html
  *
  * @note
  *   Paths representing directories MUST end with a slash,
  *   paths representing files MUST NOT end with a slash.
  */
final case class GitIgnore(gitPatterns: Seq[String]) {

  import GitIgnore._

  private final val patterns: Seq[Pattern] = gitPatterns
    .map(GitIgnore.parseGitPattern)

  final def isIgnored(path: Path): Boolean =
    isIgnored(
      asScalaIterator(path.iterator()).map(_.toString).toIterable,
      path.toFile().isDirectory()
    )

  final def isIgnored(path: Path, isDirectory: Boolean): Boolean =
    isIgnored(
      asScalaIterator(path.iterator()).map(_.toString).toIterable,
      isDirectory
    )

  final def isIgnored(path: Iterable[String], isDirectory: Boolean): Boolean =
    isIgnored(
      path.toSeq.mkString("/", "/", if (isDirectory) "/" else "")
    )

  /** Path may end with slash [/] only if it denotes a directory. */
  final def isIgnored(path: String): Boolean =
    patterns
      .foldLeft[Vote](Abstain)((vote, pattern) => Vote.combine(vote, pattern.isIgnored(ensureStartSlash(path)))) match {
      case Ignore(_)             => true
      case Abstain | Unignore(_) => false
    }
}

object GitIgnore {

  /** Parse .gitignore file content into GitIgnore instance. */
  def parse(gitIgnore: String): GitIgnore =
    GitIgnore(parseGitIgnore(gitIgnore))

  /** Parse .gitignore file content and return sequence of patterns. */
  def parseGitIgnore(gitIgnore: String): Seq[String] =
    gitIgnore.lines.collect {
      case line if line.trim.nonEmpty && !line.startsWith("#") =>
        removeTrailingNonEscapedSpaces(line)
    }.toList

  /** Create GitIgnore from a single well-formed pattern. */
  def apply(gitPattern: String): GitIgnore =
    GitIgnore(Seq(gitPattern))

  /** Internal model of the path pattern. */
  sealed trait Pattern {
    def isIgnored(path: String): Vote
  }

  /** Parse single Git pattern into internal representation. */
  final def parseGitPattern(p: String): Pattern =
    if (p.startsWith("!"))
      Negate(parseGitPattern(p.drop(1)))
    else if (p.startsWith("/") || p.dropRight(1).contains("/")) {
      if (p.startsWith("**/"))
        AnyPathPattern(ensureEndSlash(p))
      else if (p.endsWith("/**"))
        AnyPathPattern(ensureStartSlash(p))
      else {
        if (p.endsWith("/"))
          DirectoryPrefixPattern(p)
        else
          AnyPrefixPattern(p)
      }
    }
    else if (p.endsWith("/"))
      AnyDirectoryPattern(p)
    else
      AnyNamePattern(p)

  /** Matches any single segment of the path. */
  final case class AnyNamePattern(gitPattern: String) extends Pattern {
    private val matcher =
      Matcher(ensureStartEndSlash(gitPattern))

    override def isIgnored(path: String): Vote =
      matcher
        .isPartOf(ensureEndSlash(path))
        .asVote
  }

  /** Matches any segments chain of the path. */
  final case class AnyPathPattern(gitPattern: String) extends Pattern {
    private val matcher =
      Matcher(gitPattern)

    override def isIgnored(path: String): Vote =
      matcher
        .isPartOf(ensureEndSlash(path))
        .asVote
  }

  /** Matches any directories chain of the path. */
  final case class AnyDirectoryPattern(gitPattern: String) extends Pattern {
    private val matcher =
      Matcher(ensureStartEndSlash(gitPattern))

    override def isIgnored(path: String): Vote =
      matcher
        .isPartOf(path)
        .asVote
  }

  /** Matches any initial segments prefix of the path. */
  final case class AnyPrefixPattern(gitPattern: String) extends Pattern {
    private val matcher =
      Matcher(ensureStartEndSlash(gitPattern))

    override def isIgnored(path: String): Vote =
      matcher
        .isPrefixOf(ensureEndSlash(path))
        .asVote
  }

  /** Matches initial directories prefix of the path. */
  final case class DirectoryPrefixPattern(gitPattern: String) extends Pattern {
    private val matcher =
      Matcher(ensureStartEndSlash(gitPattern))

    override def isIgnored(path: String): Vote =
      matcher
        .isPrefixOf(path)
        .asVote
  }

  /** Reverts match, if any, of the nested pattern. */
  final case class Negate(nestedPattern: Pattern) extends Pattern {
    override def isIgnored(path: String): Vote =
      nestedPattern.isIgnored(path) match {
        case Abstain     => Abstain
        case Ignore(p)   => Unignore(p)
        case Unignore(p) => Unignore(p)
      }
  }

  sealed trait Vote
  case class Ignore(position: Int) extends Vote
  case object Abstain extends Vote
  case class Unignore(position: Int) extends Vote

  object Vote {
    final def combine(left: Vote, right: => Vote): Vote = left match {
      case Abstain | Unignore(_) => right
      case Ignore(p1) =>
        right match {
          case Abstain    => left
          case Ignore(p2) => Ignore(Math.min(p1, p2))
          case Unignore(p2) =>
            if (p2 <= p1) Abstain else left
        }
    }
  }

  private val NONE = -1

  /** Matches path against the Git pattern.
    *
    * Each method returns the furthest matched position,
    * or -1 if not matched at all.
    */
  sealed trait Matcher {
    def isPartOf(path: String): Int
    def isPrefixOf(path: String): Int
    def isSuffixOf(path: String): Int
  }

  object Matcher {
    def apply(gitPattern: String): Matcher =
      if (RegexpMatcher.isRegexpPattern(gitPattern))
        RegexpMatcher(gitPattern)
      else
        LiteralMatcher(RegexpMatcher.unescape(gitPattern))
  }

  /** Matches path literally with Git pattern. */
  final case class LiteralMatcher(gitPattern: String) extends Matcher {
    final override def isPartOf(path: String): Int =
      path.endOfMatch(gitPattern)

    final override def isPrefixOf(path: String): Int =
      if (path.startsWith(gitPattern)) gitPattern.length else NONE

    final override def isSuffixOf(path: String): Int =
      if (path.endsWith(gitPattern)) path.length else NONE
  }

  /** Matches path using Git pattern compiled into Java regular expression. */
  final case class RegexpMatcher(gitPattern: String) extends Matcher {
    final lazy val pattern =
      RegexpMatcher.compile(gitPattern)

    final override def isPartOf(path: String): Int = {
      val m = pattern.matcher(path)
      if (m.find()) m.end else NONE
    }

    final override def isPrefixOf(path: String): Int = {
      val m = pattern.matcher(path)
      if (m.find() && m.start() == 0) m.end else NONE
    }

    final override def isSuffixOf(path: String): Int = {
      val m = pattern.matcher(path)
      if (m.find() && m.end() == path.length) m.end else NONE
    }
  }

  object RegexpMatcher {

    /** Regular expression detecting if Git pattern needs regexp matcher. */
    final val gitPatternRegexp: java.util.regex.Pattern =
      java.util.regex.Pattern.compile("""(?<!\\)(\*\*|\*|\?|\[[\p{Graph}]+\])""")

    /** Check if Git pattern needs regexp matcher. */
    final def isRegexpPattern(pattern: String): Boolean =
      gitPatternRegexp.matcher(pattern).find()

    /** Compiles .gitignore pattern into Java regular expression.
      *
      * See: https://www.man7.org/linux/man-pages/man7/glob.7.html
      */
    final def compile(gitPattern: String): java.util.regex.Pattern = {
      val matcher = gitPatternRegexp.matcher(gitPattern)
      val buffer = new StringBuffer()
      var z = 0
      while (matcher.find()) {
        val s = matcher.start()
        val e = matcher.end()
        val m = gitPattern.substring(s, e)
        if (s > z)
          buffer
            .append(java.util.regex.Pattern.quote(unescape(gitPattern.substring(z, s))))
        buffer.append(m match {
          case "*"  => """[^/]*?"""
          case "**" => """\p{Graph}*"""
          case "?"  => "[^/]"
          case s if s.startsWith("[") && s.endsWith("]") =>
            s.replaceAllLiterally("[!", "[^")
          case _ => m
        })
        z = e
      }
      if (z < gitPattern.length())
        buffer
          .append(java.util.regex.Pattern.quote(unescape(gitPattern.substring(z, gitPattern.length()))))

      java.util.regex.Pattern.compile(buffer.toString)
    }

    final def unescape(s: String): String =
      s.replaceAllLiterally("\\*", "*")
        .replaceAllLiterally("\\?", "?")
        .replaceAllLiterally("\\[", "[")
        .replaceAllLiterally("\\ ", " ")
  }

  private def ensureStartSlash(s: String): String =
    if (s.startsWith("/")) s else "/" + s

  private def ensureEndSlash(s: String): String =
    if (s.endsWith("/")) s else s + "/"

  private def ensureStartEndSlash(s: String): String =
    ensureStartSlash(ensureEndSlash(s))

  @tailrec
  private def removeTrailingNonEscapedSpaces(s: String): String =
    if (s.endsWith("\\ ")) s
    else if (s.endsWith(" "))
      removeTrailingNonEscapedSpaces(s.dropRight(1))
    else s

  private implicit class IntExtensions(val position: Int) extends AnyVal {
    final def asVote: Vote =
      if (position >= 0)
        Ignore(position)
      else
        Abstain
  }

  private implicit class StringExtensions(val string: String) extends AnyVal {
    final def endOfMatch(word: String): Int = {
      val i = string.indexOf(word)
      if (i < 0) NONE else i + word.length()
    }
  }
}
