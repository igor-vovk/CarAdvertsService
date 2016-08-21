package repository

import akka.Done
import org.joda.time.LocalDate
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RepositorySpec extends FlatSpec with MustMatchers with Inspectors with OptionValues with ScalaFutures {

  implicit val cfg = PatienceConfig(scaled(Span(10, Seconds)))

  val repository: CarAdvertsRepository = new CarAdvertsRepositoryBackedBySeq()

  val golf = CarAdvert(None, "VW Golf", CarFuelType.Diesel, 3000, false, Some(40000), Some(new LocalDate(2013, 1, 8)))

  "Repository" must "insert some data" in {
    whenReady(Future.sequence(Seq(
      repository.persist(golf),
      repository.persist(CarAdvert(None, "Audi 500", CarFuelType.Gasoline, 2500, false, Some(120000), Some(new LocalDate(1999, 5, 24)))),
      repository.persist(CarAdvert(None, "BMW 5", CarFuelType.Diesel, 6000, false, Some(120000), Some(new LocalDate(1999, 5, 24))))
    ))) { adverts =>
      forAll(adverts) { advert =>
        advert.id mustBe defined
      }
    }
  }

  it must "fetch all data" in {
    whenReady(repository.findAll()) { adverts =>
      adverts must have size 3
    }
  }

  it must "sort data" in {
    whenReady(repository.findAll()) { adverts =>
      adverts.map(_.id) mustBe sorted
    }

    whenReady(repository.findAll(Sorting("title", asc = true))) { adverts =>
      adverts.map(_.title) mustBe sorted
    }

    whenReady(repository.findAll(Sorting("price", asc = false))) { adverts =>
      adverts.map(_.price).reverse mustBe sorted
    }
  }

  it must "fetch data by identifier" in {
    val persisted = repository.persist(golf).futureValue
    val fetched = repository.findById(persisted.id.get).futureValue

    fetched mustBe defined

    fetched.value.id mustBe defined
    fetched.value.id mustEqual persisted.id
  }

  it must "update data" in {
    val persisted = repository.persist(golf).futureValue
    repository.persist(persisted.copy(title = "VW Passat")).futureValue
    val updated = repository.findById(persisted.id.value).futureValue

    updated mustBe defined
    persisted.id.value mustBe updated.value.id.value
    updated.value.title must not equal persisted.title
  }

  it must "remove data" in {
    val sizeBeforeInsert = repository.findAll().futureValue.size
    val persisted = repository.persist(golf).futureValue
    repository.remove(persisted.id.value).futureValue mustBe Done

    repository.findById(persisted.id.value).futureValue mustBe empty
    val sizeAfterRemove = repository.findAll().futureValue.size

    sizeAfterRemove mustBe sizeAfterRemove
  }

}
