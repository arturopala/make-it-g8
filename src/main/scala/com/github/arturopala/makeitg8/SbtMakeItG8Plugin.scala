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

import java.io.{File, _}

import sbt.Keys._
import sbt.{settingKey, _}

import better.files.{File => BetterFile, _}

object SbtMakeItG8Plugin extends AutoPlugin {

  override def requires = sbt.plugins.JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  trait Keys {
    val makeItG8SourceFolder = settingKey[File]("Source code path to make template from")
    val makeItG8TargetFolder = settingKey[File]("Template target path")
    val makeItG8IgnoredPaths = settingKey[List[String]]("Source path prefixes to ignore")
    val makeItG8TemplateName = settingKey[String]("Template name")
    val makeItG8TemplateDescription = settingKey[String]("Template description")
    val makeItG8CustomReadmeHeaderPath = settingKey[Option[String]]("Custom README.md header path")
    val makeItG8PackageName = settingKey[String]("Source code package name to parametrize")
    val makeItG8KeywordValueMap = settingKey[Map[String, String]]("Text chunks to parametrize by key word")
    val makeItG8BuildTemplateSource = settingKey[String]("Template project build resources root")
    val makeItG8BuildTemplateResources =
      settingKey[List[String]]("List of template file resources relative to the root")
    val makeItG8ScriptTestTarget = settingKey[String]("Where to create temp project for tests, e.g. target/sandbox")
    val makeItG8ScriptTestCommand = settingKey[String]("Command to test generated project, e.g. sbt test")
    val makeItG8CreateReadme = settingKey[Boolean]("Generate template README")
    val makeItG8ScriptBeforeTest =
      settingKey[List[String]]("Commands run to initialize project before the tests, e.g. git init")
    val makeItG8Task = taskKey[File]("Create a g8 template from source code")
  }

  object Keys extends Keys

  object autoImport extends Keys {

    lazy val defaultMakeItG8Settings: Seq[Def.Setting[_]] = Seq(
      makeItG8SourceFolder := baseDirectory.value / "target" / "sandbox" / "example-project-from-template",
      makeItG8TargetFolder := baseDirectory.value / "src" / "main" / "g8",
      makeItG8IgnoredPaths := List(
        ".git",
        "target",
        ".idea",
        "project/target",
        "project/project",
        "logs",
        "make-it-g8"),
      makeItG8TemplateName := name.value.toLowerCase.replaceAllLiterally("-", "") + ".g8",
      makeItG8PackageName := organization.value + "." + name.value.toLowerCase.replaceAllLiterally("-", ""),
      makeItG8KeywordValueMap := Map("description" -> description.value),
      makeItG8ScriptBeforeTest := List.empty,
      makeItG8Task := {
        val config = MakeItG8Config(
          BetterFile(makeItG8SourceFolder.value.toPath),
          BetterFile(makeItG8TargetFolder.value.toPath),
          makeItG8IgnoredPaths.value,
          makeItG8TemplateName.value,
          makeItG8PackageName.value,
          makeItG8KeywordValueMap.value,
          "g8-build-template",
          Nil,
          makeItG8ScriptTestTarget.value,
          makeItG8ScriptTestCommand.value,
          makeItG8ScriptBeforeTest.value,
          clearTargetFolder = false,
          makeItG8CreateReadme.value,
          makeItG8TemplateDescription.value,
          makeItG8CustomReadmeHeaderPath.value
        )
        MakeItG8Creator.createG8Template(config)
        makeItG8TargetFolder.value / "src" / "main" / "g8"
      }
    )
  }

}
