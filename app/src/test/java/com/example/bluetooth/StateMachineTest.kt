package com.example.bluetooth

import com.example.bluetooth.State.*
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Test

class StateMachineTest {
    private val mockBlock: () -> Unit = mockk()
    val subject = StateMachine()

    @Test
    fun `Given current state is Alarm When target is Buzzing Then run block not invoked`() {
        every { mockBlock.invoke() } just Runs

        subject.saveState(Alarm)
        subject.setState(targetState = Buzzing, block = mockBlock)

        assertEquals(Alarm, subject.fetchState())
        verify(exactly = 0) { run(mockBlock) }
    }

    @Test
    fun `Given current state is default(Initial)  When target is Buzzing Then run block`() {
        every { mockBlock.invoke() } just Runs

        subject.setState(targetState = Buzzing, block = mockBlock)

        assertEquals(Buzzing, subject.fetchState())
        verify { run(mockBlock) }
    }

    @Test
    fun `Given current state is Buzzing  When target is Alarm Then run block`() {
        every { mockBlock.invoke() } just Runs

        subject.saveState(Buzzing)
        subject.setState(targetState = Alarm, block = mockBlock)

        assertEquals(Alarm, subject.fetchState())
        verify { run(mockBlock) }
    }

    @Test
    fun `Given current state is Buzzing  When target is Initial Then run block`() {
        every { mockBlock.invoke() } just Runs

        subject.saveState(Buzzing)
        subject.setState(targetState = Initial, block = mockBlock)

        assertEquals(Initial, subject.fetchState())
        verify { run(mockBlock) }
    }
}
