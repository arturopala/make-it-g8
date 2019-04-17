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
        ("Foo Bar", "$key$")
      )

      TemplateUtils.prepareKeywordReplacement("key", "Foo") shouldBe List(
        ("Foo", "$keyCamel$"),
        ("foo", "$keycamel$"),
        ("Foo", "$key$")
      )

      TemplateUtils.prepareKeywordReplacement("key", "9786") shouldBe List(
        ("9786", "$key$")
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
  }

}
