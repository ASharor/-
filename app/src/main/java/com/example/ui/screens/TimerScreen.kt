package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomCard
import com.example.ui.components.CustomOutlinedButton
import com.example.ui.components.CustomTopBar
import com.example.utils.DateUtils
import com.example.utils.LanguageManager
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(
    isPersian: Boolean,
    onRecordToReport: (Int) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Stopwatch State
    var stopwatchSeconds by remember { mutableLongStateOf(0L) }
    var isStopwatchRunning by remember { mutableStateOf(false) }

    // Countdown State
    var targetCountdownMinutes by remember { mutableStateOf(25) }
    var countdownSecondsLeft by remember { mutableLongStateOf(25 * 60L) }
    var isCountdownRunning by remember { mutableStateOf(false) }

    // Coroutine ticker for Stopwatch
    LaunchedEffect(isStopwatchRunning) {
        while (isStopwatchRunning) {
            delay(1000)
            stopwatchSeconds++
        }
    }

    // Coroutine ticker for Countdown
    LaunchedEffect(isCountdownRunning) {
        while (isCountdownRunning && countdownSecondsLeft > 0) {
            delay(1000)
            countdownSecondsLeft--
            if (countdownSecondsLeft == 0L) {
                isCountdownRunning = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopBar(title = LanguageManager.getString("timer", isPersian))

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text(LanguageManager.getString("stopwatch", isPersian)) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text(LanguageManager.getString("countdown", isPersian)) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (selectedTabIndex == 0) {
            // STOPWATCH TAB
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = DateUtils.formatSeconds(stopwatchSeconds),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 42.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CustomButton(
                        onClick = { isStopwatchRunning = !isStopwatchRunning },
                        containerColor = if (isStopwatchRunning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (isStopwatchRunning) LanguageManager.getString("pause", isPersian)
                            else LanguageManager.getString("start", isPersian)
                        )
                    }

                    CustomOutlinedButton(
                        onClick = {
                            isStopwatchRunning = false
                            stopwatchSeconds = 0L
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(LanguageManager.getString("reset", isPersian))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                CustomButton(
                    onClick = {
                        val minutes = (stopwatchSeconds / 60).toInt().coerceAtLeast(1)
                        onRecordToReport(minutes)
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_report),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageManager.getString("record_in_report", isPersian))
                }
            }
        } else {
            // COUNTDOWN TAB
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = DateUtils.formatSeconds(countdownSecondsLeft),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 42.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!isCountdownRunning) {
                    Text(
                        text = "${LanguageManager.getString("duration_minutes", isPersian)}: $targetCountdownMinutes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Slider(
                        value = targetCountdownMinutes.toFloat(),
                        onValueChange = {
                            targetCountdownMinutes = it.toInt()
                            countdownSecondsLeft = targetCountdownMinutes * 60L
                        },
                        valueRange = 5f..120f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CustomButton(
                        onClick = { isCountdownRunning = !isCountdownRunning },
                        containerColor = if (isCountdownRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (isCountdownRunning) LanguageManager.getString("pause", isPersian)
                            else LanguageManager.getString("start", isPersian)
                        )
                    }

                    CustomOutlinedButton(
                        onClick = {
                            isCountdownRunning = false
                            countdownSecondsLeft = targetCountdownMinutes * 60L
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(LanguageManager.getString("reset", isPersian))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                CustomButton(
                    onClick = {
                        val elapsedMinutes = ((targetCountdownMinutes * 60L - countdownSecondsLeft) / 60).toInt().coerceAtLeast(1)
                        onRecordToReport(elapsedMinutes)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_report),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageManager.getString("record_in_report", isPersian))
                }
            }
        }
    }
}
