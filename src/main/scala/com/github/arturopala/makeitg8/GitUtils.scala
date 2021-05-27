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

import scala.sys.process
import scala.util.Try
import java.io.File

object GitUtils {

  val githubUrlRegex = """(?:https://|git@)github.com(?:/|:)([^/]+)/([^/]+)\.git""".r

  def parseGithubUrl(url: String): Option[(String, String)] =
    url match {
      case githubUrlRegex(b, c) => Some((b, c))
      case _                    => None
    }

  def remoteGithubUser(folder: File): Option[String] =
    Try(
      process
        .Process("git config --get remote.origin.url", folder)
        .lineStream
        .headOption
        .flatMap(parseGithubUrl)
        .map(_._1)
    ).toOption.flatten

  def currentBranch(folder: File): Option[String] =
    Try(
      process
        .Process("git branch --show-current", folder)
        .lineStream
        .headOption
    ).toOption.flatten

}
