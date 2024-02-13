package kr.ac.kaist.safe.util

object MapHelper {

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

      newMap
    }
  }
}
