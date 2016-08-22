import java.util.UUID

package object repository {

  case class Sorting(field: String, asc: Boolean)

  type Identifier = String

  object Identifier {
    def next: Identifier = UUID.randomUUID().toString

    def fromString(str: String): Identifier = str
  }

  trait Identifiable[T] {

    def apply(t: T): Identifier

    def withId(t: T, id: Identifier): T

  }

}
