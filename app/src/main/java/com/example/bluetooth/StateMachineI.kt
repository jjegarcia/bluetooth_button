package com.example.bluetooth

interface StateMachineI {
    fun setState(targetState: State, block: (() -> Unit?)? = null)
    fun fetchState(): State
    fun saveState(newState: State)
}
