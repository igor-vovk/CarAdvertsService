import java.util.UUID

package object repository {

  case class Sorting(field: String, asc: Boolean)

  type Identifier = UUID

  object Identifier {
    def next: Identifier = UUID.randomUUID()

    def fromString(str: String): Identifier = UUID.fromString(str)
  }

  trait Identifiable[T] {

    def apply(t: T): Identifier

    def withId(t: T, id: Identifier): T

  }

}
