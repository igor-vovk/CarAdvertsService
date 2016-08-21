package algebra

import org.joda.time.LocalDate


trait Orderings {

  implicit val jodaLocalDateOrdering: Ordering[LocalDate] = new Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }

}

object Orderings extends Orderings
