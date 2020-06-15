package service

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.search.SearchResponse
import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties, Response}
import com.sksamuel.elastic4s.searches.aggs.TermsAggregation
import com.sksamuel.elastic4s.searches.queries.matches.MatchPhrase
import com.sksamuel.elastic4s.searches.queries.term.TermsQuery
import javax.inject.Inject
import model.{FilterParameter, PageParameter}
import play.api.Configuration

import scala.concurrent.Future

class Elasticsearch @Inject()(configuration: Configuration) {

  val host: String = configuration.get[String]("elastic.host")
  val client: ElasticClient = ElasticClient(ElasticProperties(s"http://$host"))
  val indexName: String = configuration.get[String]("elastic.indexName")

  def queryScientificPapers(fulltext: String,
                            pageParameter: Option[PageParameter],
                            filterParameters: Option[Seq[FilterParameter]]): Future[Response[SearchResponse]] = {
    val filterQueries =
      filterParameters
        .map(_.filter(_.selectedValue.exists(_.nonEmpty)).map(f => TermsQuery(field = f.name, values = f.selectedValue)))
        .getOrElse(Seq.empty)
    try {
      client.execute {
        search(indexName)
          .query {
            val fulltextQuery = if (fulltext.isEmpty) Seq.empty else Seq(MatchPhrase(field = "fulltext", value = fulltext))
            boolQuery().must(fulltextQuery)
          }
          .from(pageParameter.flatMap(_.from).getOrElse(0))
          .size(pageParameter.flatMap(_.size).getOrElse(10))
          .aggregations(createAggregations())
          .postFilter(boolQuery().must(filterQueries))
      }
    } catch {
      case e: Exception => Future.failed(e)
    }
  }

  def createAggregations(): Seq[TermsAggregation] = {
    val filters = configuration.get[Configuration]("graphql.filters")
    filters.subKeys.map { key =>
      TermsAggregation(key).field(key)
    }.toSeq
  }

}
