package com.example.bluetooth

import com.example.bluetooth.State.*

class StateMachine : StateMachineI {
    private var state: State = Initial
    override fun setState(targetState: State, block: (() -> Unit?)?) {
        when {
            state == Initial && targetState == Connected -> {}
            state == Connected && targetState == Paired -> {}
            state == Paired && targetState == Buzzing -> {}
            state == Buzzing && targetState == Alarm -> {}
            (state == Buzzing || state == Alarm) && targetState == Initial -> {}
            else -> return
        }
        state = targetState
        block?.let { run(block) }
    }

    override fun fetchState(): State = state
    override fun saveState(newState: State) {
        state = newState
    }
}