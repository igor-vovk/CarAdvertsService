package guice

import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import repository.{CarAdvertsRepository, CarAdvertsRepositoryBackedBySeq}

class TestRepositoriesModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    seq(
      bind[CarAdvertsRepository].to[CarAdvertsRepositoryBackedBySeq]
    )
  }
}
