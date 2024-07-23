package ee.rsx.kata.codurance.atm

import ee.rsx.kata.codurance.atm.money.Note
import ee.rsx.kata.codurance.atm.money.Note.*
import kotlin.math.min

class AtmMachine(limitedFunds: Boolean = false) {

  private val funds: MutableList<Note>?

  init {
    funds = if (limitedFunds)
      filledFunds()
    else
      null
  }

  private fun filledFunds() =
    mutableListOf<Note>().apply {
      mapOf(
        BILL_500 to 2,
        BILL_200 to 3,
        BILL_100 to 5,
        BILL_50 to 12,
        BILL_20 to 20,
        BILL_10 to 50,
        BILL_5 to 100,
        COIN_2 to 250,
        COIN_1 to 500
      ).forEach { (note, count) ->
        repeat(count) { add(note) }
      }
      sortByDescending { it.nomination }
    }

  val remainingBalance: Int
    get() = funds?.let { funds.sumOf { it.nomination } }
      ?: throw IllegalStateException("Cannot determine remaining balance, ATM works with limitless funds")

  val remainingFunds: List<Note>
    get() = funds?.let { funds.toList() }
      ?: throw IllegalStateException("Cannot determine remaining funds, ATM works with limitless funds")

  fun withdraw(amount: Int): List<Note> {
    var remainingAmount = amount

    val notesTaken = Note.entries
      .sortedByDescending { it.nomination }
      .flatMap { note ->
        if (remainingAmount == 0 || note.nomination > remainingAmount || funds?.noneAvailableOf(note) == true)
          emptyList()
        else {
          val numberOfNotes = determineNumberOfNotesToTake(remainingAmount, note)
          remainingAmount -= numberOfNotes * note.nomination

          funds?.take(numberOfNotes, note)
            ?: List(numberOfNotes) { note }
        }
      }

    ensureCompleteWithdrawal(remainingAmount, notesTaken, amount)

    return notesTaken
  }

  private fun ensureCompleteWithdrawal(remainingAmount: Int, notesTaken: List<Note>, amount: Int) {
    if (remainingAmount == 0) {
      return
    }
    funds?.addAll(notesTaken)
    throw IllegalStateException("Not enough funds to withdraw required amount ($amount) - please use another ATM")
  }

  private fun List<Note>.noneAvailableOf(note: Note) = none { it == note }

  private fun determineNumberOfNotesToTake(remainingAmount: Int, note: Note): Int {
    val sumOfNotes = remainingAmount - (remainingAmount % note.nomination)
    val requiredNotesCount = sumOfNotes / note.nomination
    val availableNotes = funds?.countOf(note) ?: requiredNotesCount
    return min(availableNotes, requiredNotesCount)
  }

  private fun List<Note>.countOf(note: Note) = count { it == note }

  private fun MutableList<Note>.take(number: Int, ofNote: Note): List<Note> {
    val notesTaken: MutableList<Note> = mutableListOf()
    repeat(number) {
      val removeIndex = indexOfFirst { availableNote -> availableNote == ofNote }
      notesTaken.add(removeAt(removeIndex))
    }
    return notesTaken
  }
}
