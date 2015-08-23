package esakkacqrs3.HTTP

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import esakkacqrs3.Command.UserCommandActor
import esakkacqrs3.Domain.Commands.{ChangeUserEmail, CreateUser}
import esakkacqrs3.Domain.Queries.{ShowUser, ShowUsers}
import esakkacqrs3.Domain._
import esakkacqrs3.Query.UserQueryActor
import org.json4s.{DefaultFormats, jackson}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.StdIn

object API extends App {
  lazy val config = ConfigFactory.load()
  implicit val system = ActorSystem("api", config)
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(10.seconds)
  implicit val ec: ExecutionContext = system.dispatcher

  system.actorOf(APIHttpService.props(config), "api-http")
}

object APIHttpService {
  def props(config: Config)(implicit ec: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer, timeout: Timeout): Props = Props(new APIHttpService(config))
}

class APIHttpService(config: Config)(implicit ec: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer, timeout: Timeout) extends Actor with Directives {

  override def receive: Receive = Actor.emptyBehavior

  def commandActor: ActorRef = context.actorOf(UserCommandActor.props(config), "user-command-actor-" + java.util.UUID.randomUUID().toString)

  def queryActor: ActorRef = context.actorOf(UserQueryActor.props(config), "user-query-actor-" + java.util.UUID.randomUUID().toString)

  val route: Route = {
    import Json4sSupport._

    implicit val serialization = jackson.Serialization
    implicit val formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

    path("user") {
      post {
        entity(as[CreateUser]) { createUser =>
          complete {
            (commandActor ? createUser).mapTo[DomainResponse]
          }
        }
      } ~ get {
        complete {
          (queryActor ? ShowUsers()).mapTo[Map[_,_]]
        }
      }
    } ~ path("user" / JavaUUID) { id =>
      put {
        entity(as[ChangeUserEmail]) { changeUserEmail =>
          // This is hacky, changing the marshalled command like this.
          // We could also just require the id to be posted inside the
          // JSON object.
          val changeCommand = changeUserEmail.copy(id = id.toString)
          complete {
            (commandActor ? changeCommand).mapTo[DomainResponse]
          }
        }
      } ~ get {
        complete {
          (queryActor ? ShowUser(id = id)).mapTo[DomainEntity]
        }
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, config.getString("app.api.host"), config.getInt("app.api.port"))

  println(s"Server online... /\nPress RETURN to stop...")

  // Shutdown with return
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ system.terminate()) // and shutdown when done
}
