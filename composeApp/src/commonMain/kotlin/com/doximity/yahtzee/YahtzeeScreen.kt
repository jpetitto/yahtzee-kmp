package com.doximity.yahtzee

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

    Column(modifier = Modifier.background(Color(91, 156, 45))) {
        Row(modifier = Modifier.fillMaxWidth()) {
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
private fun RowScope.ScorecardLeft(scorecard: Scorecard) {
    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
        ScoreBox("Ones", scorecard.ones)
        ScoreBox("Twos", scorecard.twos)
        ScoreBox("Threes", scorecard.threes)
        ScoreBox("Fours", scorecard.fours)
        ScoreBox("Fives", scorecard.fives)
        ScoreBox("Sixes", scorecard.sixes)
        ExtraBox("Sum", scorecard.sum)
        ExtraBox("Bonus", scorecard.sumBonus)
    }
}

@Composable
private fun RowScope.ScorecardRight(scorecard: Scorecard) {
    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
        ScoreBox("Three of a kind", scorecard.threeKind)
        ScoreBox("Four of a kind", scorecard.fourKind)
        ScoreBox("Full House", scorecard.fullHouse)
        ScoreBox("Small Straight", scorecard.smallStraight)
        ScoreBox("Large Straight", scorecard.largeStraight)
        ScoreBox("Chance", scorecard.chance)
        ScoreBox("Yahtzee!", scorecard.yahtzee)
        ExtraBox("Bonus", scorecard.yahtzeeBonus)
    }
}

@Composable
private fun ExtraBox(label: String, value: Int?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(16, 82, 25))
            .padding(8.dp)
            .fillMaxWidth(0.5f)
    ) {
        Text(label, color = Color.White, modifier = Modifier.weight(1f))

        TextButton({}, enabled = false) {
            Text(value?.toString().orEmpty(), color = Color.White)
        }
    }
}

@Composable
private fun ScoreBox(label: String, scoreBox: ScoreBox) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
//            .border(1.dp, Color.White, shape = RoundedCornerShape(4.dp))
            .padding(8.dp)
            .fillMaxWidth(0.5f)
    ) {
        Text(label, color = Color.White, modifier = Modifier.weight(1f))

        when (scoreBox) {
            is ScoreBox.Unfilled -> {
                TextButton(
                    onClick = scoreBox.onFill,
                    modifier = Modifier.background(Color(156, 45, 60))
                ) {
                    Text(scoreBox.score?.toString().orEmpty(), color = Color.White)
                }
            }
            is ScoreBox.Filled -> TextButton({}, enabled = false) {
                Text(scoreBox.score.toString(), color = Color.White)
            }
            ScoreBox.Empty -> TextButton({}, enabled = false) {}
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
    OutlinedButton(
        enabled = enabled,
        onClick = die.onSelect,
        border = BorderStroke(4.dp, if (die.selected) Color(156, 45, 60) else Color.Transparent),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(255, 252, 240),
            contentColor = Color.Black,
            disabledContentColor = Color.Black
        ),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Text(
            text = die.value.toString()
        )
    }
}
