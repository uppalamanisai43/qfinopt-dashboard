package com.qfinopt.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.qfinopt.app.data.model.CustomReminder
import com.qfinopt.app.data.model.ReminderFrequency
import com.qfinopt.app.data.model.ReminderType
import com.qfinopt.app.ui.theme.*

@Composable
fun AddReminderDialog(
    initialFundName: String = "",
    availableFunds: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (CustomReminder) -> Unit
) {
    var selectedType by remember { mutableStateOf(ReminderType.SIP) }
    var selectedFrequency by remember { mutableStateOf(ReminderFrequency.MONTHLY) }
    var title by remember { mutableStateOf("Monthly SIP Installment") }
    var fundName by remember { mutableStateOf(initialFundName.ifBlank { "AXIS Small Cap Fund" }) }
    var amountText by remember { mutableStateOf("5000") }
    var dayOfMonth by remember { mutableIntStateOf(5) }
    var dateString by remember { mutableStateOf("2026-10-15") }
    var notes by remember { mutableStateOf("Auto-debit via Direct Mutual Fund") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔔 Add Smart Reminder",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Preset Chips Row
                Text(
                    text = "SELECT TEMPLATE PRESET",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReminderType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            modifier = Modifier.clickable {
                                selectedType = type
                                when (type) {
                                    ReminderType.SIP -> {
                                        selectedFrequency = ReminderFrequency.MONTHLY
                                        title = "Monthly SIP Installment"
                                        amountText = "5000"
                                        notes = "Auto-debit scheduled via Direct MF"
                                    }
                                    ReminderType.EXIT -> {
                                        selectedFrequency = ReminderFrequency.ONCE
                                        title = "Optimal Profit Booking Target"
                                        amountText = "50000"
                                        notes = "AI optimal exit target to lock gains"
                                    }
                                    ReminderType.PRICE_TARGET -> {
                                        selectedFrequency = ReminderFrequency.ONCE
                                        title = "NAV Target / Dip Entry Alert"
                                        amountText = "10000"
                                        notes = "Alert when fund reaches entry target"
                                    }
                                    ReminderType.TAX_SAVING -> {
                                        selectedFrequency = ReminderFrequency.ONCE
                                        title = "FY Capital Gains Tax Harvest"
                                        amountText = "125000"
                                        dateString = "2027-03-15"
                                        notes = "Harvest ₹1.25L LTCG exemption"
                                    }
                                    ReminderType.REVIEW -> {
                                        selectedFrequency = ReminderFrequency.WEEKLY
                                        title = "Weekly Portfolio Review"
                                        amountText = "0"
                                        notes = "Check asset allocation & fund health"
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryBlue else CardElevated,
                            border = BorderStroke(1.dp, if (isSelected) PrimaryBlueLight else CardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(type.iconEmoji, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = type.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Reminder Title
                Text("Reminder Title", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Fund Name
                Text("Mutual Fund", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(
                    value = fundName,
                    onValueChange = { fundName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Frequency / Timing Options
                if (selectedFrequency == ReminderFrequency.MONTHLY) {
                    Text("Day of the Month (SIP Due Date)", fontSize = 11.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 5, 10, 15, 20, 25, 28).forEach { day ->
                            val isDaySelected = dayOfMonth == day
                            Surface(
                                modifier = Modifier.clickable { dayOfMonth = day },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDaySelected) BullishGreen else CardElevated,
                                border = BorderStroke(1.dp, if (isDaySelected) BullishGreen else CardBorder)
                            ) {
                                Text(
                                    text = "${day}th",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDaySelected) Color.White else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                } else if (selectedFrequency == ReminderFrequency.ONCE) {
                    Text("Target Date (YYYY-MM-DD)", fontSize = 11.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = { dateString = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Amount (₹)
                if (selectedType != ReminderType.REVIEW) {
                    Text("Target / SIP Amount (₹)", fontSize = 11.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Notes
                Text("Notes / Broker Memo", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 5000.0
                            val reminder = CustomReminder(
                                title = title.ifBlank { "Mutual Fund Reminder" },
                                fundName = fundName.ifBlank { "Mutual Fund" },
                                type = selectedType,
                                frequency = selectedFrequency,
                                dayOfMonth = dayOfMonth,
                                dateString = dateString,
                                amount = amt,
                                notes = notes
                            )
                            onSave(reminder)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Reminder", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
