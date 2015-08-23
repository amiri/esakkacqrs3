package esakkacqrs3

import java.util.UUID

import org.apache.commons.validator.routines.EmailValidator
import org.joda.time.{DateTime, DateTimeZone}

object Domain {

  final class Id private[esakkacqrs3](val id: String)

  object Id {
    private[esakkacqrs3] def newId = java.util.UUID.randomUUID().toString
  }

  final class Timestamp private[esakkacqrs3](val timestamp: DateTime)

  object Timestamp {
    private[esakkacqrs3] def newTimestamp = new DateTime(DateTimeZone.UTC)
  }

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

  case class UserCreationAck(text: String) extends DomainAck

  case class UserCreationError(text: String) extends DomainError

  case class EmailChangeAck(text: String) extends DomainAck

  case class EmailChangeError(text: String) extends DomainError

  case class UserQueryError(text: String) extends DomainError

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
