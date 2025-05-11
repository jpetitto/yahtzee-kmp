package com.doximity.yahtzee

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    Box(modifier = Modifier.size(500.dp, 500.dp)) {
        var showEndGameDialog by remember(uiModel.scorecard.total) {
            mutableStateOf(uiModel.scorecard.total != null)
        }

        if (showEndGameDialog) {
            AlertDialog(
                onDismissRequest = { showEndGameDialog = false },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(156, 45, 68),
                            contentColor = Color.White
                        ),
                        onClick = uiModel.onReset
                    ) {
                        Text("Play again")
                    }
                },
                text = {
                    Text("Total score: ${uiModel.scorecard.total ?: "Unknown"}")
                }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .background(Color(41, 74, 18))
                .padding(8.dp)
                .fillMaxHeight()
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(156, 45, 68),
                    contentColor = Color.White
                ),
                onClick = uiModel.onReset
            ) {
                Text("Restart Game")
            }

            Spacer(modifier = Modifier.weight(1f))

            Row {
                ScorecardLeft(uiModel.scorecard)
                ScorecardRight(uiModel.scorecard)
            }

            Spacer(modifier = Modifier.weight(1f))

            DiceAndCup(uiModel)
        }
    }
}

@Composable
private fun RowScope.ScorecardLeft(scorecard: Scorecard) {
    Column(modifier = Modifier.weight(1f)) {
        ScoreBox("1s", scorecard.ones)
        ScoreBox("2s", scorecard.twos)
        ScoreBox("3s", scorecard.threes)
        ScoreBox("4s", scorecard.fours)
        ScoreBox("5s", scorecard.fives)
        ScoreBox("6s", scorecard.sixes)
        ExtraBox("Sum", scorecard.sum)
        ExtraBox("Bonus", scorecard.sumBonus)
    }
}

@Composable
private fun RowScope.ScorecardRight(scorecard: Scorecard) {
    Column(modifier = Modifier.weight(1f)) {
        ScoreBox("3x", scorecard.threeKind)
        ScoreBox("4x", scorecard.fourKind)
        ScoreBox("FH", scorecard.fullHouse)
        ScoreBox("SS", scorecard.smallStraight)
        ScoreBox("LS", scorecard.largeStraight)
        ScoreBox("Chance", scorecard.chance)
        ScoreBox("5x", scorecard.yahtzee)
        ExtraBox("Bonus", scorecard.yahtzeeBonus)
    }
}

@Composable
private fun ExtraBox(label: String, value: Int?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(label, color = Color.White, modifier = Modifier.weight(1f))
        Text(value?.toString().orEmpty(), color = Color.White, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ScoreBox(label: String, scoreBox: ScoreBox) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.wrapContentHeight()
    ) {
        Text(label, color = Color.White, modifier = Modifier.weight(1f).padding(8.dp))

        when (scoreBox) {
            is ScoreBox.Unfilled -> {
                Text(
                    text = scoreBox.score?.toString().orEmpty(),
                    color = Color.Black,
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(255, 254, 222))
                        .clickable { scoreBox.onFill() }
                        .padding(8.dp)
                )
            }
            is ScoreBox.Filled -> {
                Text(
                    text = scoreBox.score.toString(),
                    color = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                )
            }
            ScoreBox.Empty -> {
                Text(
                    text = "0",
                    color = Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun DiceAndCup(uiModel: YahtzeeUiModel) {
    Row {
        for (die in uiModel.dice) {
            Spacer(Modifier.weight(1f))
            Die(die, enabled = uiModel.roll.rollsLeft < 3)
        }
        Spacer(Modifier.weight(1f))
    }

    Button(
        enabled = uiModel.roll.rollsLeft > 0 && uiModel.scorecard.total == null,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(156, 45, 68),
            contentColor = Color.White
        ),
        onClick = uiModel.roll.onRoll,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Roll ${minOf(4 - uiModel.roll.rollsLeft, 3)}")
    }
}

private val diePipMappings = mapOf(
    1 to setOf(5),
    2 to setOf(3, 7),
    3 to setOf(3, 5, 7),
    4 to setOf(1, 3, 7, 9),
    5 to setOf(1, 3, 5, 7, 9),
    6 to setOf(1, 3, 4, 6, 7, 9)
)

private fun Die.showPip(location: Int): Boolean {
    return diePipMappings[value]?.contains(location) == true
}

@Composable
private fun Die(die: Die, enabled: Boolean) {
    Column(
        modifier = Modifier
            .background(if (die.selected) Color(156, 45, 60) else Color.White)
            .clickable(enabled, onClick = die.onSelect)
    ) {
        Row {
            Pip(die.showPip(1), die.selected)
            Pip(die.showPip(2), die.selected)
            Pip(die.showPip(3), die.selected)
        }
        Row {
            Pip(die.showPip(4), die.selected)
            Pip(die.showPip(5), die.selected)
            Pip(die.showPip(6), die.selected)
        }
        Row {
            Pip(die.showPip(7), die.selected)
            Pip(die.showPip(8), die.selected)
            Pip(die.showPip(9), die.selected)
        }
    }
}

@Composable
private fun Pip(visible: Boolean, selected: Boolean) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(12.dp)
            .clip(CircleShape)
            .background(
                when {
                    visible.not() -> Color.Transparent
                    selected -> Color.White
                    else -> Color.Black
                }
            )
    )
}
