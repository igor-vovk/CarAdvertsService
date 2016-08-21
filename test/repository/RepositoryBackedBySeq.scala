package repository

import akka.Done
import services.Transaction

import scala.concurrent.{ExecutionContext, Future}

abstract class RepositoryBackedBySeq[T](protected val storage: Transaction[Seq[T]])
                                       (implicit ident: Identifiable[T], ec: ExecutionContext) extends Repository[T]{

  protected def ordering(field: String): Ordering[T]


  private def mkOrdering(sort: Sorting): Ordering[T] = {
    val o = ordering(sort.field)

    if (sort.asc) {
      o
    } else {
      o.reverse
    }
  }

  override def findAll(sort: Sorting): Future[Seq[T]] = {
    for {
      seq <- storage.get
    } yield {
      seq.sorted(mkOrdering(sort))
    }
  }

  override def findById(id: Identifier): Future[Option[T]] = {
    for {
      seq <- storage.get
    } yield {
      seq.find(v => ident(v) == id)
    }
  }

  /**
    * Execute update, if id == Some, insert otherwise
    */
  override def persist(toPersist: T): Future[T] = {
    storage.updateAndGet { seq =>
      // Can be accomplished using .foldLeft, but builder gives better performance
      val b = Seq.newBuilder[T]

      val updated = seq.foldLeft(false) { case (up, advert) =>
        if (ident(advert) == ident(toPersist)) {
          b += toPersist

          true
        } else {
          b += advert

          up
        }
      }

      if (!updated) { // Then insert
        b += toPersist
      }

      (b.result(), toPersist)
    }
  }

  override def remove(id: Identifier): Future[Done] = {
    storage.update(_.filterNot(ident(_) == id)).map(_ => Done)
  }

}
