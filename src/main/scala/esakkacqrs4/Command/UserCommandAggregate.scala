package esakkacqrs4.Command

import akka.actor._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.Config
import esakkacqrs4.Domain.Commands._
import esakkacqrs4.Domain.{HasId, DomainMessage, UserCommand}


import scala.concurrent.ExecutionContext

object UserCommandAggregate {
  def props(config: Config)(implicit ec: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer, timeout: Timeout): Props = Props(new UserCommandAggregate(config))
}

class UserCommandAggregate(config: Config)(implicit ec: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer, timeout: Timeout) extends Actor with ActorLogging {

  def dispatch(message: UserCommand, id: String) = {
    println("I have a domain message")
    println(message)
    val asker = sender()
    val userCommandActor = context.child(id).getOrElse {
      context.actorOf(UserCommandActor.props(config), name = id)
    }
    userCommandActor.tell(message, asker)
  }

  def receive: Receive = {
    case message: CreateUser => dispatch(message, message.id)
    case message: ChangeUserEmail => dispatch(message, message.id)
  }
}