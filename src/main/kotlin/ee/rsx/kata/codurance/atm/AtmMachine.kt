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

    private fun filledFunds(): MutableList<Note> {
        val funds = mutableListOf<Note>()
        repeat(2) { funds.add(BILL_500) }
        repeat(3) { funds.add(BILL_200) }
        repeat(5) { funds.add(BILL_100) }
        repeat(12) { funds.add(BILL_50) }
        repeat(20) { funds.add(BILL_20) }
        repeat(50) { funds.add(BILL_10) }
        repeat(100) { funds.add(BILL_5) }
        repeat(250) { funds.add(COIN_2) }
        repeat(500) { funds.add(COIN_1) }
        funds.sortByDescending { it.nomination }
        return funds
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
