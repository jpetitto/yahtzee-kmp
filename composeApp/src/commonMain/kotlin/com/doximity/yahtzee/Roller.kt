package com.doximity.yahtzee

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class Roller {

    private val initialDice = listOf(
        Die(value = 1, selected = false),
        Die(value = 2, selected = false),
        Die(value = 3, selected = false),
        Die(value = 4, selected = false),
        Die(value = 5, selected = false)
    )

    data class Die(val value: Int, val selected: Boolean)

    private val diceFlow = MutableStateFlow(initialDice)

    suspend fun selectDie(position: Int) {
        diceFlow.emit(
            diceFlow.value.mapIndexed { index, die ->
                die.copy(selected = if (index == position) die.selected.not() else die.selected)
            }
        )
    }

    suspend fun roll() {
        diceFlow.emit(
            diceFlow.value.map {
                if (it.selected.not()) {
                    it.copy(value = Random.nextInt(6) + 1)
                } else {
                    it
                }
            }
        )
    }

    suspend fun reset() {
        diceFlow.emit(initialDice)
    }

    fun observeDice() = diceFlow.asStateFlow()
}