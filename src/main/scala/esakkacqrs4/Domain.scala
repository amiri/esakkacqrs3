package esakkacqrs4

import java.util.UUID

import org.apache.commons.validator.routines.EmailValidator
import org.joda.time.{DateTime, DateTimeZone}

object Domain {

  sealed trait DomainEntity

  sealed trait DomainMessage extends DomainEntity {
    val id = java.util.UUID.randomUUID().toString
    val timestamp: DateTime = new DateTime(DateTimeZone.UTC)
  }

  sealed trait HasEmail {
    val email: String
    require(EmailValidator.getInstance.isValid(email))
  }

  sealed trait HasId {
    protected def id: String
  }

  sealed trait HasTimestamp {
    protected def timestamp: DateTime
  }

  trait DomainResponse extends DomainMessage

  trait DomainAck extends DomainResponse

  trait DomainError extends DomainResponse

  trait DomainCommand extends DomainMessage

  trait DomainQuery extends DomainEntity

  trait DomainEvent extends DomainMessage

  trait UserEvent extends DomainEntity with HasId with HasTimestamp with HasEmail {
    val id: String
    val timestamp: DateTime
    val email: String
  }

  trait UserCommand extends DomainEntity with HasEmail

  trait UserQuery extends DomainQuery

  trait DomainObject extends DomainEntity

  final class Id private[esakkacqrs4](val id: String)

  final class Timestamp private[esakkacqrs4](val timestamp: DateTime)

  case class UserCreationAck(text: String) extends DomainAck

  case class UserCreationError(text: String) extends DomainError

  case class EmailChangeAck(text: String) extends DomainAck

  case class EmailChangeError(text: String) extends DomainError

  case class UserQueryError(text: String) extends DomainError

  object Id {
    private[esakkacqrs4] def newId = java.util.UUID.randomUUID().toString
  }

  object Timestamp {
    private[esakkacqrs4] def newTimestamp = new DateTime(DateTimeZone.UTC)
  }

  object Objects {

    case class User(email: String, id: String = Id.newId, timestamp: DateTime = Timestamp.newTimestamp) extends DomainObject with HasEmail with HasId with HasTimestamp

  }

  object Events {

    case class UserCreated(id: String = Id.newId, timestamp: DateTime = Timestamp.newTimestamp, email: String) extends UserEvent with HasId with HasTimestamp

    case class UserEmailChanged(id: String = Id.newId, timestamp: DateTime = Timestamp.newTimestamp, email: String) extends UserEvent with HasId with HasTimestamp

  }

  object Commands {

    case class CreateUser(id: String = Id.newId, timestamp: DateTime = Timestamp.newTimestamp, email: String) extends UserCommand with HasId with HasTimestamp

    case class ChangeUserEmail(id: String = Id.newId, timestamp: DateTime = Timestamp.newTimestamp, email: String) extends UserCommand with HasId with HasTimestamp

  }

  object Queries {

    case class ShowUser(id: UUID)

    case class ShowUsers()

  }

}
