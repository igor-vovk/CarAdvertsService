import javax.inject._

import play.api.http.HttpFilters
import play.filters.cors.CORSFilter


@Singleton
class Filters @Inject() (cors: CORSFilter) extends HttpFilters {

  override val filters = Seq(cors)

}
