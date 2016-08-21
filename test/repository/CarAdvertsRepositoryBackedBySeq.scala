package repository

import services.SynchronizedTransactionImp

import scala.concurrent.ExecutionContext

object CarAdvertsRepositoryBackedBySeq extends algebra.Orderings {

  val orderingsByFieldName: Map[String, Ordering[CarAdvert]] = Map(
    "id" -> Ordering.by(_.id),
    "title" -> Ordering.by((c: CarAdvert) => c.title),
    "fuel" -> Ordering.by((c: CarAdvert) => c.fuel.name),
    "price" -> Ordering.by((c: CarAdvert) => c.price),
    "new" -> Ordering.by((c: CarAdvert) => c.`new`),
    "mileage" -> Ordering.by((c: CarAdvert) => c.mileage),
    "firstRegistration" -> Ordering.by((c: CarAdvert) => c.firstRegistration)
  )

}

class CarAdvertsRepositoryBackedBySeq()(implicit ec: ExecutionContext)
  extends RepositoryBackedBySeq[CarAdvert](new SynchronizedTransactionImp(Seq.empty))
    with CarAdvertsRepository {

  import CarAdvertsRepositoryBackedBySeq._

  override protected def ordering(field: String) = {
    orderingsByFieldName.getOrElse(field, orderingsByFieldName(defaultSort.field))
  }

}