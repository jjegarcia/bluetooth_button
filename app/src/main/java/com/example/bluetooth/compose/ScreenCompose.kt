package com.example.bluetooth.compose


import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.bluetooth.MainViewModel
import com.example.bluetooth.State
import com.example.bluetooth.ui.theme.BluetoothTheme

@Composable
fun MainScreen(viewModel: MainViewModel) {
    BluetoothTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val infiniteTransition = rememberInfiniteTransition()
            val flashAnimation by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    tween(300),
                    repeatMode = RepeatMode.Reverse
                )
            )

            val screenData = viewModel.screenDataFlow.collectAsState().value
            Column(
                modifier =
                Modifier.background(
                    color = if (screenData?.indicatorColor == State.Buzzing.state.color)
                        screenData.indicatorColor.copy(flashAnimation)
                    else
                        screenData?.indicatorColor ?: State.Initial.state.color
                )
            )
            {

                Button(onClick = { viewModel.stopScanning() }) {
                    Text(text = "Stop Scanning")
                }
                Button(onClick = { viewModel.connectBle() }) {
                    Text(text = "Connect Push Button")
                }
                Button(onClick = { viewModel.requestNotify() }) {
                    Text(text = "Pair Push Button")
                }
                Button(onClick = { viewModel.requestReset() }) {
                    Text(text = "Cancel Alert")
                }
                Button(onClick = { viewModel.sendToForeground() }) {
                    Text(text = "Foreground App")
                }
            }
        }
    }
}

