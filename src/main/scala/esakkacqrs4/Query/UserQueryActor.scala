package esakkacqrs4.Query

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentView
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.Config
import esakkacqrs4.Domain.Events.{UserCreated, UserEmailChanged}
import esakkacqrs4.Domain.Objects.User
import esakkacqrs4.Domain.Queries.{ShowUsers, ShowUser}
import esakkacqrs4.Domain.{UserEvent, UserQueryError}

import scala.concurrent.ExecutionContext

object UserQueryActor {
  def props(config: Config)(implicit ec: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer, timeout: Timeout): Props = Props(new UserQueryActor(config))
}

class UserQueryActor(config: Config) extends PersistentView with ActorLogging {
  override def persistenceId: String = "user"

  override def viewId: String = "user-view"

  var state: Map[String, User] = Map.empty[String, User]

  def updateState(event: UserEvent): Unit = event match {
    case UserCreated(id, timestamp, email) =>
      state = state + (event.id -> User(id = event.id, timestamp = event.timestamp, email = event.email))
    case UserEmailChanged(id, timestamp, email) =>
      state.get(id) match {
        case Some(user) =>
          state = state + (id -> user.copy(email = email))
        case None =>
          state = state
      }
  }

  def receive: Receive = {
    case event: UserEvent if isPersistent =>
      updateState(event)
    case ShowUser(id) =>
      sender ! state.getOrElse(id.toString, UserQueryError(text = "Nothing available"))
    case ShowUsers() =>
      sender ! state
    case _ =>
      sender ! UserQueryError(text = "WTF are you doing?")
  }
}
