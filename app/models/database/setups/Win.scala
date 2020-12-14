package models.database.setups

import com.google.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class Win(
    ownerName: String,
    experimentId: String,
    userId: String,
    variantType: Int,
    winType: Int,
    numOfWins: Int
)

import slick.jdbc.MySQLProfile.api._

class WinTableDef(tag: Tag) extends Table[Win](tag, "win") {
  def ownerName    = column[String]("OWNERNAME", O.PrimaryKey)
  def experimentId = column[String]("EXPERIMENT_ID", O.PrimaryKey)
  def userId       = column[String]("USER_ID", O.PrimaryKey)
  def variantType  = column[Int]("VARIANT_TYPE")
  def winType      = column[Int]("WIN_TYPE", O.PrimaryKey)
  def numOfWins    = column[Int]("NUM_OF_WINS")

  // Select
  def * =
    (
      ownerName,
      experimentId,
      userId,
      variantType,
      winType,
      numOfWins
    ) <> (Win.tupled, Win.unapply)
}

class Wins @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(
    implicit executionContext: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  val wins = TableQuery[WinTableDef]

  def addWin(win: Win): Future[Int] = {

    dbConfig.db.run(wins += win)
  }

  def getWin(
      ownerName: String,
      experimentId: String,
      userId: String,
      winType: Int
  ) = {
    dbConfig.db.run(
      wins
        .filter(x =>
          x.ownerName === ownerName && x.experimentId === experimentId && x.userId === userId && x.winType === winType
        )
        .result
        .headOption
    )
  }

  def updateWin(
      ownerName: String,
      experimentId: String,
      userId: String,
      winType: Int,
      oldNumOfWins: Int
  ) = {

    val query =
      for (
        win <- wins if (win.ownerName === ownerName
          && win.experimentId === experimentId && win.userId === userId && win.winType === winType)
      )
        yield (win.numOfWins)

    dbConfig.db.run(query.update(oldNumOfWins + 1))
  }

  def listAllWins(ownerName: String, experimentId: String): Future[Seq[Win]] = {

    dbConfig.db.run(
      wins
        .filter(x =>
          x.ownerName === ownerName && x.experimentId === experimentId
        )
        .result
    )
  }
}
