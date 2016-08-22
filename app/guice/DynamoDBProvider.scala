package guice

import awscala.Credentials
import awscala.dynamodbv2.DynamoDB
import com.amazonaws.regions.{Region, Regions}
import com.google.inject.{Inject, ProvidedBy, Provider, Singleton}
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._


case class DynamoDbConfig(mode: String, credentials: Option[Credentials], dropTables: Boolean)

@Singleton
class DynamoDbConfigProvider @Inject()(config: Config) extends Provider[DynamoDbConfig] {
  lazy val get = {
    val ns = config.as[Config]("dynamodb")

    val mode = ns.as[String]("mode")

    val credentials = for {
      acckey <- ns.getAs[String]("credentials.acckey")
      secret <- ns.getAs[String]("credentials.secret")
    } yield Credentials(acckey, secret)

    val dropTables = ns.getAs[Boolean]("dropTables").getOrElse(false)

    DynamoDbConfig(mode, credentials, dropTables)
  }
}

class DynamoDBProvider @Inject()(config: DynamoDbConfig) extends Provider[DynamoDB] {
  override def get(): DynamoDB = {
    val dydb = config.mode match {
      case "local" => DynamoDB.local()
      case "remote" => DynamoDB(config.credentials.getOrElse(
        sys.error("You must set credentials if you wish to run dynamo in remote mode")
      ))(Region.getRegion(Regions.EU_CENTRAL_1))
      case _ => sys.error("Unknown value set for 'dynamodb.mode'")
    }

    dydb
  }
}
