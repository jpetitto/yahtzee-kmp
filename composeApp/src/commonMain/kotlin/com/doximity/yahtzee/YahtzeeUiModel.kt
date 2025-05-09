package com.doximity.yahtzee

data class YahtzeeUiModel(
    val scorecard: Scorecard,
    val dice: List<Die>,
    val roll: Roll,
    val onReset: () -> Unit
) {

    sealed interface ScoreBox {
        data object Empty : ScoreBox
        data class Unfilled(val score: Int?, val onFill: () -> Unit) : ScoreBox
        data class Filled(val score: Int) : ScoreBox
    }

    data class Scorecard(
        val ones: ScoreBox,
        val twos: ScoreBox,
        val threes: ScoreBox,
        val fours: ScoreBox,
        val fives: ScoreBox,
        val sixes: ScoreBox,
        val sum: Int?,
        val sumBonus: Int?,
        val threeKind: ScoreBox,
        val fourKind: ScoreBox,
        val fullHouse: ScoreBox,
        val smallStraight: ScoreBox,
        val largeStraight: ScoreBox,
        val chance: ScoreBox,
        val yahtzee: ScoreBox,
        val yahtzeeBonus: Int?,
        val total: Int?
    )

    data class Die(val value: Int, val selected: Boolean, val onSelect: () -> Unit)

    data class Roll(val rollsLeft: Int, val onRoll: () -> Unit)
}
