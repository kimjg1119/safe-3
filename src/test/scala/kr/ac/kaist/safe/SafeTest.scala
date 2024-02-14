/**
 * *****************************************************************************
 * Copyright (c) 2016-2019, KAIST.
 * All rights reserved.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 * ****************************************************************************
 */

package kr.ac.kaist.safe

import java.io._
import kr.ac.kaist.safe.phase._
import kr.ac.kaist.safe.util._
import org.scalatest.funsuite.AnyFunSuite
import scala.io.Source

abstract class SafeTest extends AnyFunSuite {
  // tests directory
  lazy val testDir = BASE_DIR + SEP + "tests" + SEP

  // safe configuration
  lazy val safeConfig: SafeConfig = SafeConfig(
    command = CmdBase,
    fileNames = Nil,
    testMode = true
  )

  ////////////////////////////////////////////////////////////////////////////////
  // Helper Functions
  ////////////////////////////////////////////////////////////////////////////////
  // filename filters
  def extFilter(ext: String): String => Boolean = _.endsWith(s".$ext")
  lazy val jsFilter: String => Boolean = extFilter("js")
  lazy val htmlFilter: String => Boolean = extFilter("html")
  lazy val errFilter: String => Boolean = extFilter("err")

  // normalization
  def norm(s: String): String = s.replaceAll("\\s+", "").replaceAll("\\n+", "")

  // read a file
  def readFile(filename: String): String = {
    assert(new File(filename).exists)
    norm(Source.fromFile(filename).getLines().mkString(LINE_SEP))
  }

  // walk file tree
  def walkTree(file: File): Iterable[File] = {
    val children = new Iterable[File] {
      def iterator: Iterator[File] = if (file.isDirectory) file.listFiles.iterator else Iterator.empty
    }
    Seq(file) ++: children.flatMap(walkTree(_))
  }

  // change extension
  def changeExt(from: String, to: String): String => String =
    filename => filename.substring(0, filename.length - from.length) + to
}
