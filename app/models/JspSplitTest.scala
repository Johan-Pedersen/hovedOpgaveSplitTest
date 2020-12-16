package models

import com.google.inject.Inject
import models.database.GeneralDBSplitTest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JspSplitTest @Inject() (generalDB: GeneralDBSplitTest) {

  /**
    *
    * @param ownerName
    * @param experimentId
    * @param userId
    * @return variantType
    */
  def getVariantType(
      ownerName: String,
      experimentId: String,
      userId: String
  ): Future[Int] = {
    generalDB.getVariant(ownerName, experimentId, userId).map(_.variantType)
  }

  /**
    *
    * @param ownerName
    * @param experimentId
    * @param userId
    * @param winType*
    * @return variantType
    */

  def markVariantWon(
      ownerName: String,
      experimentId: String,
      userId: String,
      winType: Int
  ): Future[Int] = {
    generalDB.markVariantWon(ownerName, experimentId, userId, winType)
  }

  /**
    *
    * @param ownerName
    * @param experimentId
    * @param userId
    * @return variantType
    */
  def markVariantDisplayed(
      ownerName: String,
      experimentId: String,
      userId: String
  ): Future[Int] = {
    generalDB.markVariantDisplayed(ownerName, experimentId, userId)
  }
}
