package models.database

import java.time.format.DateTimeFormatter

import com.google.inject.Inject
import models.database.setups.{
  Experiment,
  Experiments,
  Owner,
  Owners,
  TimeStamp,
  TimeStamps,
  Variant,
  Variants,
  Win,
  Wins
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.hashing.MurmurHash3

class GeneralDB @Inject() (
    experiments: Experiments,
    owners: Owners,
    variants: Variants,
    timeStamps: TimeStamps,
    wins: Wins
) {

  def getVariant(
      ownerName: String,
      experimentId: String,
      userId: String
  ): Future[Variant] = {

    if (userId.length > 25) throw new Exception("userId too long")

    val FOPOwner: Future[Option[Owner]] = owners.getOwner(ownerName)
    FOPOwner flatMap ({
      //         verify owner exists
      case Some(owner) => {

        val FOPExperiment: Future[Option[Experiment]] =
          experiments.getExperiment(ownerName, experimentId)

        FOPExperiment flatMap ({

          //                  verify experiment exists
          case Some(experiment) => {
            variants
              .getVariant(ownerName, experimentId, userId)
              .flatMap((opVariant: Option[Variant]) =>
                opVariant match {
                  //verify userId exists

                  case Some(variant) => Future(variant)
                  case None => {
                    val variantType =
                      generateVariantType(userId, experiment.numOfVariants)
                    createVariant(
                      ownerName,
                      experimentId,
                      userId,
                      variantType,
                      experiment.numOfWinTypes
                    ).flatMap {
                      case Some(variant) => Future(variant)
                      case None =>
                        throw new NoSuchElementException("variant dosent exist")
                    }
                  }
                }
              )
          }
          case None =>
            throw new NoSuchElementException("experiment dosent exist")
        })
      }
      case None => throw new NoSuchElementException("owner dosent exist")
    })
  }

  private def generateVariantType(userId: String, numOfVariants: Int): Int = {
    val stringHash = MurmurHash3.stringHash(userId)
    val finalHashValue = MurmurHash3.finalizeHash(
      stringHash % numOfVariants,
      numOfVariants
    ) % numOfVariants
    Math.abs(finalHashValue) + 1
  }

  private def createVariant(
      ownerName: String,
      experimentId: String,
      userId: String,
      variantType: Int,
      numOfWinTypes: Int
  ) = {
    val addVariant: Future[Int] = variants.addVariant(
      Variant(ownerName, experimentId, userId, variantType, 0, 0)
    )

    addVariant.flatMap(done => {
      (1 to numOfWinTypes).map((winType: Int) => {
        val newWin =
          Win(ownerName, experimentId, userId, variantType, winType, 0)
        wins.addWin(newWin)
      })
      variants.getVariant(ownerName, experimentId, userId)
    })
  }

  def markVariantDisplayed(
      ownerName: String,
      experimentId: String,
      userId: String
  ): Future[Int] = {
    val fVariant: Future[Variant] =
      getVariant(ownerName, experimentId, userId)

    fVariant.map(variant => {
      countDisplayedUp(
        ownerName,
        experimentId,
        userId,
        variant.variantType,
        variant.numOfDisplays
      )
      variant.variantType
    })
  }

  private def countDisplayedUp(
      ownerName: String,
      experimentId: String,
      userId: String,
      variantType: Int,
      oldNumOfDisplays: Int
  ) = {
    variants.updateNumOfDisplays(
      ownerName,
      experimentId,
      userId,
      oldNumOfDisplays
    )
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timestamp = TimeStamp(
      ownerName,
      experimentId,
      userId,
      variantType,
      java.time.LocalDate.now.format(formatter),
      display = true,
      win = false
    )
    timeStamps.addTimeStamp(timestamp)
  }

  def markVariantWon(
      ownerName: String,
      experimentId: String,
      userId: String,
      winType: Int
  ): Future[Int] = {
    val fVariant: Future[Variant] =
      getVariant(ownerName, experimentId, userId)
    fVariant.flatMap((variant: Variant) => {
      val fOpWin: Future[Option[Win]] =
        wins.getWin(ownerName, experimentId, userId, winType)

      fOpWin.map((opWin: Option[Win]) => {
        opWin match {
          case Some(win: Win) => {
            countWonUp(
              ownerName,
              experimentId,
              userId,
              variant.variantType,
              variant.totalNumOfWins,
              winType,
              win.numOfWins
            )
            variant.variantType
          }
          case None => {
            throw new NoSuchElementException(
              "Win record dosent exist for variant"
            )

          }
        }
      })
    })
  }

  private def countWonUp(
      ownerName: String,
      experimentId: String,
      userId: String,
      variantType: Int,
      oldNumOfTotalWins: Int,
      winType: Int,
      oldNumOfWins: Int
  ) = {
    val updateVariant: Future[Int] = variants.updateNumOfWins(
      ownerName,
      experimentId,
      userId,
      oldNumOfTotalWins
    )
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timestamp = TimeStamp(
      ownerName,
      experimentId,
      userId,
      variantType,
      java.time.LocalDate.now.format(formatter),
      display = false,
      win = true
    )
    val addTimeStamp: Future[Int] = timeStamps.addTimeStamp(timestamp)
    val updateWin: Future[Int] =
      wins.updateWin(ownerName, experimentId, userId, winType, oldNumOfWins)

    updateVariant.flatMap(variantDone => {
      addTimeStamp.flatMap(timeStampDone => {
        updateWin
      })
    })
  }
}
