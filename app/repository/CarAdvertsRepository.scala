package repository

import org.joda.time.LocalDate

object CarFuelType {

  sealed abstract class CarFuelType(val name: String)

  case object Diesel extends CarFuelType("diesel")

  case object Gasoline extends CarFuelType("gasoline")

  val valid: Set[CarFuelType] = Set(Diesel, Gasoline)

  def fromString(str: String): Option[CarFuelType] = valid.find(_.name == str)

}

object CarAdvert {

  implicit val identifiable: Identifiable[CarAdvert] = new Identifiable[CarAdvert] {
    override def apply(t: CarAdvert): Option[Identifier] = t.id

    override def withId(t: CarAdvert, id: Identifier): CarAdvert = t.copy(id = Some(id))
  }

}

case class CarAdvert(id: Option[Identifier], title: String, fuel: CarFuelType.CarFuelType, price: Int, `new`: Boolean,
                     mileage: Option[Int], firstRegistration: Option[LocalDate])


trait CarAdvertsRepository extends Repository[CarAdvert]{

  override val defaultSort: Sorting = Sorting("id", asc = true)

}
