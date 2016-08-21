
package object repository {

  case class Sorting(field: String, asc: Boolean)

  type Identifier = Long

  object Identifier {
    def zero: Identifier = 0L

    def inc(id: Identifier): Identifier = id + 1L
  }

  trait Identifiable[T] {

    def apply(t: T): Option[Identifier]

    def withId(t: T, id: Identifier): T

  }

}
