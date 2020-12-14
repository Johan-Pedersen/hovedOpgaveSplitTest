package models.database.setups

import com.google.inject.Inject
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class Experiment(
    ownerName: String,
    experimentId: String,
    numOfVariants: Int,
    numOfWinTypes: Int
)

//used to take create experiment input data
case class ExperimentFormData(
    experimentId: String,
    numOfVariants: Int,
    numOfWinTypes: Int
)

object ExperimentForm {

  val form = Form(
    mapping(
      "experimentId"  -> nonEmptyText(maxLength = 25),
      "numOfVariants" -> number,
      "numOfWinTypes" -> number
    )(ExperimentFormData.apply)(ExperimentFormData.unapply)
  )
}

import slick.jdbc.MySQLProfile.api._

class ExperimentTableDef(tag: Tag)
    extends Table[Experiment](tag, "experiment") {
  def ownerName     = column[String]("OWNERNAME", O.PrimaryKey)
  def experimentId  = column[String]("EXPERIMENT_ID", O.PrimaryKey)
  def numOfVariants = column[Int]("NUM_OF_VARIANTS")
  def numOfWinTypes = column[Int]("NUM_OF_WIN_TYPES")
  // Select
  def * =
    (
      ownerName,
      experimentId,
      numOfVariants,
      numOfWinTypes
    ) <> (Experiment.tupled, Experiment.unapply)
}

class Experiments @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  val experiments = TableQuery[ExperimentTableDef]

  def addExperiment(experiment: Experiment): Future[Int] = {

    dbConfig.db.run(experiments += experiment)
  }

  def deleteExperiment(ownerName: String, experimentId: String): Future[Int] = {
    dbConfig.db.run(
      experiments
        .filter(x =>
          x.ownerName === ownerName
            && x.experimentId === experimentId
        )
        .delete
    )
  }

  def getExperiment(
      ownerName: String,
      experimentId: String
  ): Future[Option[Experiment]] = {
    dbConfig.db.run(
      experiments
        .filter(x =>
          x.ownerName === ownerName
            && x.experimentId === experimentId
        )
        .result
        .headOption
    )
  }

  def listAllExperiments(ownerName: String): Future[Seq[Experiment]] = {

    dbConfig.db.run(experiments.filter(x => x.ownerName === ownerName).result)
  }

}
