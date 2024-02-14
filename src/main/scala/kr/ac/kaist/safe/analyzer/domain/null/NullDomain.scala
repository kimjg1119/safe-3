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

// null abstract domain
trait NullDomain extends AbsDomain[Null] {
  // abstract null element
  type Elem <: ElemTrait

  // abstract null element traits
  trait ElemTrait extends AbsElemTrait { this: Elem =>
    def StrictEquals(that: Elem): AbsBool
  }
}
