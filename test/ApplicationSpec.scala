import org.scalatest.Inspectors
import org.scalatestplus.play._
import play.api.libs.json.JsArray
import play.api.test.Helpers._
import play.api.test._

class ApplicationSpec extends PlaySpec with Inspectors with OneAppPerSuite {

  "Routes" should {

    "send 404 on a bad request" in {
      status(route(app, FakeRequest(GET, "/boum")).get) mustBe NOT_FOUND
    }

  }

  "CarAdvertsController" should {

    "render all entities" in {
      val index = route(app, FakeRequest(GET, "/")).get

      status(index) mustBe OK
      contentType(index) mustBe Some("application/json")
      contentAsJson(index) mustBe JsArray()
    }

    "accept CORS requests" in {
      forAll(Seq("http://ex.com", "https://amp.com", "http://e.com")) { origin =>
        val r = FakeRequest(GET, "/")
          .withHeaders("Host" -> "car_adverts.com", "Origin" -> origin)
        val index = route(app, r).get

        header("Access-Control-Allow-Origin", index) mustBe Some(origin)
      }
    }

    "insert some data" in {
      val r = FakeRequest(POST, "/")
        .withBody(
          """
            |{
            | "id": "xcmvbkffjdj",
            | "title": "VW Golf",
            | "fuel": "gasoline",
            | "price": 1000,
            | "new": false,
            | "mileage": 1111,
            | "first_registration": "2010-01-30"
            |}
          """.stripMargin)
        .withHeaders("Content-Type" -> "application/json")
      val persist = route(app, r).get

      status(persist) mustBe OK
    }

    "return inserted data" in {
      val r = FakeRequest(GET, "/xcmvbkffjdj")
      val get = route(app, r).get

      status(get) mustBe OK
      contentAsString(get) mustBe "{\"id\":\"xcmvbkffjdj\",\"title\":\"VW Golf\",\"fuel\":\"gasoline\",\"price\":1000,\"new\":false,\"mileage\":1111,\"firstRegistration\":\"2010-01-30\"}"
    }

  }

}
