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

package kr.ac.kaist.safe.analyzer.domain

import kr.ac.kaist.safe.analyzer._
import kr.ac.kaist.safe.errors.error._
import kr.ac.kaist.safe.nodes.cfg.{ CFG, Call }
import kr.ac.kaist.safe.util.PipeOps._
import kr.ac.kaist.safe.util._
import scala.util.parsing.combinator._
import scala.util.{ Try, Success, Failure }
import scala.util.matching.Regex

// concrete location type
abstract class Loc extends Value {
  def isUser: Boolean = this match {
    case Recency(loc, _) => loc.isUser
    case TraceSensLoc(loc, _) => loc.isUser
    case UserAllocSite(_) => true
    case PredAllocSite(_) => false
  }
}
object Loc {
  def parse(str: String, cfgIn: CFG): Try[Loc] = (new LocParser { val cfg = cfgIn })(str)
  def apply(str: String): Loc = apply(PredAllocSite(str), Sensitivity.initTP)
  def apply(asite: AllocSite, tp: TracePartition): Loc = {
    asite |>
      condApply[Loc](HeapClone, TraceSensLoc(_, tp)) |>
      condApply[Loc](RecencyMode, Recency(_, Recent))
  }

  implicit def ordering[B <: Loc]: Ordering[B] = Ordering.by({
    case addrPart => addrPart.toString
  })
}

// location parser
trait LocParser extends TracePartitionParser {
  // allocation site abstraction
  lazy val userASite: Parser[UserAllocSite] = "#" ~> nat ^^ { id => UserAllocSite(id) }
  lazy val predName: Regex = "[0-9a-zA-Z-.<>\\[\\]]+".r
  lazy val predASite: Parser[PredAllocSite] = "#" ~> predName ^^ { name => PredAllocSite(name) }
  lazy val allocSite: Parser[AllocSite] = userASite | predASite

  // trace sensitive address abstraction
  def heapClone(parser: Parser[Loc]): Parser[Loc] = {
    (parser <~ ":Sens[") ~ tp <~ "]" ^^ {
      case loc ~ tp => TraceSensLoc(loc, tp)
    }
  }

  // recency
  lazy val recent: Parser[Recent.type] = "R" ^^^ Recent
  lazy val old: Parser[Old.type] = "O" ^^^ Old
  lazy val recencyTag: Parser[RecencyTag] = recent | old
  def recency(parser: Parser[Loc]): Parser[Loc] = recencyTag ~ parser ^^ {
    case tag ~ loc => Recency(loc, tag)
  }

  // abstract location
  lazy val loc: Parser[Loc] = allocSite |>
    condApply(HeapClone, heapClone) |>
    condApply(RecencyMode, recency)

  def apply(str: String): Try[Loc] =
    Try(parse(loc, str).getOrElse(throw LocParseError(str)))
}
