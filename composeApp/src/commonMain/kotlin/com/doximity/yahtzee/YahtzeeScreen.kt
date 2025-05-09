package com.doximity.yahtzee

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.doximity.yahtzee.YahtzeeUiModel.Die
import com.doximity.yahtzee.YahtzeeUiModel.ScoreBox
import com.doximity.yahtzee.YahtzeeUiModel.Scorecard

@Composable
fun YahtzeeScreen() {
    val presenter = YahtzeePresenter(Roller(), Scorer())
    YahtzeeContent(presenter)
}

@Composable
private fun YahtzeeContent(presenter: YahtzeePresenter) {
    val uiModel = presenter.present()

    Column {
        Row {
            ScorecardLeft(uiModel.scorecard)
            ScorecardRight(uiModel.scorecard)
        }

        DiceAndCup(uiModel)

        TextButton(onClick = uiModel.onReset) {
            Text("Restart Game")
        }
    }
}

@Composable
private fun ScorecardLeft(scorecard: Scorecard) {
    Column {
        ScoreBox("1s", scorecard.ones)
        ScoreBox("2s", scorecard.twos)
        ScoreBox("3s", scorecard.threes)
        ScoreBox("4s", scorecard.fours)
        ScoreBox("5s", scorecard.fives)
        ScoreBox("6s", scorecard.sixes)
    }
}

@Composable
private fun ScorecardRight(scorecard: Scorecard) {
    Column {
        ScoreBox("3x", scorecard.threeKind)
        ScoreBox("4x", scorecard.fourKind)
        ScoreBox("FH", scorecard.fullHouse)
        ScoreBox("SS", scorecard.smallStraight)
        ScoreBox("LS", scorecard.largeStraight)
        ScoreBox("Ch", scorecard.chance)
        ScoreBox("5x", scorecard.yahtzee)
    }
}

@Composable
private fun ScoreBox(label: String, scoreBox: ScoreBox) {
    Row {
        Text(label)

        when (scoreBox) {
            is ScoreBox.Empty -> {
                TextButton(onClick = scoreBox.onFill) {
                    Text(scoreBox.score?.toString().orEmpty())
                }
            }
            is ScoreBox.Filled -> Text(scoreBox.score.toString())
        }
    }
}

@Composable
private fun DiceAndCup(uiModel: YahtzeeUiModel) {
    Row {
        for (die in uiModel.dice) {
            Die(die)
        }
    }

    TextButton(
        enabled = uiModel.roll.rollsLeft > 0,
        onClick = uiModel.roll.onRoll
    ) {
        Text("Rolls left: ${uiModel.roll.rollsLeft}")
    }
}

@Composable
private fun Die(die: Die) {
    TextButton(onClick = die.onSelect) {
        Text(
            text = die.value.toString(),
            color = if (die.selected) Color.Green else Color.Black
        )
    }
}
