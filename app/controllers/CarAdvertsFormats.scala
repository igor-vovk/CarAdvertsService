package controllers

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, Json, Reads, Writes, _}
import repository.CarFuelType.CarFuelType
import repository.{CarAdvert, CarFuelType, _}


trait CarAdvertsFormats {

  implicit val carFuelTypeFormat: Format[CarFuelType] = {
    val error = JsError(ValidationError("error.undefined", "Must be one of: " + CarFuelType.valid.mkString(",")))

    Format[CarFuelType](
      Reads(jsv => {
        for {
          str <- jsv.validate[String]
          cft <- CarFuelType.fromString(str).fold[JsResult[CarFuelType]](error)(JsSuccess(_))
        } yield cft
      }),
      Writes(o => JsString(o.name))
    )
  }

  implicit val carAdvertFormat: Format[CarAdvert] = {
    val errorMustBeEmpty = ValidationError("error.defined", "Field must be empty")
    val errorMustBeDefined = ValidationError("error.empty", "Field must be defined")

    def existenceCheck[T](enabled: Boolean)(rds: Reads[Option[T]]): Reads[Option[T]] = {
      rds
        .filter(errorMustBeEmpty)(_.isDefined && enabled)
        .filter(errorMustBeDefined)(_.isEmpty && !enabled)
    }

    Format[CarAdvert](
      for {
        id <- (__ \ "id").read[String].map(Identifier.fromString)
        title <- (__ \ "title").read[String]
        carFuelType <- (__ \ "car_fuel_type").read[CarFuelType]
        price <- (__ \ "price").read[Int]
        isNew <- (__ \ "new").read[Boolean]
        mileage <- existenceCheck(isNew)((__ \ "mileage").readNullable[Int])
        firstRegistration <- existenceCheck(isNew)((__ \ "first_registration").readNullable[LocalDate])
      } yield {
        CarAdvert(id, title, carFuelType, price, isNew, mileage, firstRegistration)
      },
      Json.writes[CarAdvert]
    )
  }

}

object CarAdvertsFormats extends CarAdvertsFormats
