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

class GitUtilsSpec extends AnyWordSpec with Matchers {

  "GitUtils" should {
    "parse github URL" in {
      GitUtils.parseGithubUrl("") shouldBe None
      GitUtils.parseGithubUrl("https://github.com") shouldBe None
      GitUtils.parseGithubUrl("https://github.com/arturopala") shouldBe None
      GitUtils.parseGithubUrl("git@github.com") shouldBe None
      GitUtils.parseGithubUrl("git@github.com:arturopala") shouldBe None
      GitUtils.parseGithubUrl("https://github.com/arturopala/make-it-g8.git") shouldBe Some(
        ("arturopala", "make-it-g8")
      )
      GitUtils.parseGithubUrl("git@github.com:arturopala/buffer-and-slice.git") shouldBe Some(
        ("arturopala", "buffer-and-slice")
      )
      GitUtils.parseGithubUrl("https://github.com/artur.opala999/make-it-g8.git") shouldBe Some(
        ("artur.opala999", "make-it-g8")
      )
      GitUtils.parseGithubUrl("git@github.com:artur-999-opala/buffer-and-slice.git") shouldBe Some(
        ("artur-999-opala", "buffer-and-slice")
      )
    }
  }

}
