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

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TemplateUtilsSpec extends AnyWordSpec with Matchers {

  "TemplateUtils" should {
    "parse keyword" in {
      TemplateUtils.parseWord("") shouldBe Nil
      TemplateUtils.parseWord(" ") shouldBe Nil
      TemplateUtils.parseWord("   ") shouldBe Nil
      TemplateUtils.parseWord("  f ") shouldBe List("f")
      TemplateUtils.parseWord("foo") shouldBe List("foo")
      TemplateUtils.parseWord("fooBar") shouldBe List("foo", "Bar")
      TemplateUtils.parseWord("FooBar") shouldBe List("Foo", "Bar")
      TemplateUtils.parseWord("FooBBar") shouldBe List("Foo", "BBar")
      TemplateUtils.parseWord("Foo Bar") shouldBe List("Foo", "Bar")
      TemplateUtils.parseWord("Foo bar") shouldBe List("Foo", "bar")
      TemplateUtils.parseWord("Foo bar Zoo") shouldBe List("Foo", "bar", "Zoo")
      TemplateUtils.parseWord("Foo bar_Zoo") shouldBe List("Foo", "bar_", "Zoo")
      TemplateUtils.parseWord("Foo bar_zoo") shouldBe List("Foo", "bar_zoo")
      TemplateUtils.parseWord("Foo-bar-Zoo") shouldBe List("Foo", "bar", "Zoo")
      TemplateUtils.parseWord("foo-bar-zoo") shouldBe List("foo", "bar", "zoo")
      TemplateUtils.parseWord("9786") shouldBe List("9786")
      TemplateUtils.parseWord("97#86") shouldBe List("97#86")
      TemplateUtils.parseWord("foo9786") shouldBe List("foo", "9786")
      TemplateUtils.parseWord("Do It G8") shouldBe List("Do", "It", "G8")
      TemplateUtils.parseWord("Play27") shouldBe List("Play", "27")
      TemplateUtils.parseWord("Scala3") shouldBe List("Scala3")
      TemplateUtils.parseWord("Scala3x") shouldBe List("Scala", "3x")
      TemplateUtils.parseWord("Scala 2.13") shouldBe List("Scala", "2.13")
    }

    "create replacements sequence" in {
      TemplateUtils.prepareKeywordReplacement("key", "Foo Bar") shouldBe List(
        ("Foo Bar", "$key$"),
        ("FooBar", "$keyCamel$"),
        ("fooBar", "$keycamel$"),
        ("foobar", "$keyNoSpaceLowercase$"),
        ("FOOBAR", "$keyNoSpaceUppercase$"),
        ("foo_bar", "$keysnake$"),
        ("FOO_BAR", "$keySnake$"),
        ("Foo.Bar", "$keyPackage$"),
        ("foo.bar", "$keyPackageLowercase$"),
        ("Foo/Bar", "$keyPackaged$"),
        ("foo/bar", "$keyPackagedLowercase$"),
        ("foo-bar", "$keyHyphen$"),
        ("foo bar", "$keyLowercase$"),
        ("FOO BAR", "$keyUppercase$")
      )

      TemplateUtils.prepareKeywordReplacement("key", "Foo") shouldBe List(
        ("Foo", "$key$"),
        ("Foo", "$keyCamel$"),
        ("foo", "$keycamel$"),
        ("foo", "$keyLowercase$"),
        ("FOO", "$keyUppercase$")
      )

      TemplateUtils.prepareKeywordReplacement("key", "9786") shouldBe List(
        ("9786", "$key$")
      )

      TemplateUtils.prepareKeywordReplacement("key", "foo9786") shouldBe List(
        ("foo9786", "$key$"),
        ("Foo9786", "$keyCamel$"),
        ("foo9786", "$keycamel$"),
        ("foo9786", "$keyNoSpaceLowercase$"),
        ("FOO9786", "$keyNoSpaceUppercase$"),
        ("foo_9786", "$keysnake$"),
        ("FOO_9786", "$keySnake$"),
        ("foo.9786", "$keyPackage$"),
        ("foo.9786", "$keyPackageLowercase$"),
        ("foo/9786", "$keyPackaged$"),
        ("foo/9786", "$keyPackagedLowercase$"),
        ("foo-9786", "$keyHyphen$"),
        ("foo 9786", "$keyLowercase$"),
        ("FOO 9786", "$keyUppercase$")
      )

      TemplateUtils.prepareKeywordReplacement("key", "Do It G8") shouldBe List(
        ("Do It G8", "$key$"),
        ("DoItG8", "$keyCamel$"),
        ("doItG8", "$keycamel$"),
        ("doitg8", "$keyNoSpaceLowercase$"),
        ("DOITG8", "$keyNoSpaceUppercase$"),
        ("do_it_g8", "$keysnake$"),
        ("DO_IT_G8", "$keySnake$"),
        ("Do.It.G8", "$keyPackage$"),
        ("do.it.g8", "$keyPackageLowercase$"),
        ("Do/It/G8", "$keyPackaged$"),
        ("do/it/g8", "$keyPackagedLowercase$"),
        ("do-it-g8", "$keyHyphen$"),
        ("do it g8", "$keyLowercase$"),
        ("DO IT G8", "$keyUppercase$")
      )
    }

    "create multiple replacements sequences" in {
      TemplateUtils.prepareKeywordsReplacements(Seq("bcde", "ABC"), Map("ABC" -> "abc")) shouldBe
        List(
          ("bcde", "$bcde$"),
          ("Bcde", "$bcdeCamel$"),
          ("bcde", "$bcdecamel$"),
          ("bcde", "$bcdeLowercase$"),
          ("BCDE", "$bcdeUppercase$"),
          ("abc", "$ABC$"),
          ("Abc", "$ABCCamel$"),
          ("abc", "$ABCcamel$"),
          ("abc", "$ABCLowercase$"),
          ("ABC", "$ABCUppercase$")
        )
    }

    "amend the text using provided replacements" in {
      TemplateUtils.replace("Foo Bar", Seq("Foo" -> "$foo$")) shouldBe
        ("$foo$ Bar", Map("$foo$" -> 1))
      TemplateUtils.replace("Foo Bar", Seq("Foo" -> "$foo$", "bar" -> "$Bar$")) shouldBe
        ("$foo$ Bar", Map("$foo$" -> 1, "$Bar$" -> 0))
      TemplateUtils.replace("Foo Bar", Seq("Foo" -> "$foo$", "Bar" -> "$Bar$")) shouldBe
        ("$foo$ $Bar$", Map("$foo$" -> 1, "$Bar$" -> 1))
      TemplateUtils.replace("Foo Bar", Seq("foo" -> "$Foo$", "Bar" -> "$Bar$")) shouldBe
        ("Foo $Bar$", Map("$Foo$" -> 0, "$Bar$" -> 1))
      TemplateUtils
        .replace(
          """
            |Foo
            |Zoo
            |Bar
            |
            |999
        """.stripMargin,
          Seq("Foo" -> "$foo$", "Bar" -> "$bar$")
        ) shouldBe
        ("""
          |$foo$
          |Zoo
          |$bar$
          |
          |999
        """.stripMargin, Map("$foo$" -> 1, "$bar$" -> 1))
    }

    "amend the text using provided replacements when overlap" in {
      TemplateUtils.replace("FooFoo Bar", Seq("Foo" -> "$foo$", "FooF" -> "$foof$")) shouldBe
        ("$foof$oo Bar", Map("$foof$" -> 1, "$foo$" -> 0))

      TemplateUtils.replace(
        "FooFooFoo Bar",
        Seq("Foo" -> "$foo$", "FooF" -> "$foof$")
      ) shouldBe ("$foof$oo$foo$ Bar", Map("$foo$" -> 1, "$foof$" -> 1))

      TemplateUtils.replace(
        "FooFooFoo Bar",
        Seq("Foo" -> "FooFoo", "FooF" -> "FooFF")
      ) shouldBe ("FooFFooFooFoo Bar", Map("FooFoo" -> 1, "FooFF" -> 1))

      TemplateUtils
        .replace(
          "FooBarFooFoo Bar",
          Seq("Foo" -> "FooBar", "FooBar" -> "Foo", "Bar" -> "Foo")
        ) shouldBe ("FooFooBarFooBar Foo", Map("Foo" -> 2, "FooBar" -> 2))
    }
  }

}
