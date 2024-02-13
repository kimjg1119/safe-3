package kr.ac.kaist.safe.util

object MapHelper {

  type PartialOrder[B1] = (B1, B1) => Boolean
  type OptionPartialOrder[B1] = (Option[B1], Option[B1]) => Boolean

  implicit class MapExtensions[A, B](val self: Map[A, B]) {

    /** Creates a new map which is the merge of this and the argument hash map
      * with an idempotent collision resolution.
      *
      * Uses the specified collision resolution function if two keys are the
      * same. The collision resolution function will always take the first
      * argument from `this` hash map and the second from `that`.
      *
      * @tparam B1
      *   the value type of the other hash map
      * @param that
      *   the other hash map
      * @param mergef
      *   the merge function
      */
    def mergeWithIdem[B1 >: B](
        that: Map[A, B1]
    )(mergef: (Option[B1], Option[B1]) => B1): Map[A, B1] = {
      val keys = self.keySet ++ that.keySet
      keys.map { k =>
        k -> mergef(self.get(k), that.get(k))
      }.toMap
    }

    /** Checks whether this is a subset of the argument hash map based on a
      * given relation.
      *
      * @tparam B1
      *   the value type of the other hash map
      * @param that
      *   the other hash map
      * @param order
      *   the compare function
      */
    def compareWithPartialOrder[B1 >: B](
        that: Map[A, B1]
    )(order: PartialOrder[B1]): Boolean = {
      self.forall { case (k, v) =>
        that.get(k) match {
          case Some(v1) => order(v, v1)
          case None     => false
        }
      }
    }

    /** Checks whether this is a subset of the argument hash map based on a
      * given relation.
      *
      * @tparam B1
      *   the value type of the other hash map
      * @param that
      *   the other hash map
      * @param order
      *   the compare function
      */
    def compareOptionWithPartialOrder[B1 >: B](
        that: Map[A, B1]
    )(order: OptionPartialOrder[B1]): Boolean = {
      self.forall { case (k, v) =>
        that.get(k) match {
          case Some(v1) => order(Some(v), Some(v1))
          case None     => false
        }
      }
    }

    /** Creates a new map which is the union of this and the argument hash map
      * with an idempotent collision resolution.
      *
      * Uses the specified collision resolution function if two keys are the
      * same. The collision resolution function will always take the first
      * argument from `this` hash map and the second from `that`.
      *
      * @tparam B1
      *   the value type of the other hash map
      * @param that
      *   the other hash map
      * @param mergef
      *   the merge function
      */
    def unionWithIdem[B1 >: B](
        that: Map[A, B1]
    )(mergef: (B1, B1) => B1): Map[A, B1] = {
      val keys = self.keySet ++ that.keySet
      keys.map { k =>
        k -> {
          (self.get(k), that.get(k)) match {
            case (Some(v), Some(v1)) => mergef(v, v1)
            case (Some(v), None)     => v
            case (None, Some(v1))    => v1
            case (None, None)        => throw new Error("unreachable")
          }
        }
      }.toMap
    }

    /** Creates a new map which is the intersection of this and the argument
      * hash map with an idempotent collision resolution.
      *
      * Uses the specified collision resolution function if two keys are the
      * same. The collision resolution function will always take the first
      * argument from `this` hash map and the second from `that`.
      *
      * @tparam B1
      *   the value type of the other hash map
      * @param that
      *   the other hash map
      * @param mergef
      *   the merge function
      */
    def intersectWithIdem[B1 >: B](
        that: Map[A, B1]
    )(mergef: (B1, B1) => B1): Map[A, B1] = {
      val keys = self.keySet.intersect(that.keySet)
      keys.map { k =>
        k -> mergef(self(k), that(k))
      }.toMap
    }

  }
}
