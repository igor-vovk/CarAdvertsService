package guice

import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import repository.{CarAdvertsRepository, CarAdvertsRepositoryDynamoProvider}


class RepositoriesModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    seq(
      bind[CarAdvertsRepository].toProvider[CarAdvertsRepositoryDynamoProvider]
    )
  }
}
