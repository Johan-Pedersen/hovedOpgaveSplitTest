package models.database.setups

import com.google.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class Variant(
    ownerName: String,
    experimentId: String,
    userId: String,
    variantType: Int,
    numOfDisplays: Int,
    totalNumOfWins: Int
)

import slick.jdbc.MySQLProfile.api._

class VariantTableDef(tag: Tag) extends Table[Variant](tag, "variant") {
  def ownerName      = column[String]("OWNERNAME", O.PrimaryKey)
  def experimentId   = column[String]("EXPERIMENT_ID", O.PrimaryKey)
  def userId         = column[String]("USER_ID", O.PrimaryKey)
  def variantType    = column[Int]("VARIANT_TYPE")
  def numOfDisplays  = column[Int]("NUM_OF_DISPLAYS")
  def totalNumOfWins = column[Int]("TOTAL_NUM_OF_WINS")

  // Select
  def * =
    (
      ownerName,
      experimentId,
      userId,
      variantType,
      numOfDisplays,
      totalNumOfWins
    ) <> (Variant.tupled, Variant.unapply)
}

class Variants @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  val variants = TableQuery[VariantTableDef]

  def addVariant(variant: Variant): Future[Int] = {

    dbConfig.db.run(variants += variant)
  }

  def deleteVariant(
      ownerName: String,
      experimentId: String,
      userId: String
  ): Future[Int] = {
    dbConfig.db.run(
      variants
        .filter(x =>
          x.ownerName === ownerName
            && x.experimentId === experimentId && x.userId === userId
        )
        .delete
    )
  }

  def getVariant(
      ownerName: String,
      experimentId: String,
      userId: String
  ): Future[Option[Variant]] = {
    dbConfig.db.run(
      variants
        .filter(x =>
          x.ownerName === ownerName
            && x.experimentId === experimentId && x.userId === userId
        )
        .result
        .headOption
    )
  }

  def listAllVariantsFor(
      ownerName: String,
      experimentId: String
  ): Future[Seq[Variant]] = {
    dbConfig.db.run(
      variants
        .filter(x =>
          x.ownerName === ownerName
            && x.experimentId === experimentId
        )
        .result
    )
  }

  def updateNumOfDisplays(
      ownerName: String,
      experimentId: String,
      userId: String,
      oldNumOfDisplays: Int
  ) = {

    val query =
      for (
        variant <- variants if (variant.ownerName === ownerName
          && variant.experimentId === experimentId && variant.userId === userId)
      )
        yield (variant.numOfDisplays)

    dbConfig.db.run(query.update(oldNumOfDisplays + 1))
  }

  def updateNumOfWins(
      ownerName: String,
      experimentId: String,
      userId: String,
      oldNumOfWins: Int
  ) = {

    val query =
      for (
        variant <- variants if (variant.ownerName === ownerName
          && variant.experimentId === experimentId && variant.userId === userId)
      )
        yield (variant.totalNumOfWins)

    dbConfig.db.run(query.update(oldNumOfWins + 1))
  }

}
