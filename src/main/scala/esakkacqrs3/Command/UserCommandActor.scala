package esakkacqrs3.Command

import akka.actor.{ Props, ActorSystem, ActorLogging }
import akka.persistence.PersistentActor
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.Config
import esakkacqrs3.Domain.Commands._
import esakkacqrs3.Domain.Events._
import esakkacqrs3.Domain.Objects._
import esakkacqrs3.Domain._

import scala.concurrent.ExecutionContext

object UserCommandActor {
  def props(config: Config)(implicit ec: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer, timeout: Timeout): Props = Props(new UserCommandActor(config))
}

class UserCommandActor(config: Config) extends PersistentActor with ActorLogging {
  override def persistenceId = "user"

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

  def receiveRecover: Receive = {
    case event: UserEvent =>
      updateState(event)
  }

  def initializeState(event: UserCreated) = {
    state = state + (event.id -> User(id = event.id, timestamp = event.timestamp, email = event.email))
  }

  def receiveCommand: Receive = {
    case CreateUser(id, timestamp, email) => {
      val asker = sender()
      if (!state.contains(id)) {
        persist(UserCreated(id, timestamp, email))(event => {
          asker ! UserCreationAck(text = s"You created a user with ID ${event.id}!")
          initializeState(event)
        })
      } else {
        asker ! UserCreationError(text = s"User $id already created")
      }
    }
    case ChangeUserEmail(id, timestamp, email) => {
      val asker = sender()
      state.get(id) match {
        case Some(user) =>
          persist(UserEmailChanged(id, timestamp, email))(event => {
            asker ! EmailChangeAck(text = s"You changed user ${event.id} email!")
            updateState(event)
          })
        case None =>
          asker ! EmailChangeError(text = s"There is no user with id $id")
      }
    }
  }
}
