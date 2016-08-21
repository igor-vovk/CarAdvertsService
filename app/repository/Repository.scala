package repository

import akka.Done

import scala.concurrent.Future

/**
  * Implementing classes can add additional functionality
  */
trait Repository[T] {

  protected def defaultSort: Sorting

  def findAll(sort: Sorting = defaultSort): Future[Seq[T]]

  def findById(id: Identifier): Future[Option[T]]

  def persist(toPersist: T): Future[T]

  def remove(id: Identifier): Future[Done]

}
