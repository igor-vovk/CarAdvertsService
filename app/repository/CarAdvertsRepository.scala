package repository

import awscala.dynamodbv2._
import com.amazonaws.services.dynamodbv2.model.{ScalarAttributeType, TableStatus}
import com.google.inject.{Inject, Provider}
import guice.DynamoDbConfig
import org.joda.time.LocalDate
import play.api.Logger

import scala.concurrent.ExecutionContext

object CarFuelType {

  sealed abstract class CarFuelType(val name: String)

  case object Diesel extends CarFuelType("diesel")

  case object Gasoline extends CarFuelType("gasoline")

  val valid: Set[CarFuelType] = Set(Diesel, Gasoline)

  def fromString(str: String): Option[CarFuelType] = valid.find(_.name == str)

}

object CarAdvert {

  implicit val identifiable: Identifiable[CarAdvert] = new Identifiable[CarAdvert] {
    override def apply(t: CarAdvert): Identifier = t.id

    override def withId(t: CarAdvert, id: Identifier): CarAdvert = t.copy(id = id)
  }

}

case class CarAdvert(id: Identifier, title: String, fuel: CarFuelType.CarFuelType, price: Int, `new`: Boolean,
                     mileage: Option[Int], firstRegistration: Option[LocalDate])


trait CarAdvertsRepository extends Repository[CarAdvert] {

  override val defaultSort: Sorting = Sorting("id", asc = true)

}

object CarAdvertsDynamoTable {
  val tableName = "car_adverts"

  val format: EntityFormat[CarAdvert] = new EntityFormat[CarAdvert] {
    override def doRead(i: Map[String, AttributeValue]): CarAdvert = {
      val maybeAdv = for {
        id <- i("id").s.map(Identifier.fromString)
        title <- i("title").s
        fuel <- i("fuel").s.flatMap(CarFuelType.fromString)
        price <- i("price").n.map(_.toInt)
        isNew <- i("new").n.map(n => if (n == "1") true else false)
        mileage <- i.get("mileage").map(_.n.map(_.toInt))
        firstRegistration <- i.get("first_registration").map(_.n.map(n => new LocalDate(n.toLong * 1000L)))
      } yield CarAdvert(id, title, fuel, price, isNew, mileage, firstRegistration)

      maybeAdv.getOrElse(sys.error(s"Can not deserialize CarAdvert from $i"))
    }

    override def write(t: CarAdvert): Seq[(String, Any)] = {
      Seq(
        "id" -> t.id.toString,
        "index_hash" -> 0, // Dummy prop used as indexes hash property
        "title" -> t.title,
        "fuel" -> t.fuel.name,
        "price" -> t.price,
        "new" -> (if (t.`new`) 1 else 0),
        "mileage" -> t.mileage.orNull,
        "first_registration" -> t.firstRegistration.map(dt => dt.toDateTimeAtStartOfDay.getMillis / 1000L).orNull
      )
    }
  }

  val attributes = Seq(
    AttributeDefinition("id", ScalarAttributeType.S),
    AttributeDefinition("index_hash", ScalarAttributeType.N),
    AttributeDefinition("title", ScalarAttributeType.S),
    AttributeDefinition("fuel", ScalarAttributeType.S),
    AttributeDefinition("price", ScalarAttributeType.N),
    AttributeDefinition("new", ScalarAttributeType.N),
    AttributeDefinition("mileage", ScalarAttributeType.N),
    AttributeDefinition("first_registration", ScalarAttributeType.N)
  )

  val TitleIdx = IndexDescription("index_hash", 0, "title")
  val FuelIdx = IndexDescription("index_hash", 0, "fuel")
  val PriceIdx = IndexDescription("index_hash", 0, "price")
  val MileageIdx = IndexDescription("new", 0, "mileage")
  val FirstRegistrationIdx = IndexDescription("new", 0, "first_registration")

  val indexes = Seq(TitleIdx, FuelIdx, PriceIdx, MileageIdx, FirstRegistrationIdx)

}

class CarAdvertsRepositoryDynamoProvider @Inject()(db: DynamoDB, conf: DynamoDbConfig)
                                                  (implicit ec: ExecutionContext)
  extends Provider[CarAdvertsRepositoryDynamo] {

  import CarAdvertsDynamoTable._

  val log = Logger(getClass)

  def get(): CarAdvertsRepositoryDynamo = {
    if (conf.dropTables) {
      log.info("Dropping tables")

      db.table(CarAdvertsDynamoTable.tableName) match {
        case Some(oldTable) =>
          db.deleteTable(oldTable)
        case _ =>
        // Do nothing
      }
    }

    val table = db.table(CarAdvertsDynamoTable.tableName) match {
      case Some(t) => t
      case None =>
        val tableMeta = db.createTable(Table(
          name = CarAdvertsDynamoTable.tableName,
          hashPK = "id",
          attributes = CarAdvertsDynamoTable.attributes,
          globalSecondaryIndexes = indexes.map(_.index)
        ))
        log.info(s"Created Table: $tableMeta")


        log.info("Waiting for DynamoDB table activation...")
        var isTableActivated = false
        while (!isTableActivated) {
          db.describe(tableMeta.table).foreach { meta =>
            isTableActivated = meta.status == TableStatus.ACTIVE
          }

          log.info("Wait...")

          Thread.sleep(1000L)
        }

        log.info("Created DynamoDB table has been activated.")

        db.table(CarAdvertsDynamoTable.tableName).get
    }

    new CarAdvertsRepositoryDynamo(db, table)
  }
}

// TODO: provide different ExecutionContext with higher number of threads here
class CarAdvertsRepositoryDynamo @Inject()(val db: DynamoDB, table: Table)(implicit ec: ExecutionContext)
  extends DynamoRepository[CarAdvert](table, CarAdvertsDynamoTable.format)
    with CarAdvertsRepository {

  override def indexByField(field: String): Option[IndexDescription] = {
    CarAdvertsDynamoTable.indexes.find(_.rangeProp == field)
  }

}