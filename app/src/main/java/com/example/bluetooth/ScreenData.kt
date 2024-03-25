package com.example.bluetooth

import androidx.compose.ui.graphics.Color

data class ScreenData(
    val indicatorColor: Color? = null
)

sealed class State(val state: States) {
    object Initial : State(state = States.INITIAL)
    object Connected : State(state = States.CONNECTED)
    object Paired : State(state = States.PAIRED)
    object Buzzing : State(state = States.BUZZING)
    object Alarm : State(state = States.ALARM)
}

enum class States(val color: Color) {
    INITIAL(color = Color.White),
    CONNECTED(color = Color(0xFF52DCE9)),
    PAIRED(color = Color(0xFF5093A4)),
    BUZZING(color = Color(0xFF5FA450)),
    ALARM(color = Color(0xFF9E50A4))
}