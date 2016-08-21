package repository

import com.google.inject.Inject
import services.SynchronizedTransactionImpl

import scala.concurrent.ExecutionContext

object CarAdvertsRepositoryBackedBySeq extends algebra.Orderings {

  val orderingsByFieldName: Map[String, Ordering[CarAdvert]] = Map(
    "id" -> Ordering.by(_.id),
    "title" -> Ordering.by(_.title),
    "fuel" -> Ordering.by(_.fuel.name),
    "price" -> Ordering.by(_.price),
    "new" -> Ordering.by(_.`new`),
    "mileage" -> Ordering.by(_.mileage),
    "firstRegistration" -> Ordering.by(_.firstRegistration)
  )

}

class CarAdvertsRepositoryBackedBySeq @Inject() ()(implicit ec: ExecutionContext)
  extends RepositoryBackedBySeq[CarAdvert](new SynchronizedTransactionImpl(Seq.empty))
    with CarAdvertsRepository {

  import CarAdvertsRepositoryBackedBySeq._

  override protected def ordering(field: String) = {
    orderingsByFieldName.getOrElse(field, orderingsByFieldName(defaultSort.field))
  }

}