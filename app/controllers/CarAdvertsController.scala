package controllers

import com.google.inject.Inject
import repository.{CarAdvert, CarAdvertsRepository}

import scala.concurrent.ExecutionContext


class CarAdvertsController @Inject()(val repository: CarAdvertsRepository)
                                    (implicit ec: ExecutionContext) extends RESTController[CarAdvert] {

  // Additional functionality to be added

}
