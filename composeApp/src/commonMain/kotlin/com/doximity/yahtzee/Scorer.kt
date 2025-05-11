package com.doximity.yahtzee

import com.doximity.yahtzee.Scorer.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TOTAL_TURNS = 13

private val UPPER_SECTION = listOf(
    Category.ONES,
    Category.TWOS,
    Category.THREES,
    Category.FOURS,
    Category.FIVES,
    Category.SIXES
)

class Scorer {
    enum class Category {
        ONES,
        TWOS,
        THREES,
        FOURS,
        FIVES,
        SIXES,
        THREE_OF_KIND,
        FOUR_OF_KIND,
        FULL_HOUSE,
        SMALL_STRAIGHT,
        LARGE_STRAIGHT,
        CHANCE,
        YAHTZEE
    }

    data class Entry(val category: Category, val score: Int? = null)

    data class Scorecard(
        val ones: Entry = Entry(Category.ONES),
        val twos: Entry = Entry(Category.TWOS),
        val threes: Entry = Entry(Category.THREES),
        val fours: Entry = Entry(Category.FOURS),
        val fives: Entry = Entry(Category.FIVES),
        val sixes: Entry = Entry(Category.SIXES),
        val sum: Int? = null,
        val sumBonus: Int? = null,
        val threeKind: Entry = Entry(Category.THREE_OF_KIND),
        val fourKind: Entry = Entry(Category.FOUR_OF_KIND),
        val fullHouse: Entry = Entry(Category.FULL_HOUSE),
        val smallStraight: Entry = Entry(Category.SMALL_STRAIGHT),
        val largeStraight: Entry = Entry(Category.LARGE_STRAIGHT),
        val chance: Entry = Entry(Category.CHANCE),
        val yahtzee: Entry = Entry(Category.YAHTZEE),
        val yahtzeeBonus: Int? = null,
        val total: Int? = null
    )

    private var turnsLeft = TOTAL_TURNS
    private var sum = 0
    private var yahtzeeBonus: Int? = null
    private val scorecardEntries: MutableMap<Category, Int> = mutableMapOf()
    private val scorecardFlow = MutableStateFlow(Scorecard())

    suspend fun submitScore(category: Category, dice: List<Roller.Die>) {
        if (scorecardEntries[category] != null) return // already scored

        val score = calculateScore(category, dice)

        // check for bonus yahtzee
        if (dice.isBonusYahtzee()) {
            yahtzeeBonus = (yahtzeeBonus ?: 0) + 100
        }

        scorecardEntries[category] = score

        // update sum for bonus
        when (category) {
            Category.ONES,
            Category.TWOS,
            Category.THREES,
            Category.FOURS,
            Category.FIVES,
            Category.SIXES -> sum += score
            else -> Unit
        }

        turnsLeft--
        updateScorecardFlow()
    }

    fun calculateScore(category: Category, dice: List<Roller.Die>): Int {
        return when (category) {
            Category.ONES -> dice.getSumForDie(1)
            Category.TWOS -> dice.getSumForDie(2)
            Category.THREES -> dice.getSumForDie(3)
            Category.FOURS -> dice.getSumForDie(4)
            Category.FIVES -> dice.getSumForDie(5)
            Category.SIXES -> dice.getSumForDie(6)
            Category.THREE_OF_KIND -> dice.getSumForKind(3)
            Category.FOUR_OF_KIND -> dice.getSumForKind(4)
            Category.FULL_HOUSE -> if (
                dice.distinctBy { it.value }.let {
                    when (it.size) {
                        1 -> dice.isBonusYahtzee()
                        2 -> dice.groupingBy { it.value }.eachCount().filterValues { it < 4 }.size == 2
                        else -> false
                    }
                }
            ) 25 else 0
            Category.SMALL_STRAIGHT -> if (detectStraight(dice, 4) || dice.isBonusYahtzee()) 30 else 0
            Category.LARGE_STRAIGHT -> if (detectStraight(dice, 5) || dice.isBonusYahtzee()) 40 else 0
            Category.CHANCE -> dice.sumOf { it.value }
            Category.YAHTZEE -> if (dice.isYahtzee()) 50 else 0
        }
    }

    private suspend fun updateScorecardFlow() {
        scorecardFlow.emit(
            Scorecard(
                ones = Entry(Category.ONES, scorecardEntries[Category.ONES]),
                twos = Entry(Category.TWOS, scorecardEntries[Category.TWOS]),
                threes = Entry(Category.THREES, scorecardEntries[Category.THREES]),
                fours = Entry(Category.FOURS, scorecardEntries[Category.FOURS]),
                fives = Entry(Category.FIVES, scorecardEntries[Category.FIVES]),
                sixes = Entry(Category.SIXES, scorecardEntries[Category.SIXES]),
                sum = if (isUpperSectionComplete()) sum else null,
                sumBonus = if (isUpperSectionComplete()) getSumBonusTotal() else null,
                threeKind = Entry(Category.THREE_OF_KIND, scorecardEntries[Category.THREE_OF_KIND]),
                fourKind = Entry(Category.FOUR_OF_KIND, scorecardEntries[Category.FOUR_OF_KIND]),
                fullHouse = Entry(Category.FULL_HOUSE, scorecardEntries[Category.FULL_HOUSE]),
                smallStraight = Entry(Category.SMALL_STRAIGHT, scorecardEntries[Category.SMALL_STRAIGHT]),
                largeStraight = Entry(Category.LARGE_STRAIGHT, scorecardEntries[Category.LARGE_STRAIGHT]),
                chance = Entry(Category.CHANCE, scorecardEntries[Category.CHANCE]),
                yahtzee = Entry(Category.YAHTZEE, scorecardEntries[Category.YAHTZEE]),
                yahtzeeBonus = yahtzeeBonus,
                total = if (turnsLeft == 0) {
                    scorecardEntries.values.sum() + getSumBonusTotal() + (yahtzeeBonus ?: 0)
                } else {
                    null
                }
            )
        )
    }

    private fun List<Roller.Die>.getSumForDie(die: Int) =
        filter { it.value == die }.sumOf { it.value }

    private fun List<Roller.Die>.getSumForKind(kind: Int) =
        if (groupingBy { it.value }.eachCount().filterValues { it >= kind }.isNotEmpty()) {
            sumOf { it.value }
        } else {
            0
        }

    private fun detectStraight(dice: List<Roller.Die>, length: Int): Boolean {
        var count = 0
        var prevValue = 0

        for (die in dice.map { it.value }.distinct().sorted()) {
            if (count == length) break

            if (die == prevValue + 1) {
                count++
            } else {
                count = 1
            }

            prevValue = die
        }

        return count >= length
    }

    private fun List<Roller.Die>.isYahtzee() = distinctBy { it.value }.size == 1

    private fun List<Roller.Die>.isBonusYahtzee() = scorecardEntries[Category.YAHTZEE] == 50 && isYahtzee()

    private fun getSumBonusTotal() = if (sum >= 63) 35 else 0

    private fun isUpperSectionComplete() = scorecardEntries.keys.containsAll(UPPER_SECTION)

    suspend fun reset() {
        turnsLeft = TOTAL_TURNS
        sum = 0
        yahtzeeBonus = null
        scorecardEntries.clear()
        scorecardFlow.emit(Scorecard())
    }

    fun observeScorecard() = scorecardFlow.asStateFlow()
}