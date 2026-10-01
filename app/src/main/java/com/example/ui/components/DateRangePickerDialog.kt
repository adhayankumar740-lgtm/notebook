package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.DateFilterType
import com.example.ui.viewmodel.DateRangePreset
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DateRangePickerDialog(
    currentPreset: DateRangePreset,
    currentDateType: DateFilterType,
    customStartMillis: Long?,
    customEndMillis: Long?,
    onDismiss: () -> Unit,
    onApply: (preset: DateRangePreset, dateType: DateFilterType, customStart: Long?, customEnd: Long?) -> Unit
) {
    var selectedPreset by remember { mutableStateOf(currentPreset) }
    var selectedDateType by remember { mutableStateOf(currentDateType) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var startDateText by remember {
        val date = customStartMillis?.let { Date(it) } ?: Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000)
        mutableStateOf(dateFormat.format(date))
    }

    var endDateText by remember {
        val date = customEndMillis?.let { Date(it) } ?: Date()
        mutableStateOf(dateFormat.format(date))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Filter by Date Range", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Toggle: Upload Date vs Lecture Date
                Text(
                    text = "Apply Date Filter To:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                TabRow(
                    selectedTabIndex = if (selectedDateType == DateFilterType.CREATION_DATE) 0 else 1
                ) {
                    Tab(
                        selected = selectedDateType == DateFilterType.CREATION_DATE,
                        onClick = { selectedDateType = DateFilterType.CREATION_DATE },
                        text = { Text("Upload Date", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedDateType == DateFilterType.LECTURE_DATE,
                        onClick = { selectedDateType = DateFilterType.LECTURE_DATE },
                        text = { Text("Lecture Date", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Presets list
                Text(
                    text = "Timeframe Range:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                for (preset in DateRangePreset.values()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPreset = preset }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPreset == preset,
                            onClick = { selectedPreset = preset }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = preset.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedPreset == preset) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // If Custom is selected, show Start & End inputs
                if (selectedPreset == DateRangePreset.CUSTOM) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Enter Dates (YYYY-MM-DD):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = startDateText,
                                onValueChange = { startDateText = it },
                                label = { Text("From Date") },
                                placeholder = { Text("2026-09-01") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = endDateText,
                                onValueChange = { endDateText = it },
                                label = { Text("To Date") },
                                placeholder = { Text("2026-10-31") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var startMillis: Long? = null
                    var endMillis: Long? = null
                    if (selectedPreset == DateRangePreset.CUSTOM) {
                        try {
                            val parsedStart = dateFormat.parse(startDateText.trim())
                            val parsedEnd = dateFormat.parse(endDateText.trim())
                            startMillis = parsedStart?.time
                            // End of that day
                            endMillis = parsedEnd?.let { it.time + (24 * 60 * 60 * 1000 - 1) }
                        } catch (_: Exception) {}
                    }
                    onApply(selectedPreset, selectedDateType, startMillis, endMillis)
                },
                modifier = Modifier.testTag("apply_date_filter_button")
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    // Reset to All Time
                    onApply(DateRangePreset.ALL_TIME, DateFilterType.CREATION_DATE, null, null)
                }
            ) {
                Text("Reset")
            }
        }
    )
}
