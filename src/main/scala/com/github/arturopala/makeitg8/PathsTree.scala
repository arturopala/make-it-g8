package com.github.arturopala.makeitg8

import java.nio.file.{Path, Paths}

import scala.annotation.tailrec

object PathsTree {

  def compute(paths: Iterator[Path]): List[(Int, String)] = {
    val pathsSorted = paths.toSeq.sortWith((pl, pr) => comparePaths(pl, pr, 0))
    pathsSorted
      .foldLeft((List.empty[(Int, String)], Option.empty[Path])) {
        case ((list, prevOpt), path) =>
          (
            prevOpt
              .map { prev =>
                val (prefix, _, p2) = commonPrefix(Paths.get("/"), prev, path)
                list ::: (0 until p2.getNameCount).toList
                  .map(i => (i + prefix.getNameCount, p2.getName(i).toString))
              }
              .getOrElse((0, path.toString) :: list),
            Some(path))
      }
      ._1
  }

  def draw(pathsTree: List[(Int, String)]): String = {

    def draw(node: String, n: String, l: List[Int]): (String, List[Int]) =
      ((0 until l.max).map(i => if (l.contains(i)) "│  " else "   ").mkString + node + n, l)

    def draw2(n: String, ls: (List[Int], String)): (String, List[Int]) =
      ((0 until ls._1.max).map(i => if (ls._1.contains(i)) "│  " else "   ").mkString + ls._2 + n, ls._1)

    pathsTree.reverse
      .foldLeft(("", List.empty[Int])) {
        case ((s, l), (p, n)) =>
          val (s1, l1) = l match {
            case Nil => draw("└─ ", n, p :: Nil)
            case head :: tail =>
              if (p == head) draw("├─ ", n, l)
              else if (p < head) draw2(n, tail match {
                case Nil              => (p :: Nil, "└─ ")
                case x :: _ if x == p => (tail, "├─ ")
                case _                => (p :: tail, "└─ ")
              })
              else {
                val l1 = draw("└─ ", n, p :: l)
                val l2 = draw("   ", "", p :: l)
                (l1._1 + "\n\t" + l2._1, l1._2)
              }
          }
          (s1 + "\n\t" + s, l1)
      }
      ._1
  }

  @tailrec
  private def comparePaths(path1: Path, path2: Path, i: Int): Boolean =
    if (path1.getNameCount - 1 == i || path2.getNameCount - 1 == i) {
      path1.getName(i).toString.compareToIgnoreCase(path2.getName(i).toString) < 0
    } else {
      val c = path1.getName(i).toString.compareToIgnoreCase(path2.getName(i).toString)
      if (c != 0) c < 0 else comparePaths(path1, path2, i + 1)
    }

  @tailrec
  private def commonPrefix(prefix: Path, path1: Path, path2: Path): (Path, Path, Path) =
    if (path1.getNameCount > 0 && path2.getNameCount > 0) {
      if (path1.getName(0) != path2.getName(0)) (prefix, path1, path2)
      else
        commonPrefix(
          prefix.resolve(path1.subpath(0, 1)),
          path1.subpath(1, path1.getNameCount),
          path2.subpath(1, path2.getNameCount)
        )
    } else (prefix, path1, path2)
}
