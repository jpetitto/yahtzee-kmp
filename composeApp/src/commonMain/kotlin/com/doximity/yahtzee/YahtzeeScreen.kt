package com.doximity.yahtzee

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

        Text("Total score: ${uiModel.scorecard.total ?: ""}")

        DiceAndCup(uiModel)

        TextButton(onClick = uiModel.onReset) {
            Text("Restart Game")
        }
    }
}

@Composable
private fun ScorecardLeft(scorecard: Scorecard) {
    Column {
        ScoreBox("Ones", scorecard.ones)
        ScoreBox("Twos", scorecard.twos)
        ScoreBox("Threes", scorecard.threes)
        ScoreBox("Fours", scorecard.fours)
        ScoreBox("Fives", scorecard.fives)
        ScoreBox("Sixes", scorecard.sixes)
        Text("Sum: ${scorecard.sum ?: ""}")
        Text("Bonus: ${scorecard.sumBonus ?: ""}")
    }
}

@Composable
private fun ScorecardRight(scorecard: Scorecard) {
    Column {
        ScoreBox("Three of a kind", scorecard.threeKind)
        ScoreBox("Four of a kind", scorecard.fourKind)
        ScoreBox("Full House", scorecard.fullHouse)
        ScoreBox("Small Straight", scorecard.smallStraight)
        ScoreBox("Large Straight", scorecard.largeStraight)
        ScoreBox("Chance", scorecard.chance)
        ScoreBox("Yahtzee!", scorecard.yahtzee)
        Text("Bonus: ${scorecard.yahtzeeBonus ?: ""}")
    }
}

@Composable
private fun ScoreBox(label: String, scoreBox: ScoreBox) {
    Row {
        Text(label)

        when (scoreBox) {
            is ScoreBox.Unfilled -> {
                TextButton(onClick = scoreBox.onFill) {
                    Text(scoreBox.score?.toString().orEmpty())
                }
            }
            is ScoreBox.Filled -> Text(scoreBox.score.toString())
            ScoreBox.Empty -> Unit
        }
    }
}

@Composable
private fun DiceAndCup(uiModel: YahtzeeUiModel) {
    Row {
        for (die in uiModel.dice) {
            Die(die, enabled = uiModel.roll.rollsLeft < 3)
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
private fun Die(die: Die, enabled: Boolean) {
    Button(
        enabled = enabled,
        onClick = die.onSelect,
        modifier = Modifier.padding(end = 2.dp)
    ) {
        Text(
            text = die.value.toString(),
            color = if (die.selected) Color.Green else Color.White
        )
    }
}
