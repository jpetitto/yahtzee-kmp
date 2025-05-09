package com.doximity.yahtzee

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TOTAL_TURNS = 13

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

    data class Box(val category: Category, val score: Int? = null)

    data class Scorecard(
        val ones: Box = Box(Category.ONES),
        val twos: Box = Box(Category.TWOS),
        val threes: Box = Box(Category.THREES),
        val fours: Box = Box(Category.FOURS),
        val fives: Box = Box(Category.FIVES),
        val sixes: Box = Box(Category.SIXES),
        val sumBonus: Int? = null,
        val threeKind: Box = Box(Category.THREE_OF_KIND),
        val fourKind: Box = Box(Category.FOUR_OF_KIND),
        val fullHouse: Box = Box(Category.FULL_HOUSE),
        val smallStraight: Box = Box(Category.SMALL_STRAIGHT),
        val largeStraight: Box = Box(Category.LARGE_STRAIGHT),
        val chance: Box = Box(Category.CHANCE),
        val yahtzee: Box = Box(Category.YAHTZEE),
        val yahtzeeBonus: Int? = null,
        val total: Int? = null
    )

    private var turnsLeft = TOTAL_TURNS
    private var sumBonus = 0
    private var yahtzeeBonus: Int? = null
    private val scorecardEntries: MutableMap<Category, Int> = mutableMapOf()
    private val scorecardFlow = MutableStateFlow(Scorecard())

    suspend fun submitScore(category: Category, dice: List<Roller.Die>) {
        if (scorecardEntries[category] != null) return // already scored

        val score = calculateScore(category, dice)
        scorecardEntries[category] = score

        // update sum for bonus
        when (category) {
            Category.ONES,
            Category.TWOS,
            Category.THREES,
            Category.FOURS,
            Category.FIVES,
            Category.SIXES -> sumBonus += score
            else -> Unit
        }

        // check for bonus yahtzee
        if (category != Category.YAHTZEE && dice.isYahtzee() && score != 0) {
            yahtzeeBonus = yahtzeeBonus?.plus(100)
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
                dice.distinct().let {
                    when (it.size) {
                        1 -> dice.isBonusYahtzee()
                        2 -> dice.groupingBy { it.value }.eachCount().filterValues { it < 4 }.size == 2
                        else -> false
                    }
                }
            ) 25 else 0
            Category.SMALL_STRAIGHT -> dice.map { it.value }.distinct().sorted().let {
                when (it.size) {
                    1 -> if (dice.isBonusYahtzee()) 30 else 0
                    4 -> if (it == listOf(1, 2, 3, 4) || it == listOf(2, 3, 4, 5) || it == listOf(3, 4, 5, 6)) {
                        30
                    } else {
                        0
                    }
                    5 -> 30
                    else -> 0
                }
            }
            Category.LARGE_STRAIGHT -> if (dice.distinctBy { it.value }.size == 5 || dice.isBonusYahtzee()) 40 else 0
            Category.CHANCE -> dice.sumOf { it.value }
            Category.YAHTZEE -> if (dice.isYahtzee()) 50 else 0
        }
    }

    private suspend fun updateScorecardFlow() {
        scorecardFlow.emit(
            Scorecard(
                ones = Box(Category.ONES, scorecardEntries[Category.ONES]),
                twos = Box(Category.TWOS, scorecardEntries[Category.TWOS]),
                threes = Box(Category.THREES, scorecardEntries[Category.THREES]),
                fours = Box(Category.FOURS, scorecardEntries[Category.FOURS]),
                fives = Box(Category.FIVES, scorecardEntries[Category.FIVES]),
                sixes = Box(Category.SIXES, scorecardEntries[Category.SIXES]),
                sumBonus = if (turnsLeft == 0) getSumBonusTotal() else null,
                threeKind = Box(Category.THREE_OF_KIND, scorecardEntries[Category.THREE_OF_KIND]),
                fourKind = Box(Category.FOUR_OF_KIND, scorecardEntries[Category.FOUR_OF_KIND]),
                fullHouse = Box(Category.FULL_HOUSE, scorecardEntries[Category.FULL_HOUSE]),
                smallStraight = Box(Category.FIVES, scorecardEntries[Category.SMALL_STRAIGHT]),
                largeStraight = Box(Category.LARGE_STRAIGHT, scorecardEntries[Category.LARGE_STRAIGHT]),
                chance = Box(Category.CHANCE, scorecardEntries[Category.CHANCE]),
                yahtzee = Box(Category.YAHTZEE, scorecardEntries[Category.YAHTZEE]),
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

    private fun List<Roller.Die>.isYahtzee() = distinctBy { it.value }.size == 1

    private fun List<Roller.Die>.isBonusYahtzee() = scorecardEntries[Category.YAHTZEE] == 50 && isYahtzee()

    private fun getSumBonusTotal() = if (sumBonus >= 63) 35 else 0

    suspend fun reset() {
        turnsLeft = TOTAL_TURNS
        sumBonus = 0
        yahtzeeBonus = null
        scorecardEntries.clear()
        scorecardFlow.emit(Scorecard())
    }

    fun observeScorecard() = scorecardFlow.asStateFlow()
}