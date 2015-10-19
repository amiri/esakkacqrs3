package esakkacqrs4.Command

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.Config
import esakkacqrs4.Domain.Commands._
import esakkacqrs4.Domain.Events._
import esakkacqrs4.Domain.Objects._
import esakkacqrs4.Domain._

import scala.concurrent.ExecutionContext

object UserCommandActor {
  def props(config: Config)(implicit ec: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer, timeout: Timeout): Props = Props(new UserCommandActor(config))
}

class UserCommandActor(config: Config) extends PersistentActor with ActorLogging {
  val userId = java.util.UUID.fromString(self.path.name).toString
  var state: Option[User] = None

  println(self.path.name)
  println(persistenceId)
  println(userId)

  override def persistenceId = "user"

  def receiveRecover: Receive = {
    case event: UserEvent =>
      if (event.id == userId) updateState(event)
  }

  def receiveCommand: Receive = {
    case CreateUser(id, timestamp, email) => {
      val asker = sender()
      state match {
        case Some(user) =>
          asker ! UserCreationError(text = s"User $id already created")
        case None =>
          persist(UserCreated(userId, timestamp, email))(event => {
            asker ! UserCreationAck(text = s"You created a user with ID ${userId}!")
            initializeState(event)
          })
      }
    }
    case ChangeUserEmail(id, timestamp, email) => {
      val asker = sender()
      state match {
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

  def updateState(event: UserEvent): Unit = event match {
    case UserCreated(id, timestamp, email) =>
      state = Some(User(id = userId, timestamp = event.timestamp, email = event.email))
    case UserEmailChanged(id, timestamp, email) =>
      state match {
        case Some(user) =>
          state = Some(user.copy(email = email, timestamp = timestamp))
        case None =>
          state = state
      }
  }

  def initializeState(event: UserCreated) = {
    state = Some(User(id = event.id, timestamp = event.timestamp, email = event.email))
  }
}
