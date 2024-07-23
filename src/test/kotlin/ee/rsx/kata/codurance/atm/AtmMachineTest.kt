package ee.rsx.kata.codurance.atm

import ee.rsx.kata.codurance.atm.money.Note
import ee.rsx.kata.codurance.atm.money.Note.*
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AtmMachineTest {

    @Test
    fun `withdraw funds of 434 Euros, from limitless ATM`() {
        val atm = AtmMachine()

        val withdrawal = atm.withdraw(434)

        assertThat(withdrawal.sumOf { it.nomination }).isEqualTo(434)
        assertThat(withdrawal)
            .containsExactlyInAnyOrderElementsOf(
                listOfNotesInCounts(mapOf(
                    BILL_200 to 2,
                    BILL_20 to 1,
                    BILL_10 to 1,
                    COIN_2 to 2
                ))
            )
    }

    @Test
    fun `withdraw funds of 1397 Euros, from limitless ATM`() {
        val atm = AtmMachine()

        val withdrawal = atm.withdraw(1397)

        assertThat(withdrawal.sumOf { it.nomination }).isEqualTo(1397)
        assertThat(withdrawal)
            .containsExactlyInAnyOrderElementsOf(
                listOfNotesInCounts(mapOf(
                    BILL_500 to 2,
                    BILL_200 to 1,
                    BILL_100 to 1,
                    BILL_50 to 1,
                    BILL_20 to 2,
                    BILL_5 to 1,
                    COIN_2 to 1
                ))
            )
    }

    @Test
    fun `withdraw funds of 10_000 Euros, from limitless ATM`() {
        val atm = AtmMachine()

        val withdrawal = atm.withdraw(10_000)

        assertThat(withdrawal.sumOf { it.nomination }).isEqualTo(10_000)
        assertThat(withdrawal)
            .containsExactlyInAnyOrderElementsOf(
                listOfNotesInCounts(mapOf(
                    BILL_500 to 20
                ))
            )
    }

    @Test
    fun `withdraw funds of 500_800 Euros, from limitless ATM`() {
        val atm = AtmMachine()

        val withdrawal = atm.withdraw(500_800)

        assertThat(withdrawal.sumOf { it.nomination }).isEqualTo(500_800)
        assertThat(withdrawal)
            .containsExactlyInAnyOrderElementsOf(
                listOfNotesInCounts(mapOf(
                    BILL_500 to 1001,
                    BILL_200 to 1,
                    BILL_100 to 1,
                ))
            )
    }

    @Test
    fun `withdraw funds of 1725 Euros, from limited funds of ATM`() {
        val atm = AtmMachine(limitedFunds = true)
        val initialBalance = atm.remainingBalance

        val withdrawal = atm.withdraw(1725)

        assertThat(withdrawal.sumOf { it.nomination }).isEqualTo(1725)
        assertThat(withdrawal)
            .containsExactlyInAnyOrderElementsOf(
                listOfNotesInCounts(mapOf(
                    BILL_500 to 2,
                    BILL_200 to 3,
                    BILL_100 to 1,
                    BILL_20 to 1,
                    BILL_5 to 1
                ))
            )

        val expectedRemainingBalance = initialBalance - 1725
        assertThat(atm.remainingBalance).isEqualTo(expectedRemainingBalance)
        atm.remainingFunds.assertHasExactlyNotesInCounts(mapOf(
            BILL_100 to 4,
            BILL_50 to 12,
            BILL_20 to 19,
            BILL_10 to 50,
            BILL_5 to 99,
            COIN_2 to 250,
            COIN_1 to 500
        ))
    }

    @Test
    fun `withdraw funds of 1725 Euros and then 1825 Euros, from limited funds of ATM`() {
        val atm = AtmMachine(limitedFunds = true)
        val initialBalance = atm.remainingBalance
        atm.withdraw(1725)

        val withdrawal = atm.withdraw(1825)

        assertThat(withdrawal.sumOf { it.nomination }).isEqualTo(1825)

        assertThat(withdrawal)
            .containsExactlyInAnyOrderElementsOf(
                listOfNotesInCounts(mapOf(
                    BILL_100 to 4,
                    BILL_50 to 12,
                    BILL_20 to 19,
                    BILL_10 to 44,
                    BILL_5 to 1
                ))
            )

        val expectedRemainingBalance = initialBalance - 1725 - 1825
        assertThat(atm.remainingBalance)
            .isEqualTo(expectedRemainingBalance)

        atm.remainingFunds.assertHasExactlyNotesInCounts(mapOf(
            BILL_10 to 6,
            BILL_5 to 98,
            COIN_2 to 250,
            COIN_1 to 500,
        ))
    }

    @Test
    fun `withdraw funds of 1725 Euros, then 1825 Euros, then more than available, from limited funds of ATM`() {
        val atm = AtmMachine(limitedFunds = true)
        val initialBalance = atm.remainingBalance
        atm.withdraw(1725)
        atm.withdraw(1825)

        val test: () -> Unit = { atm.withdraw(1551) }

        assertThrows<IllegalStateException>(test).run {
            assertThat(message).isEqualTo("Not enough funds to withdraw required amount (1551) - please use another ATM")

            val expectedRemainingBalance = initialBalance - 1725 - 1825
            assertThat(atm.remainingBalance).isEqualTo(expectedRemainingBalance)
        }
    }


    private fun List<Note>.assertHas(countOf: Int, note: Note) = assertThat(count { it == note }).isEqualTo(countOf)

    private fun List<Note>.assertHasExactlyNotesInCounts(notesOfCount: Map<Note, Int>) {
        assertThat(size)
            .isEqualTo(notesOfCount.values.sum())

        notesOfCount.forEach { (note, countOf) -> assertHas(countOf, note) }
    }

    private fun listOfNotesInCounts(notesOfCount: Map<Note, Int>) =
        notesOfCount.flatMap { (note, count) -> List(count) { note } }
}
