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

class FileTreeSpec extends AnyWordSpec with Matchers {

  "FileTree" should {
    "compute a tree" in {
      FileTree.compute(Seq(Paths.get("test.scala")).iterator) shouldBe List(0 -> "test.scala")
      FileTree.compute(Seq(Paths.get("/test", "test.scala")).iterator) shouldBe List(0 -> "test", 1 -> "test.scala")
      FileTree.compute(Seq(Paths.get("/test"), Paths.get("/test", "test.scala")).iterator) should contain
        .theSameElementsInOrderAs(List(0 -> "test", 1 -> "test.scala"))
      FileTree.compute(Seq(Paths.get("/test", "test.scala"), Paths.get("/test")).iterator) should contain
        .theSameElementsInOrderAs(List(0 -> "test", 1 -> "test.scala"))
      FileTree.compute(Seq(Paths.get("/test", "test.scala"), Paths.get("/test")).iterator) should contain
        .theSameElementsInOrderAs(List(0 -> "test", 1 -> "test.scala"))
      FileTree.compute(Seq(Paths.get("/test"), Paths.get("/test", "test.scala")).iterator) should contain
        .theSameElementsInOrderAs(List(0 -> "test", 1 -> "test.scala"))
      FileTree.compute(
        Seq(Paths.get("/test"), Paths.get("/test", "foo", "bar.txt"), Paths.get("/test", "test.scala")).iterator
      ) should contain
        .theSameElementsInOrderAs(List(0 -> "test", 1 -> "foo", 2 -> "bar.txt", 1 -> "test.scala"))
      FileTree.compute(
        Seq(
          Paths.get("/test"),
          Paths.get("/test", "foo", "bar.txt"),
          Paths.get("foo.bar"),
          Paths.get("/test", "test.scala")
        ).iterator
      ) should contain
        .theSameElementsInOrderAs(List(0 -> "foo.bar", 0 -> "test", 1 -> "foo", 2 -> "bar.txt", 1 -> "test.scala"))
      FileTree.compute(
        Seq(
          Paths.get("/test"),
          Paths.get("/test", "foo", "bar.txt"),
          Paths.get("/bar", "foo.bar"),
          Paths.get("/test", "test.scala")
        ).iterator
      ) should contain
        .theSameElementsInOrderAs(
          List(0 -> "bar", 1 -> "foo.bar", 0 -> "test", 1 -> "foo", 2 -> "bar.txt", 1 -> "test.scala")
        )
    }

    "draw a tree 1" in {
      val pathTree = FileTree.compute(
        Seq(
          Paths.get("/test"),
          Paths.get("/test", "foo", "bar.txt"),
          Paths.get("foo.bar"),
          Paths.get("/test", "test.scala")
        ).iterator
      )
      FileTree.draw(pathTree) shouldBe
        """├── foo.bar
          |└── test
          |    ├── foo
          |    │   └── bar.txt
          |    │
          |    └── test.scala""".stripMargin
    }

    "draw a tree 2" in {
      val pathTree = FileTree.compute(
        Seq(
          Paths.get("/test"),
          Paths.get("/test", "foo", "bar.txt"),
          Paths.get("/bar", "foo.bar"),
          Paths.get("/test", "test.scala")
        ).iterator
      )
      FileTree.draw(pathTree) shouldBe
        """├── bar
          |│   └── foo.bar
          |│
          |└── test
          |    ├── foo
          |    │   └── bar.txt
          |    │
          |    └── test.scala""".stripMargin
    }

    "draw a tree 3" in {
      val pathTree = FileTree.compute(Seq(Paths.get("/test")).iterator)
      FileTree.draw(pathTree) shouldBe
        """└── test""".stripMargin
    }

    "draw a tree 4" in {
      val pathTree = FileTree.compute(
        Seq(
          Paths.get("zoo.scala"),
          Paths.get("/foo", "zoo", "bar.txt"),
          Paths.get("/bar", "foo", "foo.bar"),
          Paths.get("/zoo", "foo", "bar", "zoo.scala")
        ).iterator
      )
      FileTree.draw(pathTree) shouldBe
        """├── bar
          |│   └── foo
          |│       └── foo.bar
          |│
          |├── foo
          |│   └── zoo
          |│       └── bar.txt
          |│
          |├── zoo
          |│   └── foo
          |│       └── bar
          |│           └── zoo.scala
          |│
          |└── zoo.scala""".stripMargin
    }
  }

}
