package models.database.setups

import com.google.inject.Inject
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

case class Owner(ownerName: String, pass: String, salt: String)

case class OwnerFormData(ownerName: String, pass: String)

object OwnerForm {

  val form = Form(
    mapping(
      "ownerName" -> nonEmptyText(maxLength = 100),
      "pass"      -> nonEmptyText(maxLength = 51)
    )(OwnerFormData.apply)(OwnerFormData.unapply)
  )
}

import slick.jdbc.MySQLProfile.api._

class OwnerTableDef(tag: Tag) extends Table[Owner](tag, "owners") {
  def ownerName = column[String]("OWNERNAME", O.PrimaryKey, O.Unique)
  def pass      = column[String]("PASS", O.PrimaryKey)
  def salt      = column[String]("SALT")

  override def * = (ownerName, pass, salt) <> (Owner.tupled, Owner.unapply)
}

class Owners @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(
    implicit executionContext: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {
  val owners = TableQuery[OwnerTableDef]

  def addOwner(owner: Owner) = {
    dbConfig.db.run(owners += owner)
  }

  def deleteOwner(ownerName: String, pass: String) = {
    dbConfig.db.run(
      owners.filter(x => x.ownerName === ownerName && x.pass === pass).delete
    )
  }
  def getOwner(ownerName: String) = {
    dbConfig.db.run(
      owners.filter(x => x.ownerName === ownerName).result.headOption
    )
  }
  def getOwnerSecure(ownerName: String, pass: String) = {
    dbConfig.db.run(
      owners
        .filter(x => x.ownerName === ownerName && x.pass === pass)
        .result
        .headOption
    )
  }

  def listAllOwners() = {
    dbConfig.db.run(owners.result)
  }
}
