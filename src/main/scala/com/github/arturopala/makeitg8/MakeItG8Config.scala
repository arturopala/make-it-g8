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

import better.files.File

case class MakeItG8Config(
  sourceFolder: File,
  targetFolder: File,
  ignoredPaths: List[String],
  templateName: String,
  packageName: String,
  keywordValueMap: Map[String, String],
  g8BuildTemplateSource: String,
  g8BuildTemplateResources: List[String],
  scriptTestTarget: String,
  scriptTestCommand: String,
  createBuildFiles: Boolean,
  templateDescription: String)
