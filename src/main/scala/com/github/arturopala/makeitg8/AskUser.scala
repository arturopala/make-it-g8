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

import scala.annotation.tailrec
import scala.io.StdIn

trait AskUser {

  import EscapeCodes._

  final def askYesNo(prompt: String): Boolean =
    ask[Boolean](
      prompt,
      s =>
        if (s.toLowerCase == "yes" || s.toLowerCase == "y") {
          print(CLEAR_PREVIOUS_LINE)
          Some(true)
        }
        else if (s.toLowerCase == "no" || s.toLowerCase == "n") {
          print(CLEAR_PREVIOUS_LINE)
          Some(false)
        }
        else {
          print(CLEAR_PREVIOUS_LINE)
          None
        }
    )

  @tailrec
  final def askString(prompt: String, defaultValue: Option[String] = None): String = {
    val value = askOptional(prompt).orElse(defaultValue)
    if (value.isDefined) value.get
    else {
      print(CLEAR_PREVIOUS_LINE)
      askString(prompt, defaultValue)
    }
  }

  @tailrec
  final def ask[T](prompt: String, parse: String => Option[T], defaultValue: Option[T] = None): T = {
    val value = askOptional(prompt).flatMap(parse).orElse(defaultValue)
    if (value.isDefined) value.get
    else {
      print(CLEAR_PREVIOUS_LINE)
      ask[T](prompt, parse, defaultValue)
    }
  }

  final def askOptional(prompt: String): Option[String] =
    Option(StdIn.readLine(prompt))
      .map(_.trim)
      .flatMap(s => if (s.isEmpty) None else Some(s))
}
