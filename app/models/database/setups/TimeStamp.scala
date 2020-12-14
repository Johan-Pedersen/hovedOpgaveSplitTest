package models.database.setups

import com.google.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class TimeStamp(
    ownerName: String,
    experimentId: String,
    userId: String,
    variantType: Int,
    registeredDate: String,
    display: Boolean,
    win: Boolean
)

import slick.jdbc.MySQLProfile.api._

class TimeStampTableDef(tag: Tag) extends Table[TimeStamp](tag, "timeStamps") {
  def ownerName      = column[String]("OWNERNAME", O.PrimaryKey)
  def experimentId   = column[String]("EXPERIMENT_ID", O.PrimaryKey)
  def userId         = column[String]("USER_ID", O.PrimaryKey)
  def variantType    = column[Int]("VARIANT_TYPE")
  def registeredDate = column[String]("REGISTERED_DATE")
  def display        = column[Boolean]("DISPLAY")
  def win            = column[Boolean]("WIN")
  def id             = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  // Select
  def * =
    (
      ownerName,
      experimentId,
      userId,
      variantType,
      registeredDate,
      display,
      win
    ) <> (TimeStamp.tupled, TimeStamp.unapply)
}

class TimeStamps @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  val timeStamps = TableQuery[TimeStampTableDef]

  def addTimeStamp(timeStamp: TimeStamp): Future[Int] = {

    dbConfig.db.run(timeStamps += timeStamp)
  }

  def getTimeStamp(
      ownerName: String,
      experimentId: String,
      userId: String,
      id: Int
  ) = {
    dbConfig.db.run(
      timeStamps
        .filter(x =>
          x.ownerName === ownerName && x.experimentId === experimentId && x.userId === userId && x.id === id
        )
        .result
        .headOption
    )
  }

  def listAllTimeStamps(
      ownerName: String,
      experimentId: String
  ): Future[Seq[TimeStamp]] = {

    dbConfig.db.run(
      timeStamps
        .filter(x =>
          x.ownerName === ownerName && x.experimentId === experimentId
        )
        .result
    )
  }

}
