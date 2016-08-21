package controllers

import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, Controller}
import repository.{Identifiable, Identifier, Repository, Sorting}

import scala.concurrent.{ExecutionContext, Future}


abstract class RESTController[T](implicit f: Format[T],
                                 ident: Identifiable[T],
                                 ec: ExecutionContext) extends Controller {

  def repository: Repository[T]

  def findAll(field: Option[String], ascending: Option[Boolean]) = Action.async {
    val sort: Option[Sorting] = for {
      f <- field
      asc <- ascending
    } yield Sorting(f, asc)

    for {
      res <- repository.findAll(sort.getOrElse(repository.defaultSort))
    } yield Ok(Json.toJson(res))
  }

  def findById(id: Identifier) = Action.async {
    for {
      maybeResult <- repository.findById(id)
    } yield {
      maybeResult match {
        case Some(result) => Ok(Json.toJson(result))
        case _ => NotFound
      }
    }
  }

  def insert = Action.async(parse.json[T]) { req =>
    if (ident(req.body).isDefined) {
      Future.successful(BadRequest(Json.obj("err" -> "Passed identifier in entity under insert route")))
    } else {
      for {
        persisted <- repository.persist(req.body)
      } yield Ok(Json.toJson(persisted))
    }
  }

  def update(id: Identifier) = Action.async(parse.json[T]) { req =>
    val entity = ident.withId(req.body, id)

    for {
      persisted <- repository.persist(entity)
    } yield Ok(Json.toJson(persisted))
  }

  def delete(id: Identifier) = Action.async {
    for {
      _ <- repository.remove(id)
    } yield Ok(Json.obj("status" -> "OK"))
  }

}
