
import awscala.dynamodbv2.DynamoDB
import com.typesafe.config.Config
import guice.{DynamoDBProvider, DynamoDbConfig, DynamoDbConfigProvider}
import play.api.inject.{Binding, Module => M}
import play.api.{Configuration, Environment}


class Module extends M {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    seq(
      bind[Config].to(configuration.underlying),
      bind[DynamoDbConfig].toProvider[DynamoDbConfigProvider].eagerly(),
      bind[DynamoDB].toProvider[DynamoDBProvider]
    )
  }
}
