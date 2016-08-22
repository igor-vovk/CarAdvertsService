package controllers

import com.google.inject.{Inject, Singleton}
import repository.{CarAdvert, CarAdvertsRepository}

import scala.concurrent.ExecutionContext


@Singleton
class CarAdvertsController @Inject()(val repository: CarAdvertsRepository)
                                    (implicit ec: ExecutionContext) extends RESTController[CarAdvert] {

  // Additional functionality to be added

}
