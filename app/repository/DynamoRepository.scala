package repository

import akka.Done
import awscala.dynamodbv2.{AttributeValue, DynamoDB, Item, Table}

import scala.concurrent.{ExecutionContext, Future}

trait EntityFormat[T] {

  def read(i: Item): T = {
    doRead(i.attributes.map(a => a.name -> a.value)(collection.breakOut))
  }

  protected def doRead(map: Map[String, AttributeValue]): T

  def write(t: T): Seq[(String, Any)]

}


abstract class DynamoRepository[T](table: Table, fmt: EntityFormat[T])
                                  (implicit ec: ExecutionContext,
                                   ident: Identifiable[T]) extends Repository[T]{

  implicit def db: DynamoDB

  override def findAll(sort: Sorting): Future[Seq[T]] = ???

  override def findById(id: Identifier): Future[Option[T]] = Future {
    table.getItem(id.toString).map(fmt.read)
  }

  override def persist(toPersist: T): Future[T] = Future {
    table.putItem(ident(toPersist).toString, fmt.write(toPersist): _*)

    toPersist
  }

  override def remove(id: Identifier): Future[Done] = Future {
    table.delete(id.toString)

    Done
  }
}
