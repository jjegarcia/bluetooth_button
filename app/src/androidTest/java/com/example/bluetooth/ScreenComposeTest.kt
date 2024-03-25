package com.example.bluetooth

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.bluetooth.compose.MainScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val STOP_BUTTON_TEXT = "Stop Scan"
private const val CONNECT_BUTTON_TEXT = "Connect"
private const val REQUEST_NOTIFY_BUTTON_TEXT = "Request Notify"
private const val REQUEST_RESET_BUTTON_TEXT = "Reset"
private const val TEST_BUTTON_TEXT = "Test"

class ScreenComposeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        composeTestRule.setContent { MainScreen(viewModel = MainViewModel(BluetoothComponent())) }

    }

    @Test
    fun Given_screen_opens_Then_show_all_componets() {
        composeTestRule.onNodeWithText(STOP_BUTTON_TEXT).assertExists()
        composeTestRule.onNodeWithText(CONNECT_BUTTON_TEXT).assertExists()
        composeTestRule.onNodeWithText(REQUEST_NOTIFY_BUTTON_TEXT).assertExists()
        composeTestRule.onNodeWithText(REQUEST_RESET_BUTTON_TEXT).assertExists()
        composeTestRule.onNodeWithText(TEST_BUTTON_TEXT).assertExists()
    }


}