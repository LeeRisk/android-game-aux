package http

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, RequestContext, Route, RouteResult}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import better.files._
import models.{Commands, ClientRequest, Image}
import nyhx.ClientActor
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global


case class CollectHttpRequest(
                               method: String,
                               url: String,
                               headers: Map[String, String],
                               success: Boolean,
                               startTime: Long,
                               endTime: Long,
                               code: Int = -1
                             ) {
  override def toString = s"[$code] $method $url time:${(endTime - startTime).toDouble / 1000}"
}

object CollectRequestInfo {
  private val logger = LoggerFactory.getLogger("http")

  def collectRequestInfo(route: Route): Route = (context: RequestContext) => {
    val startTime = System.currentTimeMillis()
    route.andThen { rt: Future[RouteResult] =>
      val default = CollectHttpRequest(
        method = context.request.method.value,
        url = context.request.uri.toString(),
        headers = context.request.headers.map(e => e.name() -> e.value()).toMap,
        success = true,
        startTime = startTime,
        endTime = System.currentTimeMillis())
      rt.onComplete {
        case Success(RouteResult.Complete(e)) =>
          logger.info(default.copy(endTime = System.currentTimeMillis(), code = e.status.intValue()).toString)
        case Success(e)                       =>
          logger.info(default.copy(endTime = System.currentTimeMillis()).toString)
        case Failure(e)                       =>
          logger.info(default.copy(endTime = System.currentTimeMillis()).toString)
      }
      rt
    }(context)
  }
}