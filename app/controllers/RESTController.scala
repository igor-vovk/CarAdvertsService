package controllers

import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, Controller}
import repository.{Identifiable, Identifier, Repository, Sorting}

import scala.concurrent.ExecutionContext


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

  def findById(id: String) = Action.async {
    for {
      maybeResult <- repository.findById(Identifier.fromString(id))
    } yield {
      maybeResult match {
        case Some(result) => Ok(Json.toJson(result))
        case _ => NotFound
      }
    }
  }

  def persist = Action.async(parse.json[T]) { req =>
    for {
      persisted <- repository.persist(req.body)
    } yield Ok(Json.toJson(persisted))
  }

  def delete(id: String) = Action.async {
    for {
      _ <- repository.remove(Identifier.fromString(id))
    } yield Ok(Json.obj("status" -> "OK"))
  }

}
