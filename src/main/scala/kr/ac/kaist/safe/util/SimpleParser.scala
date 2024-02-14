/**
 * *****************************************************************************
 * Copyright (c) 2016-2018, KAIST.
 * All rights reserved.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 * ****************************************************************************
 */

package kr.ac.kaist.safe.util

import scala.util.parsing.combinator._
import scala.util.matching.Regex

trait SimpleParser extends RegexParsers {
  lazy val any: Regex = ".+".r
  lazy val nat: Parser[Int] = "[0-9]+".r ^^ { n => n.toInt }
  lazy val num: Parser[Int] = "-?[0-9]+".r ^^ { n => n.toInt }
  lazy val alpha: Regex = "[a-zA-Z]+".r
  lazy val alphaNum: Regex = "[0-9a-zA-Z]+".r
}
