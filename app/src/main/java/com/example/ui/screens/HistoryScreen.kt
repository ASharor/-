package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.models.Report
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomCard
import com.example.ui.components.CustomTopBar
import com.example.utils.DateUtils
import com.example.utils.LanguageManager
import com.example.viewmodel.ReportViewModel

@Composable
fun HistoryScreen(
    reportViewModel: ReportViewModel,
    username: String,
    isPersian: Boolean
) {
    val context = LocalContext.current
    val reports by reportViewModel.allReports.collectAsState()
    val weeklyChart by reportViewModel.weeklyStudyMinutes.collectAsState()

    var reportToDelete by remember { mutableStateOf<Report?>(null) }

    LaunchedEffect(username) {
        reportViewModel.loadReports(username)
    }

    // Delete Confirmation Dialog
    if (reportToDelete != null) {
        AlertDialog(
            onDismissRequest = { reportToDelete = null },
            title = { Text(LanguageManager.getString("delete_confirm", isPersian)) },
            text = { Text("${reportToDelete?.book} - ${reportToDelete?.topic}") },
            confirmButton = {
                TextButton(onClick = {
                    reportToDelete?.let { reportViewModel.deleteReport(username, it.id) }
                    reportToDelete = null
                }) {
                    Text(LanguageManager.getString("confirm", isPersian), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { reportToDelete = null }) {
                    Text(LanguageManager.getString("cancel", isPersian))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopBar(title = LanguageManager.getString("history", isPersian))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 7-day Bar Chart using Canvas
            item {
                CustomCard {
                    Text(
                        text = LanguageManager.getString("weekly_chart", isPersian),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val primaryColor = MaterialTheme.colorScheme.primary
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        val maxVal = (weeklyChart.maxOrNull() ?: 1).coerceAtLeast(60)
                        val barWidth = size.width / 15f
                        val gap = size.width / 8f

                        weeklyChart.forEachIndexed { i, mins ->
                            val barHeight = (mins.toFloat() / maxVal.toFloat()) * size.height * 0.8f
                            val x = (i + 0.5f) * gap
                            val y = size.height - barHeight

                            drawRoundRect(
                                color = primaryColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Summary Totals & Share Section
            item {
                val totalDuration = reports.sumOf { it.duration }
                val totalTests = reports.sumOf { it.testCount }
                val totalCorrect = reports.sumOf { it.correctCount }
                val totalWrong = reports.sumOf { it.wrongCount }

                CustomCard(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                ) {
                    Column {
                        Text(
                            text = LanguageManager.getString("daily_summary", isPersian),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${LanguageManager.getString("total_study", isPersian)}: ${DateUtils.formatDuration(totalDuration, isPersian)}")
                            Text("${LanguageManager.getString("test_count", isPersian)}: $totalTests")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${LanguageManager.getString("correct_count", isPersian)}: $totalCorrect", color = Color(0xFF4CAF50))
                            Text("${LanguageManager.getString("wrong_count", isPersian)}: $totalWrong", color = Color(0xFFF44336))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        CustomButton(
                            onClick = {
                                val summaryText = "گزارش مطالعه پارتینو کاربر $username:\n" +
                                        "مجموع زمان: ${DateUtils.formatDuration(totalDuration, isPersian)}\n" +
                                        "تعداد تست: $totalTests (درست: $totalCorrect | نادرست: $totalWrong)"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, summaryText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری گزارش"))
                            },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_logo), contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(LanguageManager.getString("share", isPersian))
                        }
                    }
                }
            }

            // Reports Header
            item {
                Text(
                    text = LanguageManager.getString("all_reports", isPersian),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Report Cards List
            if (reports.isEmpty()) {
                item {
                    CustomCard {
                        Text(
                            text = if (isPersian) "هیچ گزارشی ثبت نشده است." else "No study reports found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(reports) { report ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = report.book,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${report.topic} (${report.grade})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(onClick = { reportToDelete = report }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_logo),
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = DateUtils.formatDuration(report.duration, isPersian),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "تست: ${report.testCount} (درست: ${report.correctCount} | غلط: ${report.wrongCount})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (report.note.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = report.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = report.date,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }
    }
}
