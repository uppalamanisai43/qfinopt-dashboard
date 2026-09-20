package com.qfinopt.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.qfinopt.app.data.api.ApiClient
import com.qfinopt.app.data.model.CustomReminder
import com.qfinopt.app.data.model.PdfReportRequest
import com.qfinopt.app.data.model.ReminderFrequency
import com.qfinopt.app.data.model.ReminderType
import com.qfinopt.app.ui.components.AddReminderDialog
import com.qfinopt.app.ui.theme.*
import com.qfinopt.app.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.*

@Composable
fun RemindersScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val reminders by viewModel.reminders.collectAsState()
    val selectedFund by viewModel.selectedFund.collectAsState()
    val withdrawalResult by viewModel.withdrawalResult.collectAsState()
    val investmentAmount by viewModel.investmentAmount.collectAsState()
    val investDate by viewModel.investDate.collectAsState()
    val sipAmount by viewModel.sipAmount.collectAsState()
    val sipYears by viewModel.sipYears.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var investorName by remember { mutableStateOf("MANI SAI") }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    if (showAddDialog) {
        AddReminderDialog(
            initialFundName = selectedFund,
            onDismiss = { showAddDialog = false },
            onSave = { reminder ->
                viewModel.addCustomReminder(reminder)
                Toast.makeText(context, "Reminder created: ${reminder.title}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = TextPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("🔔 Smart Reminders (${reminders.count { it.isEnabled }})", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                selectedContentColor = BullishGreen,
                unselectedContentColor = TextSecondary
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("📄 PDF Reports & Exports", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                selectedContentColor = AccentGold,
                unselectedContentColor = TextSecondary
            )
        }

        if (selectedTab == 0) {
            // TAB 0: SMART REMINDERS
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Summary Banner
                item {
                    val activeCount = reminders.count { it.isEnabled }
                    val nextSip = reminders.firstOrNull { it.isEnabled && it.type == ReminderType.SIP }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(CardElevated.copy(alpha = 0.8f), SurfaceDark)
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ACTIVE ALERTS & SIP SCHEDULE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$activeCount Active Reminders",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                }

                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Reminder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (nextSip != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = CardBorder, thickness = 0.8.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = BullishGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Next: ${nextSip.dayOfMonth}th of month • ${currencyFormat.format(nextSip.amount)} SIP into ${nextSip.fundName.take(24)}",
                                        fontSize = 11.sp,
                                        color = BullishGreen,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Preset Chips
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "⚡ QUICK REMINDER PRESETS (TAP TO CREATE):",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val presets = listOf(
                                Triple("📅 Monthly SIP", ReminderType.SIP, 5000.0),
                                Triple("🎯 ML Exit Alert", ReminderType.EXIT, 50000.0),
                                Triple("🛡️ Tax Harvest", ReminderType.TAX_SAVING, 125000.0),
                                Triple("🔔 Buy the Dip", ReminderType.PRICE_TARGET, 10000.0)
                            )

                            presets.forEach { (label, type, amt) ->
                                Surface(
                                    modifier = Modifier.clickable {
                                        val newRem = CustomReminder(
                                            title = when (type) {
                                                ReminderType.SIP -> "Monthly SIP: ${selectedFund.take(24)}"
                                                ReminderType.EXIT -> "Optimal Exit: ${selectedFund.take(24)}"
                                                ReminderType.TAX_SAVING -> "FY Tax Harvesting (₹1.25L Exemption)"
                                                ReminderType.PRICE_TARGET -> "Dip Buy: ${selectedFund.take(24)}"
                                                else -> "Portfolio Review"
                                            },
                                            fundName = selectedFund,
                                            type = type,
                                            frequency = if (type == ReminderType.SIP) ReminderFrequency.MONTHLY else ReminderFrequency.ONCE,
                                            dayOfMonth = 10,
                                            dateString = withdrawalResult?.withdrawDate ?: "2026-10-15",
                                            amount = amt,
                                            notes = "Scheduled via Q-FinOpt"
                                        )
                                        viewModel.addCustomReminder(newRem)
                                        Toast.makeText(context, "Added: ${newRem.title}", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = CardElevated,
                                    border = BorderStroke(1.dp, CardBorder)
                                ) {
                                    Text(
                                        text = "+ $label",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (reminders.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("🔔", fontSize = 36.sp)
                                Text("No Active Reminders", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Add SIP due dates or profit booking targets to get notified.", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Text("Create First Reminder")
                                }
                            }
                        }
                    }
                } else {
                    items(reminders, key = { it.id }) { reminder ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (reminder.isEnabled) SurfaceDark else SurfaceDark.copy(alpha = 0.6f)
                            ),
                            border = BorderStroke(1.dp, if (reminder.isEnabled) CardBorder else CardBorder.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Title Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(34.dp),
                                            shape = CircleShape,
                                            color = when (reminder.type) {
                                                ReminderType.SIP -> BullishGreen.copy(alpha = 0.15f)
                                                ReminderType.EXIT -> AccentGold.copy(alpha = 0.15f)
                                                ReminderType.TAX_SAVING -> CyanAccent.copy(alpha = 0.15f)
                                                else -> PrimaryBlue.copy(alpha = 0.15f)
                                            },
                                            border = BorderStroke(
                                                1.dp,
                                                when (reminder.type) {
                                                    ReminderType.SIP -> BullishGreen.copy(alpha = 0.5f)
                                                    ReminderType.EXIT -> AccentGold.copy(alpha = 0.5f)
                                                    ReminderType.TAX_SAVING -> CyanAccent.copy(alpha = 0.5f)
                                                    else -> PrimaryBlue.copy(alpha = 0.5f)
                                                }
                                            )
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(reminder.type.iconEmoji, fontSize = 16.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = reminder.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (reminder.isEnabled) TextPrimary else TextMuted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = reminder.fundName,
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = reminder.isEnabled,
                                        onCheckedChange = { viewModel.toggleCustomReminder(reminder.id) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = PrimaryBlue,
                                            checkedTrackColor = PrimaryBlue.copy(alpha = 0.4f)
                                        )
                                    )
                                }

                                // Badges Row: Timing & Amount
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val timingText = when (reminder.frequency) {
                                        ReminderFrequency.MONTHLY -> "📅 Every ${reminder.dayOfMonth}th of month"
                                        ReminderFrequency.ONCE -> "🎯 Target: ${reminder.dateString.ifBlank { "Optimal Date" }}"
                                        ReminderFrequency.WEEKLY -> "📊 Every Sunday"
                                    }

                                    Surface(
                                        color = CardElevated,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, CardBorder)
                                    ) {
                                        Text(
                                            text = timingText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    if (reminder.amount > 0) {
                                        Surface(
                                            color = BullishGreen.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, BullishGreen.copy(alpha = 0.35f))
                                        ) {
                                            Text(
                                                text = currencyFormat.format(reminder.amount),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BullishGreen,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                if (reminder.notes.isNotBlank()) {
                                    Text(
                                        text = reminder.notes,
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        maxLines = 2
                                    )
                                }

                                HorizontalDivider(color = CardBorder, thickness = 0.8.dp)

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Add to Native Phone Calendar
                                        OutlinedButton(
                                            onClick = {
                                                try {
                                                    val calIntent = Intent(Intent.ACTION_INSERT).apply {
                                                        data = CalendarContract.Events.CONTENT_URI
                                                        putExtra(CalendarContract.Events.TITLE, "${reminder.type.iconEmoji} ${reminder.title} - ${reminder.fundName}")
                                                        putExtra(
                                                            CalendarContract.Events.DESCRIPTION,
                                                            "Q-FinOpt Reminder: ${reminder.notes}\nFund: ${reminder.fundName}\nTarget Amount: ${currencyFormat.format(reminder.amount)}"
                                                        )
                                                        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                                                        if (reminder.frequency == ReminderFrequency.MONTHLY) {
                                                            putExtra(CalendarContract.Events.RRULE, "FREQ=MONTHLY;BYMONTHDAY=${reminder.dayOfMonth}")
                                                        }
                                                    }
                                                    context.startActivity(calIntent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Could not open calendar app", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, PrimaryBlueLight.copy(alpha = 0.6f)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = PrimaryBlueLight, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Calendar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueLight)
                                        }

                                        // WhatsApp Reminder
                                        Button(
                                            onClick = {
                                                val timingStr = if (reminder.frequency == ReminderFrequency.MONTHLY) "Every ${reminder.dayOfMonth}th of month" else reminder.dateString
                                                val rawMsg = "🔔 *Q-FinOpt Alert: ${reminder.title}*\n" +
                                                        "📈 *Fund*: ${reminder.fundName}\n" +
                                                        "💰 *Amount*: ${currencyFormat.format(reminder.amount)}\n" +
                                                        "📅 *Schedule*: $timingStr\n" +
                                                        "📝 *Note*: ${reminder.notes}\n\n" +
                                                        "⚡ _Generated via Q-FinOpt Portfolio AI_"
                                                val encodedMsg = URLEncoder.encode(rawMsg, "UTF-8")
                                                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=$encodedMsg"))
                                                try {
                                                    context.startActivity(waIntent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteCustomReminder(reminder.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(17.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 1: PDF REPORTS & EXPORT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Optimal Exit Summary Card
                val withdrawDateStr = withdrawalResult?.withdrawDate ?: "Optimal Date"
                val optDay = withdrawalResult?.optDay ?: 0
                val expectedVal = withdrawalResult?.optVal ?: 0.0
                val gainVal = withdrawalResult?.gain ?: 0.0
                val retPct = withdrawalResult?.retPct ?: 0.0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(CardElevated.copy(alpha = 0.7f), SurfaceDark)
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ML OPTIMAL EXIT FORECAST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                            OutlinedButton(
                                onClick = {
                                    val ok = viewModel.createExitReminderFromMl()
                                    if (ok) {
                                        Toast.makeText(context, "Added optimal exit to Smart Reminders!", Toast.LENGTH_SHORT).show()
                                        selectedTab = 0
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BullishGreen.copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(Icons.Default.AddAlert, contentDescription = null, tint = BullishGreen, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("🔔 Add to Reminders", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BullishGreen)
                            }
                        }

                        Text(selectedFund, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Target Exit Date", fontSize = 10.sp, color = TextSecondary)
                                Text("$withdrawDateStr (Day $optDay)", fontSize = 13.sp, color = AccentGold, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Expected Return", fontSize = 10.sp, color = TextSecondary)
                                Text("+${currencyFormat.format(gainVal)} (${String.format("%.1f", retPct)}%)", fontSize = 13.sp, color = BullishGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // PDF Generator Configuration
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "📄 GENERATE EXECUTIVE PDF REPORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        Text("Investor Full Name (for Report Title)", fontSize = 11.sp, color = TextSecondary)
                        OutlinedTextField(
                            value = investorName,
                            onValueChange = { investorName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        // Meta details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active Fund: ${selectedFund.take(20)}...", fontSize = 11.sp, color = TextSecondary)
                            Text("Investment: ${currencyFormat.format(investmentAmount)}", fontSize = 11.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isGeneratingPdf = true
                                    try {
                                        val req = PdfReportRequest(
                                            fundName = selectedFund,
                                            userName = investorName,
                                            investment = investmentAmount,
                                            investDate = investDate,
                                            sipAmount = sipAmount,
                                            sipYears = sipYears
                                        )
                                        val resp = withContext(Dispatchers.IO) {
                                            ApiClient.getApi().downloadPdfReport(req)
                                        }
                                        if (resp.isSuccessful && resp.body() != null) {
                                            val bytes = resp.body()!!.bytes()
                                            val file = File(
                                                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                                "QFinOpt_Report_${System.currentTimeMillis()}.pdf"
                                            )
                                            withContext(Dispatchers.IO) {
                                                FileOutputStream(file).use { it.write(bytes) }
                                            }
                                            Toast.makeText(context, "PDF saved: ${file.name}", Toast.LENGTH_LONG).show()

                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(uri, "application/pdf")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(viewIntent, "Open Investment Report"))
                                        } else {
                                            Toast.makeText(context, "Failed to generate PDF: ${resp.message()}", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "PDF Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isGeneratingPdf = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isGeneratingPdf,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGeneratingPdf) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating PDF...")
                            } else {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate & Open PDF Report", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
