package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomTextField
import com.example.ui.components.CustomTopBar
import com.example.utils.LanguageManager
import com.example.viewmodel.ReportViewModel
import com.example.viewmodel.SaveReportState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    reportViewModel: ReportViewModel,
    username: String,
    initialDurationMinutes: Int = 0,
    isPersian: Boolean,
    snackbarHostState: SnackbarHostState,
    onReportSubmitted: () -> Unit
) {
    var book by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var durationStr by remember { mutableStateOf(if (initialDurationMinutes > 0) initialDurationMinutes.toString() else "") }
    var testCountStr by remember { mutableStateOf("") }
    var correctCountStr by remember { mutableStateOf("") }
    var wrongCountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // Dropdown for grade
    val gradeOptions = listOf("دهم", "یازدهم", "دوازدهم", "پشت کنکوری", "فارغ‌التحصیل")
    var selectedGrade by remember { mutableStateOf(gradeOptions[2]) }
    var isGradeExpanded by remember { mutableStateOf(false) }

    // Field Errors
    var bookError by remember { mutableStateOf<String?>(null) }
    var topicError by remember { mutableStateOf<String?>(null) }
    var durationError by remember { mutableStateOf<String?>(null) }

    val saveState by reportViewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveReportState.Success -> {
                snackbarHostState.showSnackbar(LanguageManager.getString("report_saved", isPersian))
                reportViewModel.resetSaveState()
                onReportSubmitted()
            }
            is SaveReportState.ValidationError -> {
                val err = saveState as SaveReportState.ValidationError
                when (err.field) {
                    "book" -> bookError = LanguageManager.getString(err.messageKey, isPersian)
                    "topic" -> topicError = LanguageManager.getString(err.messageKey, isPersian)
                    "duration" -> durationError = LanguageManager.getString(err.messageKey, isPersian)
                }
                reportViewModel.resetSaveState()
            }
            is SaveReportState.Error -> {
                val msg = (saveState as SaveReportState.Error).message
                snackbarHostState.showSnackbar(msg)
                reportViewModel.resetSaveState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopBar(title = LanguageManager.getString("add_report", isPersian))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CustomTextField(
                value = book,
                onValueChange = {
                    book = it
                    bookError = null
                },
                label = LanguageManager.getString("book_name", isPersian),
                errorMessage = bookError
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = topic,
                onValueChange = {
                    topic = it
                    topicError = null
                },
                label = LanguageManager.getString("topic", isPersian),
                errorMessage = topicError
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grade Dropdown Menu
            ExposedDropdownMenuBox(
                expanded = isGradeExpanded,
                onExpandedChange = { isGradeExpanded = !isGradeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedGrade,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(LanguageManager.getString("grade", isPersian)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGradeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isGradeExpanded,
                    onDismissRequest = { isGradeExpanded = false }
                ) {
                    gradeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedGrade = option
                                isGradeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = durationStr,
                onValueChange = {
                    durationStr = it
                    durationError = null
                },
                label = LanguageManager.getString("duration_minutes", isPersian),
                errorMessage = durationError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                CustomTextField(
                    value = testCountStr,
                    onValueChange = { testCountStr = it },
                    label = LanguageManager.getString("test_count", isPersian),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                CustomTextField(
                    value = correctCountStr,
                    onValueChange = { correctCountStr = it },
                    label = LanguageManager.getString("correct_count", isPersian),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                CustomTextField(
                    value = wrongCountStr,
                    onValueChange = { wrongCountStr = it },
                    label = LanguageManager.getString("wrong_count", isPersian),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = note,
                onValueChange = { note = it },
                label = LanguageManager.getString("notes", isPersian),
                isSingleLine = false,
                modifier = Modifier.height(100.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            CustomButton(
                onClick = {
                    reportViewModel.submitReport(
                        username = username,
                        book = book,
                        topic = topic,
                        grade = selectedGrade,
                        durationStr = durationStr,
                        testCountStr = testCountStr,
                        correctCountStr = correctCountStr,
                        wrongCountStr = wrongCountStr,
                        note = note
                    )
                },
                enabled = saveState !is SaveReportState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(LanguageManager.getString("submit_report", isPersian))
            }
        }
    }
}
