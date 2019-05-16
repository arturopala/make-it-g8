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

import org.scalatest.{Matchers, WordSpec}

class TemplateUtilsSpec extends WordSpec with Matchers {

  "TemplateUtils" should {
    "parse keyword" in {
      TemplateUtils.parseKeyword("foo") shouldBe List("foo")
      TemplateUtils.parseKeyword("fooBar") shouldBe List("foo", "Bar")
      TemplateUtils.parseKeyword("FooBar") shouldBe List("Foo", "Bar")
      TemplateUtils.parseKeyword("FooBBar") shouldBe List("Foo", "BBar")
      TemplateUtils.parseKeyword("Foo Bar") shouldBe List("Foo", "Bar")
      TemplateUtils.parseKeyword("Foo bar") shouldBe List("Foo", "bar")
      TemplateUtils.parseKeyword("Foo bar Zoo") shouldBe List("Foo", "bar", "Zoo")
      TemplateUtils.parseKeyword("Foo bar_Zoo") shouldBe List("Foo", "bar_", "Zoo")
      TemplateUtils.parseKeyword("Foo bar_zoo") shouldBe List("Foo", "bar_zoo")
      TemplateUtils.parseKeyword("Foo-bar-Zoo") shouldBe List("Foo", "bar", "Zoo")
      TemplateUtils.parseKeyword("foo-bar-zoo") shouldBe List("foo", "bar", "zoo")
    }

    "create replacements sequence" in {
      TemplateUtils.prepareKeywordReplacement("key", "Foo Bar") shouldBe List(
        ("FooBar", "$keyCamel$"),
        ("fooBar", "$keycamel$"),
        ("FOO_BAR", "$keySnake$"),
        ("Foo.Bar", "$keyPackage$"),
        ("foo.bar", "$keyPackageLowercase$"),
        ("Foo/Bar", "$keyPackaged$"),
        ("foo/bar", "$keyPackagedLowercase$"),
        ("foo-bar", "$keyHyphen$"),
        ("foo bar", "$keyLowercase$"),
        ("FOO BAR", "$keyUppercase$"),
        ("Foo Bar", "$key$")
      )

      TemplateUtils.prepareKeywordReplacement("key", "Foo") shouldBe List(
        ("Foo", "$keyCamel$"),
        ("foo", "$keycamel$"),
        ("foo", "$keyLowercase$"),
        ("FOO", "$keyUppercase$"),
        ("Foo", "$key$")
      )

      TemplateUtils.prepareKeywordReplacement("key", "9786") shouldBe List(
        ("9786", "$key$")
      )
    }

    "create multiple replacements sequences" in {
      TemplateUtils.prepareKeywordsReplacements(Seq("bcde", "ABC"), Map("ABC" -> "abc")) shouldBe
        List(
          ("Bcde", "$bcdeCamel$"),
          ("bcde", "$bcdecamel$"),
          ("bcde", "$bcdeLowercase$"),
          ("BCDE", "$bcdeUppercase$"),
          ("bcde", "$bcde$"),
          ("Abc", "$ABCCamel$"),
          ("abc", "$ABCcamel$"),
          ("abc", "$ABCLowercase$"),
          ("ABC", "$ABCUppercase$"),
          ("abc", "$ABC$")
        )
    }

    "amend the text using provided replacements" in {
      TemplateUtils.replace("Foo Bar", Seq("Foo" -> "$foo$")) shouldBe "$foo$ Bar"
      TemplateUtils.replace("Foo Bar", Seq("Foo" -> "$foo$", "bar" -> "$Bar$")) shouldBe "$foo$ Bar"
      TemplateUtils.replace("Foo Bar", Seq("Foo" -> "$foo$", "Bar" -> "$Bar$")) shouldBe "$foo$ $Bar$"
      TemplateUtils.replace("Foo Bar", Seq("foo" -> "$Foo$", "Bar" -> "$Bar$")) shouldBe "Foo $Bar$"
      TemplateUtils.replace(
        """
          |Foo
          |Zoo
          |Bar
          |
          |999
        """.stripMargin,
        Seq("Foo" -> "$foo$", "Bar" -> "$bar$")) shouldBe
        """
          |$foo$
          |Zoo
          |$bar$
          |
          |999
        """.stripMargin
    }

    "amend the text using provided replacements when overlap" in {
      TemplateUtils.replace("FooFoo Bar", Seq("Foo"    -> "$foo$", "FooF"  -> "$foof$")) shouldBe "$foof$oo Bar"
      TemplateUtils.replace("FooFooFoo Bar", Seq("Foo" -> "$foo$", "FooF"  -> "$foof$")) shouldBe "$foof$oo$foo$ Bar"
      TemplateUtils.replace("FooFooFoo Bar", Seq("Foo" -> "FooFoo", "FooF" -> "FooFF")) shouldBe "FooFFooFooFoo Bar"
      TemplateUtils
        .replace("FooBarFooFoo Bar", Seq("Foo" -> "FooBar", "FooBar" -> "Foo", "Bar" -> "Foo")) shouldBe "FooFooBarFooBar Foo"
    }
  }

}
