package repository

import akka.Done
import awscala.dynamodbv2._

import scala.concurrent.{ExecutionContext, Future}

trait EntityFormat[T] {

  def read(i: Item): T = {
    doRead(i.attributes.map(a => a.name -> a.value)(collection.breakOut))
  }

  protected def doRead(map: Map[String, AttributeValue]): T

  def write(t: T): Seq[(String, Any)]

}

case class IndexDescription(hashProp: String, hashVal: Any, rangeProp: String) {
  val index = GlobalSecondaryIndex(
    s"${rangeProp}_idx",
    Seq(KeySchema(hashProp, KeyType.Hash), KeySchema(rangeProp, KeyType.Range)),
    Projection(ProjectionType.KeysOnly, Seq.empty),
    ProvisionedThroughput(2, 2)
  )
}

abstract class DynamoRepository[T](table: Table, fmt: EntityFormat[T])
                                  (implicit ec: ExecutionContext,
                                   ident: Identifiable[T]) extends Repository[T]{

  implicit def db: DynamoDB

  def indexByField(field: String): Option[IndexDescription]

  override def findAll(sort: Sorting): Future[Seq[T]] = Future {
    indexByField(sort.field) match {
      case Some(indexDescription) =>
        table.queryWithIndex(
          indexDescription.index,
          Seq(indexDescription.hashProp -> cond.eq(indexDescription.hashVal)),
          scanIndexForward = sort.asc
        ).map(fmt.read)
      case None =>
        table.scan(Seq.empty)
          .map(fmt.read)
    }
  }

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
