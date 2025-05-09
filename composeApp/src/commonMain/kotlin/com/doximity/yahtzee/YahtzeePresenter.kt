package com.doximity.yahtzee

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.doximity.yahtzee.YahtzeeUiModel.Die
import com.doximity.yahtzee.YahtzeeUiModel.Roll
import com.doximity.yahtzee.YahtzeeUiModel.ScoreBox
import kotlinx.coroutines.launch

private const val MAX_ROLLS = 3

class YahtzeePresenter(
    private val roller: Roller,
    private val scorer: Scorer
) {

    private var rollsLeft by mutableStateOf(MAX_ROLLS)

    @Composable
    fun present(): YahtzeeUiModel {
        val scope = rememberCoroutineScope()

        val dice by roller.observeDice().collectAsState()
        val scorecard by scorer.observeScorecard().collectAsState()

        return YahtzeeUiModel(
            scorecard = YahtzeeUiModel.Scorecard(
                ones = createScoreBox(scorecard.ones, dice),
                twos = createScoreBox(scorecard.twos, dice),
                threes = createScoreBox(scorecard.threes, dice),
                fours = createScoreBox(scorecard.fours, dice),
                fives = createScoreBox(scorecard.fives, dice),
                sixes = createScoreBox(scorecard.sixes, dice),
                sum = scorecard.sum,
                sumBonus = scorecard.sumBonus,
                threeKind = createScoreBox(scorecard.threeKind, dice),
                fourKind = createScoreBox(scorecard.fourKind, dice),
                fullHouse = createScoreBox(scorecard.fullHouse, dice),
                smallStraight = createScoreBox(scorecard.smallStraight, dice),
                largeStraight = createScoreBox(scorecard.largeStraight, dice),
                chance = createScoreBox(scorecard.chance, dice),
                yahtzee = createScoreBox(scorecard.yahtzee, dice),
                yahtzeeBonus = scorecard.yahtzeeBonus,
                total = scorecard.total,
            ),
            dice = dice.mapIndexed { index, die ->
                Die(die.value, die.selected) {
                    scope.launch {
                        roller.selectDie(index)
                    }
                }},
            roll = Roll(rollsLeft) {
                scope.launch {
                    if (rollsLeft > 0) {
                        roller.roll()
                    }

                    rollsLeft--
                }
            },
            onReset = {
                scope.launch {
                    roller.reset()
                    scorer.reset()
                    rollsLeft = MAX_ROLLS
                }
            }
        )
    }

    @Composable
    private fun createScoreBox(entry: Scorer.Entry, dice: List<Roller.Die>): ScoreBox {
        val scope = rememberCoroutineScope()

        return when {
            entry.score != null -> ScoreBox.Filled(entry.score)
            rollsLeft == MAX_ROLLS -> ScoreBox.Empty
            else -> ScoreBox.Unfilled(
                score = scorer.calculateScore(entry.category, dice),
                onFill = {
                    scope.launch {
                        scorer.submitScore(entry.category, dice)
                        roller.reset()
                        rollsLeft = MAX_ROLLS
                    }
                }
            )
        }
    }
}