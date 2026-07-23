package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomCard
import com.example.ui.components.CustomOutlinedButton
import com.example.utils.DateUtils
import com.example.utils.LanguageManager
import com.example.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    username: String,
    displayName: String,
    isPersian: Boolean,
    onNavigateToTimer: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val totalStudy by homeViewModel.totalStudyMinutes.collectAsState()
    val todayStudy by homeViewModel.todayStudyMinutes.collectAsState()
    val streak by homeViewModel.streakDays.collectAsState()
    val recentSessions by homeViewModel.recentSessions.collectAsState()
    val announcement by homeViewModel.announcement.collectAsState()
    val konkurCountdown by homeViewModel.konkurCountdown.collectAsState()
    val isOnline by homeViewModel.isOnline.collectAsState()

    LaunchedEffect(username) {
        homeViewModel.startSyncLoop(username)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // User Profile Header Card
        CustomCard(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayName.ifEmpty { username },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (username.equals("ARMIN", ignoreCase = true) || username.equals("DIYAKO", ignoreCase = true)) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_verified),
                                contentDescription = "Verified",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "@$username",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Online indicator circle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isOnline) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) Color(0xFF4CAF50) else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) LanguageManager.getString("online", isPersian) else LanguageManager.getString("offline", isPersian),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOnline) Color(0xFF4CAF50) else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Konkur Countdown Card
        CustomCard(
            containerColor = MaterialTheme.colorScheme.surface,
            borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = LanguageManager.getString("konkur_countdown", isPersian),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CountdownUnit("${konkurCountdown.days}", LanguageManager.getString("days", isPersian))
                    CountdownUnit("${konkurCountdown.hours}", LanguageManager.getString("hours", isPersian))
                    CountdownUnit("${konkurCountdown.minutes}", LanguageManager.getString("minutes", isPersian))
                    CountdownUnit("${konkurCountdown.seconds}", "ثانیه")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Stat Cards Grid
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = LanguageManager.getString("total_study", isPersian),
                value = DateUtils.formatDuration(totalStudy, isPersian),
                iconRes = R.drawable.ic_history,
                iconTint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                title = LanguageManager.getString("today_study", isPersian),
                value = DateUtils.formatDuration(todayStudy, isPersian),
                iconRes = R.drawable.ic_studying,
                iconTint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = LanguageManager.getString("streak", isPersian),
                value = "$streak ${LanguageManager.getString("days", isPersian)}",
                iconRes = R.drawable.ic_gold,
                iconTint = Color(0xFFFFB300),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                title = LanguageManager.getString("timer", isPersian),
                value = "00:00",
                iconRes = R.drawable.ic_timer,
                iconTint = Color(0xFF00BCD4),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3 Rounded Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CustomButton(
                onClick = onNavigateToTimer,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_timer),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(LanguageManager.getString("timer", isPersian), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.width(8.dp))

            CustomButton(
                onClick = onNavigateToReport,
                containerColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_report),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(LanguageManager.getString("add_report", isPersian), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.width(8.dp))

            CustomOutlinedButton(
                onClick = onNavigateToHistory,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_history),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(LanguageManager.getString("history", isPersian), style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Announcement Card
        announcement?.let { ann ->
            CustomCard(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ) {
                Column {
                    Text(
                        text = ann.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ann.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    ann.button?.let { btn ->
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomButton(
                            onClick = {
                                if (btn.url.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(btn.url))
                                    context.startActivity(intent)
                                }
                            },
                            height = 38.dp
                        ) {
                            Text(btn.text, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Recent Sessions (Horizontal List)
        Text(
            text = LanguageManager.getString("recent_sessions", isPersian),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (recentSessions.isEmpty()) {
            CustomCard {
                Text(
                    text = if (isPersian) "هیچ جلسه مطالعاتی ثبت نشده است." else "No recent study sessions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recentSessions) { session ->
                    Card(
                        modifier = Modifier.width(220.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = session.book,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = session.topic,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = DateUtils.formatDuration(session.duration, isPersian),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "${session.testCount} ${LanguageManager.getString("test_count", isPersian)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownUnit(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    iconRes: Int,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
