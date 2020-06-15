package repository

import com.sksamuel.elastic4s.http.search.{SearchHit, TermBucket}
import javax.inject.Inject
import model.{Filter, FilterParameter, FilterValue, GraphQlResult, PageParameter, ScientificPaper, ScientificPaperResult}
import play.api.Configuration
import service.{Elasticsearch, Mongo}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ScientificPaperRepo @Inject()(mongo: Mongo, elasticsearch: Elasticsearch, configuration: Configuration) {

  def queryScientificPaper(fulltext: String,
                           pageParameter: Option[PageParameter],
                           filterParameters: Option[Seq[FilterParameter]]): Future[GraphQlResult] = {
    val elasticResponse = elasticsearch.queryScientificPapers(fulltext, pageParameter, filterParameters)
    val scientificPapersFuture = elasticResponse.map(resp => resp.result.hits.hits.map(transformScientificPaper).toSeq)
    val filtersFuture = elasticResponse.map { resp =>
      val filterConfig = configuration.get[Configuration]("graphql.filters")
      filterConfig.subKeys.map { key =>
        createFilterResult(key, filterConfig.get[Configuration](key).get[String]("name"), resp.result.aggregations.terms(key).buckets)
      }.toSeq
    }
    for {
      scientificPapers <- scientificPapersFuture
      filters <- filtersFuture
      totalCount <- elasticResponse.map(r => r.result.hits.total)
    } yield GraphQlResult(scientificPapers, filters, totalCount)
  }

  def findScientificPaper(identificationNumber: String): Future[List[ScientificPaper]] =
    mongo.findScientificPaper(identificationNumber)

  private def transformScientificPaper(searchHit: SearchHit): ScientificPaperResult = {
    val searchHitMap = searchHit.sourceAsMap
    def getField(fieldName: String) = searchHitMap.getOrElse(fieldName, "").asInstanceOf[String]
    ScientificPaperResult(
      getField("identificationNumber"),
      getField("title"),
      getField("documentType"),
      getField("mentor"),
      getField("mentor"),
      getField("year"),
      getField("publisher"),
      getField("scientificField"),
      getField("description")
    )
  }

  private def createFilterResult(filerKey: String, filerName: String, termBuckets: Seq[TermBucket]): Filter = {
    val filterValues = termBuckets.map(bucket => FilterValue(bucket.key, bucket.docCount))
    Filter(filerKey, filerName, filterValues)
  }

}
