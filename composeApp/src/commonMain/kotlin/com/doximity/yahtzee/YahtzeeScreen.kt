package com.doximity.yahtzee

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(41, 74, 18))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .sizeIn(maxWidth = 500.dp, maxHeight = 600.dp)
                .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Total score: ${uiModel.scorecard.total ?: ""}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(156, 45, 68),
                        contentColor = Color.White
                    ),
                    onClick = uiModel.onReset
                ) {
                    Text(if (uiModel.scorecard.total != null) "Play Again" else "Restart Game")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.wrapContentWidth()) {
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
    Column(modifier = Modifier.weight(1f)) {
        ScoreBox("3-kind", scorecard.threeKind)
        ScoreBox("4-kind", scorecard.fourKind)
        ScoreBox("Full House", scorecard.fullHouse)
        ScoreBox("Sm. Straight", scorecard.smallStraight)
        ScoreBox("Lg. Straight", scorecard.largeStraight)
        ScoreBox("Chance", scorecard.chance)
        ScoreBox("Yahtzee", scorecard.yahtzee)
        ExtraBox("Bonus", scorecard.yahtzeeBonus)
    }
}

@Composable
private fun ScoreBox(label: String, scoreBox: ScoreBox) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.wrapContentHeight()
    ) {
        Text(
            text = label,
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        )

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
                    fontWeight = FontWeight.Bold,
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
private fun ExtraBox(label: String, value: Int?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.wrapContentHeight()
    ) {
        Text(
            text = label,
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        )

        Text(
            text = value?.toString().orEmpty(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        )
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
