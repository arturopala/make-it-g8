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

import java.nio.file.Paths

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import java.nio.file.{Path, Paths}

class GitIgnoreSpec extends AnyWordSpec with Matchers {

  val gitIgnore1 = GitIgnore(Seq(".git", "build.sbt", "target", ".scalafmt.conf"))
  val gitIgnore2 = GitIgnore(Seq(".git/", "build.sbt/", "target/", ".scalafmt.conf/"))
  val gitIgnore3 = GitIgnore(Seq("/.git", "/build.sbt", "/target", "/.scalafmt.conf"))
  val gitIgnore4 = GitIgnore(Seq("project/target", "target/streams"))
  val gitIgnore5 = GitIgnore(Seq("project/target/", "target/streams/"))
  val gitIgnore6 = GitIgnore(Seq("/project/target/", "/target/streams"))
  val gitIgnore7 = GitIgnore(Seq("project/plugins.sbt", "resources"))

  "GitIgnore" should {
    "process top directory: target" in {
      val path = Paths.get("target")
      gitIgnore1.isIgnored(path) shouldBe true
      gitIgnore2.isIgnored(path) shouldBe true
      gitIgnore3.isIgnored(path) shouldBe true
      gitIgnore4.isIgnored(path) shouldBe false
      gitIgnore5.isIgnored(path) shouldBe false
      gitIgnore6.isIgnored(path) shouldBe false

      GitIgnore(Seq.empty).isIgnored(path) shouldBe false
      GitIgnore("*").isIgnored(path) shouldBe true
      GitIgnore("*?").isIgnored(path) shouldBe true
      GitIgnore("?*").isIgnored(path) shouldBe true
      GitIgnore("?*?").isIgnored(path) shouldBe true
      GitIgnore("targ*").isIgnored(path) shouldBe true
      GitIgnore("*arget").isIgnored(path) shouldBe true
      GitIgnore("target*").isIgnored(path) shouldBe true
      GitIgnore("*target").isIgnored(path) shouldBe true
      GitIgnore("tar*get").isIgnored(path) shouldBe true
      GitIgnore("targ*/").isIgnored(path) shouldBe true
      GitIgnore("*arget/").isIgnored(path) shouldBe true
      GitIgnore("target*/").isIgnored(path) shouldBe true
      GitIgnore("*target/").isIgnored(path) shouldBe true
      GitIgnore("tar*get/").isIgnored(path) shouldBe true
      GitIgnore("/targ*").isIgnored(path) shouldBe true
      GitIgnore("/*arget").isIgnored(path) shouldBe true
      GitIgnore("/target*").isIgnored(path) shouldBe true
      GitIgnore("/*target").isIgnored(path) shouldBe true
      GitIgnore("/tar*get").isIgnored(path) shouldBe true
      GitIgnore("/targ*/").isIgnored(path) shouldBe true
      GitIgnore("/*arget/").isIgnored(path) shouldBe true
      GitIgnore("/target*/").isIgnored(path) shouldBe true
      GitIgnore("/*target/").isIgnored(path) shouldBe true
      GitIgnore("/tar*get/").isIgnored(path) shouldBe true

      GitIgnore("*rg.t").isIgnored(path) shouldBe false
      GitIgnore("*trget").isIgnored(path) shouldBe false
      GitIgnore("targets*").isIgnored(path) shouldBe false
      GitIgnore("ar*get").isIgnored(path) shouldBe false
      GitIgnore("*rg.t/").isIgnored(path) shouldBe false
      GitIgnore("*trget/").isIgnored(path) shouldBe false
      GitIgnore("targets*/").isIgnored(path) shouldBe false
      GitIgnore("ar*get/").isIgnored(path) shouldBe false
      GitIgnore("/*rg.t").isIgnored(path) shouldBe false
      GitIgnore("/*trget").isIgnored(path) shouldBe false
      GitIgnore("/targets*").isIgnored(path) shouldBe false
      GitIgnore("/ar*get").isIgnored(path) shouldBe false
      GitIgnore("/*rg.t/").isIgnored(path) shouldBe false
      GitIgnore("/*trget/").isIgnored(path) shouldBe false
      GitIgnore("/targets*/").isIgnored(path) shouldBe false
      GitIgnore("/ar*get/").isIgnored(path) shouldBe false

      GitIgnore("t?rget").isIgnored(path) shouldBe true
      GitIgnore("?arget").isIgnored(path) shouldBe true
      GitIgnore("targe?").isIgnored(path) shouldBe true
      GitIgnore("?arge?").isIgnored(path) shouldBe true
      GitIgnore("t??get").isIgnored(path) shouldBe true
      GitIgnore("t??ge?").isIgnored(path) shouldBe true
      GitIgnore("???ge?").isIgnored(path) shouldBe true
      GitIgnore("???g??").isIgnored(path) shouldBe true
      GitIgnore("/t?rget").isIgnored(path) shouldBe true
      GitIgnore("/?arget").isIgnored(path) shouldBe true
      GitIgnore("/targe?").isIgnored(path) shouldBe true
      GitIgnore("/?arge?").isIgnored(path) shouldBe true
      GitIgnore("/t??get").isIgnored(path) shouldBe true
      GitIgnore("/t??ge?").isIgnored(path) shouldBe true
      GitIgnore("/???ge?").isIgnored(path) shouldBe true
      GitIgnore("/???g??").isIgnored(path) shouldBe true
      GitIgnore("/t?rget/").isIgnored(path) shouldBe true
      GitIgnore("/?arget/").isIgnored(path) shouldBe true
      GitIgnore("/targe?/").isIgnored(path) shouldBe true
      GitIgnore("/?arge?/").isIgnored(path) shouldBe true
      GitIgnore("/t??get/").isIgnored(path) shouldBe true
      GitIgnore("/t??ge?/").isIgnored(path) shouldBe true
      GitIgnore("/???ge?/").isIgnored(path) shouldBe true
      GitIgnore("/???g??/").isIgnored(path) shouldBe true
      GitIgnore("t?rget/").isIgnored(path) shouldBe true
      GitIgnore("?arget/").isIgnored(path) shouldBe true
      GitIgnore("targe?/").isIgnored(path) shouldBe true
      GitIgnore("?arge?/").isIgnored(path) shouldBe true
      GitIgnore("t??get/").isIgnored(path) shouldBe true
      GitIgnore("t??ge?/").isIgnored(path) shouldBe true
      GitIgnore("???ge?/").isIgnored(path) shouldBe true
      GitIgnore("???g??/").isIgnored(path) shouldBe true

      GitIgnore("t?get").isIgnored(path) shouldBe false
      GitIgnore("?arge").isIgnored(path) shouldBe false
      GitIgnore("tage?").isIgnored(path) shouldBe false
      GitIgnore("?arge").isIgnored(path) shouldBe false
      GitIgnore("t?get").isIgnored(path) shouldBe false
      GitIgnore("t??ge").isIgnored(path) shouldBe false
      GitIgnore("??ge?").isIgnored(path) shouldBe false
      GitIgnore("??g?").isIgnored(path) shouldBe false
      GitIgnore("/t?get").isIgnored(path) shouldBe false
      GitIgnore("/?arge").isIgnored(path) shouldBe false
      GitIgnore("/tage?").isIgnored(path) shouldBe false
      GitIgnore("/?arge").isIgnored(path) shouldBe false
      GitIgnore("/t?get").isIgnored(path) shouldBe false
      GitIgnore("/t??ge").isIgnored(path) shouldBe false
      GitIgnore("/??ge?").isIgnored(path) shouldBe false
      GitIgnore("/??g?").isIgnored(path) shouldBe false
      GitIgnore("/t?get/").isIgnored(path) shouldBe false
      GitIgnore("/?arge/").isIgnored(path) shouldBe false
      GitIgnore("/tage?/").isIgnored(path) shouldBe false
      GitIgnore("/?arge/").isIgnored(path) shouldBe false
      GitIgnore("/t?get/").isIgnored(path) shouldBe false
      GitIgnore("/t??ge/").isIgnored(path) shouldBe false
      GitIgnore("/??ge?/").isIgnored(path) shouldBe false
      GitIgnore("/??g?/").isIgnored(path) shouldBe false

      GitIgnore("[t]arget").isIgnored(path) shouldBe true
      GitIgnore("t[a-z]rget").isIgnored(path) shouldBe true
      GitIgnore("t[!b-z]rget").isIgnored(path) shouldBe true

      GitIgnore("t*t").isIgnored(path) shouldBe true
      GitIgnore("t\\*t").isIgnored(path) shouldBe false
      GitIgnore("ta??et").isIgnored(path) shouldBe true
      GitIgnore("ta\\??et").isIgnored(path) shouldBe false

      GitIgnore("**/target").isIgnored(path) shouldBe true
      GitIgnore("**/target/").isIgnored(path) shouldBe true
      GitIgnore("**/*").isIgnored(path) shouldBe true
      GitIgnore("**/*/").isIgnored(path) shouldBe true

      GitIgnore("tar*").isIgnored(path) shouldBe true
      GitIgnore("!target").isIgnored(path) shouldBe false
      GitIgnore(Seq("tar*", "!target")).isIgnored(path) shouldBe false
      GitIgnore(Seq("!target", "tar*")).isIgnored(path) shouldBe true
      GitIgnore(Seq("tar*", "!tar*")).isIgnored(path) shouldBe false
      GitIgnore(Seq("tar*", "!*get")).isIgnored(path) shouldBe false
      GitIgnore(Seq("!*get", "tar*")).isIgnored(path) shouldBe true
      GitIgnore(Seq("*", "!*")).isIgnored(path) shouldBe false

      GitIgnore(Seq("tar*", "!?arget", "targe?")).isIgnored(path) shouldBe true
    }

    "process top hidden directory: .git" in {
      val path = Paths.get(".git")
      gitIgnore1.isIgnored(path) shouldBe true
      gitIgnore2.isIgnored(path) shouldBe true
      gitIgnore3.isIgnored(path) shouldBe true
      gitIgnore4.isIgnored(path) shouldBe false
      gitIgnore5.isIgnored(path) shouldBe false
      gitIgnore6.isIgnored(path) shouldBe false

      GitIgnore("*").isIgnored(path) shouldBe true
      GitIgnore("/*").isIgnored(path) shouldBe true
      GitIgnore("*/").isIgnored(path) shouldBe true
      GitIgnore("/*/").isIgnored(path) shouldBe true
      GitIgnore("????").isIgnored(path) shouldBe true
      GitIgnore("/????").isIgnored(path) shouldBe true
      GitIgnore("????/").isIgnored(path) shouldBe true
      GitIgnore("/????/").isIgnored(path) shouldBe true

      GitIgnore(".g*").isIgnored(path) shouldBe true
      GitIgnore(".*").isIgnored(path) shouldBe true
      GitIgnore("*g*t").isIgnored(path) shouldBe true
      GitIgnore("/*g*t").isIgnored(path) shouldBe true
      GitIgnore("*").isIgnored(path) shouldBe true
      GitIgnore("?git").isIgnored(path) shouldBe true
      GitIgnore("?gi?").isIgnored(path) shouldBe true
      GitIgnore("?g??").isIgnored(path) shouldBe true
      GitIgnore("?g*").isIgnored(path) shouldBe true
      GitIgnore("/?g??").isIgnored(path) shouldBe true
      GitIgnore("?g*/").isIgnored(path) shouldBe true

      GitIgnore("*git?").isIgnored(path) shouldBe false
      GitIgnore(".????").isIgnored(path) shouldBe false
      GitIgnore("??git").isIgnored(path) shouldBe false
      GitIgnore(".?gi?").isIgnored(path) shouldBe false

      GitIgnore("**/.git").isIgnored(path) shouldBe true
      GitIgnore("**/.git/").isIgnored(path) shouldBe true
      GitIgnore("/**/.git/").isIgnored(path) shouldBe false
      GitIgnore("/**/.git").isIgnored(path) shouldBe false
      GitIgnore("**/*git").isIgnored(path) shouldBe true
      GitIgnore("**/?git").isIgnored(path) shouldBe true
      GitIgnore("**/*").isIgnored(path) shouldBe true
    }

    "process top file: build.sbt" in {
      val path = Paths.get("build.sbt")
      gitIgnore1.isIgnored(path) shouldBe true
      gitIgnore2.isIgnored(path) shouldBe false
      gitIgnore3.isIgnored(path) shouldBe true
      gitIgnore4.isIgnored(path) shouldBe false
      gitIgnore5.isIgnored(path) shouldBe false
      gitIgnore6.isIgnored(path) shouldBe false

      GitIgnore("*").isIgnored(path) shouldBe true
      GitIgnore("/*").isIgnored(path) shouldBe true
      GitIgnore("*/").isIgnored(path) shouldBe false
      GitIgnore("/*/").isIgnored(path) shouldBe false
      GitIgnore("*.???").isIgnored(path) shouldBe true
      GitIgnore("/*.???").isIgnored(path) shouldBe true
      GitIgnore("*.???/").isIgnored(path) shouldBe false
      GitIgnore("/*.???/").isIgnored(path) shouldBe false
      GitIgnore("*.sbt").isIgnored(path) shouldBe true
      GitIgnore("/*.sbt").isIgnored(path) shouldBe true
      GitIgnore("*.sbt/").isIgnored(path) shouldBe false
      GitIgnore("/*.sbt/").isIgnored(path) shouldBe false
      GitIgnore("?????.sbt").isIgnored(path) shouldBe true
      GitIgnore("/?????.sbt").isIgnored(path) shouldBe true
      GitIgnore("?????.sbt/").isIgnored(path) shouldBe false
      GitIgnore("/?????.sbt/").isIgnored(path) shouldBe false

      GitIgnore("*.????").isIgnored(path) shouldBe false
      GitIgnore("/*.????").isIgnored(path) shouldBe false
      GitIgnore("*.????/").isIgnored(path) shouldBe false
      GitIgnore("/*.????/").isIgnored(path) shouldBe false

      GitIgnore("*.sbt").isIgnored(path) shouldBe true
      GitIgnore("build.*").isIgnored(path) shouldBe true
      GitIgnore("/*.sbt").isIgnored(path) shouldBe true
      GitIgnore("/build.*").isIgnored(path) shouldBe true

      GitIgnore("/*.sbt/").isIgnored(path) shouldBe false
      GitIgnore("/build.*/").isIgnored(path) shouldBe false
      GitIgnore("*.sbt/").isIgnored(path) shouldBe false
      GitIgnore("build.*/").isIgnored(path) shouldBe false

      GitIgnore("build*sbt").isIgnored(path) shouldBe true
      GitIgnore("build?sbt").isIgnored(path) shouldBe true
      GitIgnore("buil*bt").isIgnored(path) shouldBe true
      GitIgnore("buil???bt").isIgnored(path) shouldBe true
      GitIgnore("/build*sbt").isIgnored(path) shouldBe true
      GitIgnore("/build?sbt").isIgnored(path) shouldBe true
      GitIgnore("/buil*bt").isIgnored(path) shouldBe true
      GitIgnore("/buil???bt").isIgnored(path) shouldBe true
      GitIgnore("/build*sbt/").isIgnored(path) shouldBe false
      GitIgnore("/build?sbt/").isIgnored(path) shouldBe false
      GitIgnore("/buil*bt/").isIgnored(path) shouldBe false
      GitIgnore("/buil???bt/").isIgnored(path) shouldBe false
      GitIgnore("build*sbt/").isIgnored(path) shouldBe false
      GitIgnore("build?sbt/").isIgnored(path) shouldBe false
      GitIgnore("buil*bt/").isIgnored(path) shouldBe false
      GitIgnore("buil???bt/").isIgnored(path) shouldBe false

      GitIgnore("*.sbt").isIgnored(path) shouldBe true
      GitIgnore("!build.sbt").isIgnored(path) shouldBe false
      GitIgnore(Seq("*.sbt", "!build.sbt")).isIgnored(path) shouldBe false
    }

    "process top hidden file: .scalafmt.conf" in {
      val path = Paths.get(".scalafmt.conf")
      gitIgnore1.isIgnored(path) shouldBe true
      gitIgnore2.isIgnored(path) shouldBe false
      gitIgnore3.isIgnored(path) shouldBe true
      gitIgnore4.isIgnored(path) shouldBe false
      gitIgnore5.isIgnored(path) shouldBe false
      gitIgnore6.isIgnored(path) shouldBe false
    }

    "process nested directory: project/target" in {
      val path = Paths.get("project", "target")
      gitIgnore1.isIgnored(path) shouldBe true
      gitIgnore2.isIgnored(path) shouldBe true
      gitIgnore3.isIgnored(path) shouldBe false
      gitIgnore4.isIgnored(path) shouldBe true
      gitIgnore5.isIgnored(path) shouldBe true
      gitIgnore6.isIgnored(path) shouldBe true

      GitIgnore("*").isIgnored(path) shouldBe true
      GitIgnore("*?").isIgnored(path) shouldBe true
      GitIgnore("?*").isIgnored(path) shouldBe true
      GitIgnore("?*?").isIgnored(path) shouldBe true
      GitIgnore("targ*").isIgnored(path) shouldBe true
      GitIgnore("*arget").isIgnored(path) shouldBe true
      GitIgnore("target*").isIgnored(path) shouldBe true
      GitIgnore("*target").isIgnored(path) shouldBe true
      GitIgnore("tar*get").isIgnored(path) shouldBe true
      GitIgnore("targ*/").isIgnored(path) shouldBe true
      GitIgnore("*arget/").isIgnored(path) shouldBe true
      GitIgnore("target*/").isIgnored(path) shouldBe true
      GitIgnore("*target/").isIgnored(path) shouldBe true
      GitIgnore("tar*get/").isIgnored(path) shouldBe true
      GitIgnore("/targ*").isIgnored(path) shouldBe false
      GitIgnore("/*arget").isIgnored(path) shouldBe false
      GitIgnore("/target*").isIgnored(path) shouldBe false
      GitIgnore("/*target").isIgnored(path) shouldBe false
      GitIgnore("/tar*get").isIgnored(path) shouldBe false
      GitIgnore("/targ*/").isIgnored(path) shouldBe false
      GitIgnore("/*arget/").isIgnored(path) shouldBe false
      GitIgnore("/target*/").isIgnored(path) shouldBe false
      GitIgnore("/*target/").isIgnored(path) shouldBe false
      GitIgnore("/tar*get/").isIgnored(path) shouldBe false

      GitIgnore("*rg.t").isIgnored(path) shouldBe false
      GitIgnore("*trget").isIgnored(path) shouldBe false
      GitIgnore("targets*").isIgnored(path) shouldBe false
      GitIgnore("ar*get").isIgnored(path) shouldBe false
      GitIgnore("*rg.t/").isIgnored(path) shouldBe false
      GitIgnore("*trget/").isIgnored(path) shouldBe false
      GitIgnore("targets*/").isIgnored(path) shouldBe false
      GitIgnore("ar*get/").isIgnored(path) shouldBe false
      GitIgnore("/*rg.t").isIgnored(path) shouldBe false
      GitIgnore("/*trget").isIgnored(path) shouldBe false
      GitIgnore("/targets*").isIgnored(path) shouldBe false
      GitIgnore("/ar*get").isIgnored(path) shouldBe false
      GitIgnore("/*rg.t/").isIgnored(path) shouldBe false
      GitIgnore("/*trget/").isIgnored(path) shouldBe false
      GitIgnore("/targets*/").isIgnored(path) shouldBe false
      GitIgnore("/ar*get/").isIgnored(path) shouldBe false

      GitIgnore("project?target").isIgnored(path) shouldBe false
      GitIgnore("project*target").isIgnored(path) shouldBe false
      GitIgnore("projec*arget").isIgnored(path) shouldBe false
      GitIgnore("?roject/targe*").isIgnored(path) shouldBe true

      GitIgnore("t?rget").isIgnored(path) shouldBe true
      GitIgnore("?arget").isIgnored(path) shouldBe true
      GitIgnore("targe?").isIgnored(path) shouldBe true
      GitIgnore("?arge?").isIgnored(path) shouldBe true
      GitIgnore("t??get").isIgnored(path) shouldBe true
      GitIgnore("t??ge?").isIgnored(path) shouldBe true
      GitIgnore("???ge?").isIgnored(path) shouldBe true
      GitIgnore("???g??").isIgnored(path) shouldBe true
      GitIgnore("/t?rget").isIgnored(path) shouldBe false
      GitIgnore("/?arget").isIgnored(path) shouldBe false
      GitIgnore("/targe?").isIgnored(path) shouldBe false
      GitIgnore("/?arge?").isIgnored(path) shouldBe false
      GitIgnore("/t??get").isIgnored(path) shouldBe false
      GitIgnore("/t??ge?").isIgnored(path) shouldBe false
      GitIgnore("/???ge?").isIgnored(path) shouldBe false
      GitIgnore("/???g??").isIgnored(path) shouldBe false
      GitIgnore("/t?rget/").isIgnored(path) shouldBe false
      GitIgnore("/?arget/").isIgnored(path) shouldBe false
      GitIgnore("/targe?/").isIgnored(path) shouldBe false
      GitIgnore("/?arge?/").isIgnored(path) shouldBe false
      GitIgnore("/t??get/").isIgnored(path) shouldBe false
      GitIgnore("/t??ge?/").isIgnored(path) shouldBe false
      GitIgnore("/???ge?/").isIgnored(path) shouldBe false
      GitIgnore("/???g??/").isIgnored(path) shouldBe false
      GitIgnore("t?rget/").isIgnored(path) shouldBe true
      GitIgnore("?arget/").isIgnored(path) shouldBe true
      GitIgnore("targe?/").isIgnored(path) shouldBe true
      GitIgnore("?arge?/").isIgnored(path) shouldBe true
      GitIgnore("t??get/").isIgnored(path) shouldBe true
      GitIgnore("t??ge?/").isIgnored(path) shouldBe true
      GitIgnore("???ge?/").isIgnored(path) shouldBe true
      GitIgnore("???g??/").isIgnored(path) shouldBe true
    }

    "process nested file: project/plugins.sbt" in {
      val path = Paths.get("project", "plugins.sbt")
      gitIgnore1.isIgnored(path) shouldBe false
      gitIgnore2.isIgnored(path) shouldBe false
      gitIgnore3.isIgnored(path) shouldBe false
      gitIgnore4.isIgnored(path) shouldBe false
      gitIgnore5.isIgnored(path) shouldBe false
      gitIgnore6.isIgnored(path) shouldBe false
      gitIgnore7.isIgnored(path) shouldBe true
    }

    "process nested file: src/main/resources/application.conf" in {
      val path = Paths.get("src", "main", "resources", "application.conf")
      gitIgnore1.isIgnored(path, false) shouldBe false
      gitIgnore2.isIgnored(path, false) shouldBe false
      gitIgnore3.isIgnored(path, false) shouldBe false
      gitIgnore4.isIgnored(path, false) shouldBe false
      gitIgnore5.isIgnored(path, false) shouldBe false
      gitIgnore6.isIgnored(path, false) shouldBe false
      gitIgnore7.isIgnored(path, false) shouldBe true

      GitIgnore("*").isIgnored(path) shouldBe true
      GitIgnore("/*").isIgnored(path) shouldBe true
      GitIgnore("*/").isIgnored(path) shouldBe true
      GitIgnore("/*/").isIgnored(path) shouldBe true
      GitIgnore("*.????").isIgnored(path) shouldBe true
      GitIgnore("/*.????").isIgnored(path) shouldBe false
      GitIgnore("*.????/").isIgnored(path) shouldBe false
      GitIgnore("/*.????/").isIgnored(path) shouldBe false
      GitIgnore("*.conf").isIgnored(path) shouldBe true
      GitIgnore("/*.conf").isIgnored(path) shouldBe false
      GitIgnore("*.conf/").isIgnored(path) shouldBe false
      GitIgnore("/*.conf/").isIgnored(path) shouldBe false
      GitIgnore("applic?????.conf").isIgnored(path) shouldBe true
      GitIgnore("/applic?????.conf").isIgnored(path) shouldBe false
      GitIgnore("applic?????.conf/").isIgnored(path) shouldBe false
      GitIgnore("/applic?????.conf/").isIgnored(path) shouldBe false

      GitIgnore("*.???").isIgnored(path) shouldBe false
      GitIgnore("/*.???").isIgnored(path) shouldBe false
      GitIgnore("*.???/").isIgnored(path) shouldBe false
      GitIgnore("/*.???/").isIgnored(path) shouldBe false

      GitIgnore("application*conf").isIgnored(path) shouldBe true
      GitIgnore("application?conf").isIgnored(path) shouldBe true
      GitIgnore("applicati*nf").isIgnored(path) shouldBe true
      GitIgnore("applicati?????nf").isIgnored(path) shouldBe true
      GitIgnore("/application*conf").isIgnored(path) shouldBe false
      GitIgnore("/application?conf").isIgnored(path) shouldBe false
      GitIgnore("/applicati*nf").isIgnored(path) shouldBe false
      GitIgnore("/applicati?????nf").isIgnored(path) shouldBe false
      GitIgnore("/application*conf/").isIgnored(path) shouldBe false
      GitIgnore("/application?conf/").isIgnored(path) shouldBe false
      GitIgnore("/applicati*nf/").isIgnored(path) shouldBe false
      GitIgnore("/applicati?????nf/").isIgnored(path) shouldBe false
      GitIgnore("application*conf/").isIgnored(path) shouldBe false
      GitIgnore("application?conf/").isIgnored(path) shouldBe false
      GitIgnore("applicati*nf/").isIgnored(path) shouldBe false
      GitIgnore("applicati?????nf/").isIgnored(path) shouldBe false

      GitIgnore("**/main").isIgnored(path) shouldBe true
      GitIgnore("**/main/resources/").isIgnored(path) shouldBe true
      GitIgnore("**/resources").isIgnored(path) shouldBe true
      GitIgnore("**/application.conf").isIgnored(path) shouldBe true
      GitIgnore("**/*.conf").isIgnored(path) shouldBe true
      GitIgnore("**/application.*").isIgnored(path) shouldBe true
      GitIgnore("**/*.conf").isIgnored(path) shouldBe true

      GitIgnore("**/test").isIgnored(path) shouldBe false
      GitIgnore("**/test/resources/").isIgnored(path) shouldBe false
      GitIgnore("**/scala").isIgnored(path) shouldBe false
      GitIgnore("**/resources.conf").isIgnored(path) shouldBe false
      GitIgnore("**/resources.*").isIgnored(path) shouldBe false
      GitIgnore("**/*.yaml").isIgnored(path) shouldBe false

      GitIgnore("src/**").isIgnored(path) shouldBe true
      GitIgnore("src/main/**").isIgnored(path) shouldBe true
      GitIgnore("src/main/resources/**").isIgnored(path) shouldBe true
      GitIgnore("src/test/**").isIgnored(path) shouldBe false
      GitIgnore("src/main/scala/**").isIgnored(path) shouldBe false

      GitIgnore("src/**/application.conf").isIgnored(path) shouldBe true
      GitIgnore("src/*/application.conf").isIgnored(path) shouldBe false
      GitIgnore("src/*/*/application.conf").isIgnored(path) shouldBe true
      GitIgnore("src/*/*/*.conf").isIgnored(path) shouldBe true
      GitIgnore("*/*/*/*.conf").isIgnored(path) shouldBe true
      GitIgnore("src/**/resources").isIgnored(path) shouldBe true
      GitIgnore("src/**/resources/application.conf").isIgnored(path) shouldBe true
      GitIgnore("main/**/*.conf").isIgnored(path) shouldBe false
      GitIgnore("src/main/**/*.conf").isIgnored(path) shouldBe true
      GitIgnore("test/**/*.conf").isIgnored(path) shouldBe false
      GitIgnore("src/test/**/*.conf").isIgnored(path) shouldBe false

      GitIgnore(Seq("*.conf", "!application.conf")).isIgnored(path) shouldBe false
      GitIgnore(Seq("*.conf", "!/src/**/application.conf")).isIgnored(path) shouldBe false
      GitIgnore(Seq("*.conf", "!**/application.conf")).isIgnored(path) shouldBe false
      GitIgnore(Seq("!application.conf", "*.conf")).isIgnored(path) shouldBe true
      GitIgnore(Seq("!/src/**/application.conf", "*.conf")).isIgnored(path) shouldBe true
      GitIgnore(Seq("!**/application.conf", "*.conf")).isIgnored(path) shouldBe true

      GitIgnore(Seq("!**/application.conf", "*.conf")).isIgnored(path) shouldBe true
      GitIgnore(Seq("src/main", "!*.conf")).isIgnored(path) shouldBe true
      GitIgnore(Seq("src/", "!*.conf")).isIgnored(path) shouldBe true
      GitIgnore(Seq("/src", "!*.conf")).isIgnored(path) shouldBe true
      GitIgnore(Seq("/src/", "!*.conf")).isIgnored(path) shouldBe true
      GitIgnore(Seq("src", "!src/main/resources")).isIgnored(path) shouldBe true
      GitIgnore(Seq("/src", "!src/main/resources")).isIgnored(path) shouldBe true
      GitIgnore(Seq("src/", "!src/main/resources")).isIgnored(path) shouldBe true
      GitIgnore(Seq("/src/", "!src/main/resources")).isIgnored(path) shouldBe true
      GitIgnore(Seq("src", "!src/main/*")).isIgnored(path) shouldBe true
      GitIgnore(Seq("/src", "!src/main/*")).isIgnored(path) shouldBe true
      GitIgnore(Seq("src/", "!src/main/*")).isIgnored(path) shouldBe true
      GitIgnore(Seq("/src/", "!src/main/*")).isIgnored(path) shouldBe true
      GitIgnore(Seq("src", "!*")).isIgnored(path) shouldBe false
      GitIgnore(Seq("/src", "!*")).isIgnored(path) shouldBe false
      GitIgnore(Seq("src/", "!*")).isIgnored(path) shouldBe false
      GitIgnore(Seq("/src/", "!*")).isIgnored(path) shouldBe false
      GitIgnore(Seq("src", "!**/")).isIgnored(path) shouldBe true
      GitIgnore(Seq("/src", "!**/")).isIgnored(path) shouldBe true
      GitIgnore(Seq("src/", "!**/")).isIgnored(path) shouldBe true
      GitIgnore(Seq("/src/", "!**/")).isIgnored(path) shouldBe true
      GitIgnore(Seq("src", "!**/*")).isIgnored(path) shouldBe true
      GitIgnore(Seq("/src", "!**/*")).isIgnored(path) shouldBe true
      GitIgnore(Seq("src/", "!**/*")).isIgnored(path) shouldBe true
      GitIgnore(Seq("/src/", "!**/*")).isIgnored(path) shouldBe true
      GitIgnore(Seq("/src/", "!/src/")).isIgnored(path) shouldBe false
      GitIgnore(Seq("/src/", "!src/")).isIgnored(path) shouldBe false
      GitIgnore(Seq("/src/", "!/src")).isIgnored(path) shouldBe false
      GitIgnore(Seq("/src/", "!src")).isIgnored(path) shouldBe false
      GitIgnore(Seq("/src/", "!src", "/src/main")).isIgnored(path) shouldBe true
      GitIgnore(Seq("/src/", "!src", "**/main")).isIgnored(path) shouldBe true
      GitIgnore(Seq("/src/", "!src", "**/main", "!main")).isIgnored(path) shouldBe false
      GitIgnore(Seq("**/", "!main")).isIgnored(path) shouldBe false
      GitIgnore(Seq("**/*", "!main")).isIgnored(path) shouldBe false
      GitIgnore(Seq("/src/", "!src", "**/*", "!main")).isIgnored(path) shouldBe false
      GitIgnore(Seq("*/", "!main")).isIgnored(path) shouldBe true
      GitIgnore(Seq("*/*", "!main")).isIgnored(path) shouldBe false
      GitIgnore(Seq("/src/", "!src", "*/*", "!main")).isIgnored(path) shouldBe false
    }

    "process nested file: foo/bar/baz/[*?].txt" in {
      val path = Paths.get("foo", "bar", "baz", "[*?].txt")
      gitIgnore1.isIgnored(path, false) shouldBe false
      gitIgnore2.isIgnored(path, false) shouldBe false
      gitIgnore3.isIgnored(path, false) shouldBe false
      gitIgnore4.isIgnored(path, false) shouldBe false
      gitIgnore5.isIgnored(path, false) shouldBe false
      gitIgnore6.isIgnored(path, false) shouldBe false
      gitIgnore7.isIgnored(path, false) shouldBe false

      GitIgnore("*.txt").isIgnored(path) shouldBe true
      GitIgnore("\\[\\*\\?].txt").isIgnored(path) shouldBe true
      GitIgnore("\\[??].txt").isIgnored(path) shouldBe true
      GitIgnore("\\[\\??].txt").isIgnored(path) shouldBe false
      GitIgnore("\\[*\\?].txt").isIgnored(path) shouldBe true
      GitIgnore("\\[*].txt").isIgnored(path) shouldBe true
      GitIgnore("[*].txt").isIgnored(path) shouldBe false

      GitIgnore("**/\\[\\*\\?].txt").isIgnored(path) shouldBe true
      GitIgnore("foo/**/\\[\\*\\?].txt").isIgnored(path) shouldBe true
      GitIgnore("foo/bar/**/\\[\\*\\?].txt").isIgnored(path) shouldBe true
      GitIgnore("foo/**/baz/\\[\\*\\?].txt").isIgnored(path) shouldBe true
      GitIgnore("foo/**/baz/**").isIgnored(path) shouldBe true
      GitIgnore("foo/*/baz/*").isIgnored(path) shouldBe true
      GitIgnore("*/*/baz/*").isIgnored(path) shouldBe true
      GitIgnore("*/*/*/*").isIgnored(path) shouldBe true
      GitIgnore("foo/*/*").isIgnored(path) shouldBe true
      GitIgnore("**/foo/**").isIgnored(path) shouldBe true
      GitIgnore("**/bar/**").isIgnored(path) shouldBe true
      GitIgnore("**/baz/**").isIgnored(path) shouldBe true

      GitIgnore("**/abc/**").isIgnored(path) shouldBe false
      GitIgnore("**/abc").isIgnored(path) shouldBe false
      GitIgnore("abc/**").isIgnored(path) shouldBe false

      GitIgnore
        .parse("""
          |#foo   
          |\[\*\?].txt   
          |""".stripMargin)
        .isIgnored(path) shouldBe true

      GitIgnore
        .parse("""
          |#foo   
          |\[\*\?].txt\   
          |""".stripMargin)
        .isIgnored(path) shouldBe false

      GitIgnore
        .parse("""
          |#foo   
          |
          |#bar
          |
          |#baz
          |    
          |""".stripMargin)
        .isIgnored(path) shouldBe false

      GitIgnore
        .parse("""
          |#foo   
          |
          |#bar
          |\ 
          |#baz
          |    
          |""".stripMargin)
        .isIgnored(path) shouldBe false

      GitIgnore
        .parse("""
          |#foo   
          |
          |#bar
          |\[\*\?].* 
          |#baz
          |    
          |""".stripMargin)
        .isIgnored(path) shouldBe true
    }
  }

}
