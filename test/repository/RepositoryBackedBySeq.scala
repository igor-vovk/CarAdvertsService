package repository

import akka.Done
import services.Transaction

import scala.concurrent.{ExecutionContext, Future}

abstract class RepositoryBackedBySeq[T](protected val storage: Transaction[Seq[T]])
                                       (implicit ident: Identifiable[T], ec: ExecutionContext) extends Repository[T]{

  protected def ordering(field: String): Ordering[T]

  private def mkNewIdentifier(seq: Seq[T]): Identifier = {
    if (seq.isEmpty) {
      Identifier.zero
    } else {
      Identifier.inc(seq.flatMap(ident(_)).max)
    }
  }

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
      seq.find(v => ident(v).contains(id))
    }
  }

  /**
    * Execute update, if id == Some, insert otherwise
    */
  override def persist(toPersist: T): Future[T] = {
    storage.updateAndGet { seq =>
      ident(toPersist) match {
        case Some(id) => // Execute update
          // Can be accomplished using .foldLeft, but builder gives better performance
          val b = Seq.newBuilder[T]

          seq.foreach(advert =>
            if (ident(advert).contains(id)) {
              b += toPersist
            } else {
              b += advert
            }
          )

          (b.result(), toPersist)
        case _ => // Execute insert
          val newId: Identifier = mkNewIdentifier(seq)

          val toPersistWithId = ident.withId(toPersist, newId)

          (seq :+ toPersistWithId, toPersistWithId)
      }
    }
  }

  override def remove(id: Identifier): Future[Done] = {
    storage.update(_.filterNot(ident(_).contains(id))).map(_ => Done)
  }

}
