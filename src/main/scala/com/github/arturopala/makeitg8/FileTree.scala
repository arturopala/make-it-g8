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

import java.nio.file.{Path, Paths}

import scala.annotation.tailrec

trait FileTree {

  type Node = (Int, String)
  type Tree = List[Node]

  val middleNode = "├── "
  val endNode = "└── "
  val link = "│   "
  val space = " " * link.length

  private val root = Paths.get("/")

  def sort(paths: Iterator[Path]): Seq[Path] =
    paths.toSeq.sortWith((pl, pr) => comparePaths(pl, pr, 0))

  def compute(paths: Iterator[Path]): Tree = {

    def leafs(prefix: Path, p2: Path): Tree =
      (0 until p2.getNameCount).toList
        .map(i => (i + prefix.getNameCount, p2.getName(i).toString))

    sort(paths)
      .foldLeft((List.empty[Node], Option.empty[Path])) {
        case ((tree, prevPathOpt), path) =>
          (
            prevPathOpt
              .map { prevPath =>
                val (prefix, _, outstandingPath) = commonPrefix(root, prevPath, path)
                tree ::: leafs(prefix, outstandingPath)
              }
              .getOrElse(leafs(root, path)),
            Some(path))
      }
      ._1
  }

  def draw(pathsTree: Tree): String = {

    def drawLine(node: String, label: String, marks: List[Int]): (String, List[Int]) =
      ((0 until marks.max).map(i => if (marks.contains(i)) link else space).mkString + node + label, marks)

    def draw2(label: String, ls: (List[Int], String)): (String, List[Int]) =
      ((0 until ls._1.max).map(i => if (ls._1.contains(i)) link else space).mkString + ls._2 + label, ls._1)

    def append(lineWithMarks: (String, List[Int]), result: String): (String, List[Int]) =
      (trimRight(lineWithMarks._1) + "\n" + result, lineWithMarks._2)

    pathsTree.reverse
      .foldLeft(("", List.empty[Int])) {
        case ((result, marks), (offset, label)) =>
          marks match {
            case Nil => drawLine(endNode, label, offset :: Nil)
            case head :: tail =>
              append(
                if (offset == head) drawLine(middleNode, label, marks)
                else if (offset < head) draw2(label, tail match {
                  case Nil                   => (offset :: Nil, endNode)
                  case x :: _ if x == offset => (tail, middleNode)
                  case _                     => (offset :: tail, endNode)
                })
                else {
                  val l1 = drawLine(endNode, label, offset :: marks)
                  val l2 = drawLine(space, "", offset :: marks)
                  (l1._1 + "\n" + l2._1, l1._2)
                },
                result
              )
          }
      }
      ._1
  }

  @tailrec
  final def comparePaths(path1: Path, path2: Path, i: Int): Boolean = {
    val c = path1.getName(i).toString.compareToIgnoreCase(path2.getName(i).toString)
    val pc1 = path1.getNameCount
    val pc2 = path2.getNameCount
    if (pc1 - 1 == i || pc2 - 1 == i) {
      if (c != 0) c < 0 else pc1 < pc2
    } else {
      if (c != 0) c < 0 else comparePaths(path1, path2, i + 1)
    }
  }

  @tailrec
  final def commonPrefix(prefix: Path, path1: Path, path2: Path): (Path, Path, Path) =
    if (path1.getNameCount > 0 && path2.getNameCount > 0) {
      if (path1.getName(0) != path2.getName(0)) (prefix, path1, path2)
      else
        commonPrefix(
          prefix.resolve(path1.subpath(0, 1)),
          if (path1.getNameCount == 1) path1 else path1.subpath(1, path1.getNameCount),
          if (path2.getNameCount == 1) path2 else path2.subpath(1, path2.getNameCount)
        )
    } else (prefix, path1, path2)

  def trimRight(string: String): String = string.reverse.dropWhile(_ == ' ').reverse
}

object FileTree extends FileTree
