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


import CollectRequestInfo.collectRequestInfo

class HttpService {
  implicit val system          : ActorSystem              = ActorSystem()
  implicit val materializer    : ActorMaterializer        = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val logger = LoggerFactory.getLogger("http-service")
  logger.info("start")
  val actor: ActorRef = system.actorOf(Props(new ClientActor()))
  implicit val timeout: Timeout = 5.seconds

  val route =
    post(
      path(PathMatcher("scala") / "ajjl") {
        uploadedFile("screen") { case (fileInfo, jfile) =>
          val file = File("screen.png")
          File(jfile.getAbsolutePath).copyTo(file, true)
          val feature = actor
            .ask(ClientRequest(Image(file.pathAsString))).mapTo[Commands]
            .map(_.seq.map(_.toJsonString).mkString(";"))

          onComplete(feature) {
            case Success(x) => complete(x)
            case Failure(x) =>
              x.printStackTrace()
              System.exit(-1)
              ???
          }
        }
      }) ~ get(path("hello")(complete("hello world")))

  lazy val http = Http().bindAndHandle(collectRequestInfo(route), "0.0.0.0", 9898)

  val log = LoggerFactory getLogger "http"


}

object HttpService {
  def main(args: Array[String]): Unit = {
    val httpService = new HttpService
    httpService.http
  }
}