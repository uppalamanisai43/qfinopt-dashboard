package com.qfinopt.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.data.api.ApiClient
import com.qfinopt.app.data.model.*
import kotlinx.coroutines.launch

// ─── Quantum color palette ────────────────────────────────────────────────────
private val QuantumPurple   = Color(0xFF7C3AED)
private val QuantumBlue     = Color(0xFF2563EB)
private val QuantumCyan     = Color(0xFF06B6D4)
private val QuantumGold     = Color(0xFFF59E0B)
private val QuantumGreen    = Color(0xFF10B981)
private val SurfaceDark     = Color(0xFF0F172A)
private val CardDark        = Color(0xFF1E293B)
private val CardDarker      = Color(0xFF0F1929)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QAOAScreen() {
    val scope = rememberCoroutineScope()

    // State
    var fundInput       by remember { mutableStateOf("") }
    val fundList        = remember { mutableStateListOf<String>() }
    var kValue          by remember { mutableStateOf(3) }
    var qaaoLayers      by remember { mutableStateOf(2) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMsg        by remember { mutableStateOf<String?>(null) }
    var result          by remember { mutableStateOf<QAOAOptimizeResponse?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ── Header ────────────────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color(0xFF4C1D95), Color(0xFF1E3A8A))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚛️", fontSize = 32.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Quantum-Inspired Optimizer",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            "QAOA · QUBO · Quantum Ising Hamiltonian",
                            color = Color(0xFFBFDBFE),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Formulates mutual fund selection as a Quadratic Unconstrained Binary " +
                    "Optimization (QUBO) problem. Solved via a simulated QAOA circuit with " +
                    "p-layer Ising Hamiltonian evolution.",
                    color = Color(0xFFDDD6FE),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // ── Fund Input ────────────────────────────────────────────────────────
        item {
            QuantumCard {
                Text("📋 Select Funds to Optimize (2–15 funds)",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = fundInput,
                        onValueChange = { fundInput = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Fund name", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = QuantumPurple,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val trimmed = fundInput.trim()
                            if (trimmed.isNotEmpty() && !fundList.contains(trimmed) && fundList.size < 15) {
                                fundList.add(trimmed)
                                fundInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(QuantumPurple)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Default fund suggestions
                if (fundList.isEmpty()) {
                    Text("Quick add popular funds:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(Modifier.height(6.dp))
                    val suggestions = listOf(
                        "Axis Bluechip Fund", "Mirae Asset Large Cap Fund",
                        "Parag Parikh Flexi Cap Fund", "SBI Small Cap Fund",
                        "HDFC Mid-Cap Opportunities Fund"
                    )
                    suggestions.chunked(2).forEach { rowFunds ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowFunds.forEach { suggestion ->
                                SuggestionChip(
                                    onClick = {
                                        if (!fundList.contains(suggestion) && fundList.size < 15)
                                            fundList.add(suggestion)
                                    },
                                    label = {
                                        Text(suggestion, fontSize = 10.sp, maxLines = 1,
                                            color = QuantumCyan)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, QuantumCyan.copy(alpha = 0.4f))
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }

                // Added funds list
                if (fundList.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    fundList.forEachIndexed { idx, fund ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E2D40))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${idx + 1}.",
                                color = QuantumCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.width(22.dp)
                            )
                            Text(
                                fund,
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { fundList.removeAt(idx) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove",
                                    tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }

        // ── Parameters ───────────────────────────────────────────────────────
        item {
            QuantumCard {
                Text("⚙️ QAOA Parameters",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))

                // K selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Funds to Select (K)",
                            color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(
                            "QUBO cardinality constraint: Σxᵢ = K",
                            color = Color(0xFF64748B), fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (kValue > 2) kValue-- },
                            modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Dec",
                                tint = QuantumPurple)
                        }
                        Text(
                            "$kValue",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = { if (kValue < fundList.size - 1) kValue++ },
                            modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Inc",
                                tint = QuantumPurple)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFF1E293B))
                Spacer(Modifier.height(14.dp))

                // p layers selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("QAOA Circuit Depth (p)",
                            color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(
                            "Gates: O(p·N²), More layers = higher fidelity",
                            color = Color(0xFF64748B), fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (qaaoLayers > 1) qaaoLayers-- },
                            modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Dec",
                                tint = QuantumCyan)
                        }
                        Text(
                            "p=$qaaoLayers",
                            color = QuantumCyan,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            modifier = Modifier.width(50.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = { if (qaaoLayers < 4) qaaoLayers++ },
                            modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Inc",
                                tint = QuantumCyan)
                        }
                    }
                }
            }
        }

        // ── Run Button ────────────────────────────────────────────────────────
        item {
            Button(
                onClick = {
                    if (fundList.size < 2) {
                        errorMsg = "Add at least 2 funds to optimize."
                        return@Button
                    }
                    if (kValue >= fundList.size) {
                        errorMsg = "K must be less than number of funds."
                        return@Button
                    }
                    errorMsg = null
                    result = null
                    isLoading = true
                    scope.launch {
                        try {
                            val resp = ApiClient.getApi().runQaoaOptimizer(
                                QAOAOptimizeRequest(
                                    fundNames = fundList.toList(),
                                    k = kValue,
                                    qaaoLayers = qaaoLayers
                                )
                            )
                            if (resp.isSuccessful) {
                                result = resp.body()
                            } else {
                                errorMsg = "Backend error: ${resp.code()} — ${resp.errorBody()?.string()}"
                            }
                        } catch (e: Exception) {
                            errorMsg = "Connection error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QuantumPurple),
                enabled = !isLoading && fundList.size >= 2
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Running QAOA Circuit...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("⚛️  Run Quantum Optimizer", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // ── Error ─────────────────────────────────────────────────────────────
        errorMsg?.let { msg ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF7F1D1D).copy(alpha = 0.5f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, tint = Color(0xFFFCA5A5),
                        contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(msg, color = Color(0xFFFCA5A5), fontSize = 12.sp)
                }
            }
        }

        // ── Results ───────────────────────────────────────────────────────────
        result?.let { res ->

            // Algorithm label
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(QuantumPurple.copy(alpha = 0.15f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Science, tint = QuantumPurple,
                        contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(res.algorithm, color = QuantumPurple, fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                }
            }

            // QUBO info pills
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuantumPill("QUBO ${res.quboSize}", QuantumBlue)
                    QuantumPill("p=${res.qaaoLayers} layers", QuantumPurple)
                    QuantumPill("N=${res.nFundsInput} funds", QuantumCyan)
                    QuantumPill("K=${res.kFundsSelected} selected", QuantumGold)
                }
            }

            // Selected funds
            item {
                QuantumCard {
                    Text("✅ QAOA Optimal Portfolio",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    res.selectedFunds.forEachIndexed { i, fund ->
                        val weight = res.portfolioWeightsPct[fund] ?: 0.0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(QuantumPurple.copy(alpha = 0.12f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(QuantumPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${i + 1}", color = Color.White,
                                    fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(fund, color = Color.White,
                                fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text("${weight}%", color = QuantumGold,
                                fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            // Metrics comparison
            item {
                QuantumCard {
                    Text("📊 Portfolio Metrics",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricBlock(
                            label = "Sharpe (QAOA)",
                            value = String.format("%.2f", res.qaaoMetrics.sharpeRatio),
                            color = QuantumGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBlock(
                            label = "Annual Return",
                            value = "${String.format("%.1f", res.qaaoMetrics.expectedAnnualReturnPct)}%",
                            color = QuantumCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBlock(
                            label = "Volatility",
                            value = "${String.format("%.1f", res.qaaoMetrics.annualVolatilityPct)}%",
                            color = QuantumGold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Benchmark table — Classical vs Quantum
            item {
                QuantumCard {
                    Text("⚖️ Benchmark Comparison",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("QAOA vs Classical Markowitz vs Brute-Force",
                        color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(Modifier.height(12.dp))

                    // Table header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TableHeaderCell("Method", Modifier.weight(2.5f))
                        TableHeaderCell("Sharpe", Modifier.weight(1f))
                        TableHeaderCell("Return", Modifier.weight(1f))
                        TableHeaderCell("Time", Modifier.weight(1f))
                    }
                    HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)
                    Spacer(Modifier.height(4.dp))

                    res.benchmarkTable.forEachIndexed { i, row ->
                        val rowBg = if (i == 0) QuantumPurple.copy(alpha = 0.12f)
                                    else Color.Transparent
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(rowBg)
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                        ) {
                            Column(modifier = Modifier.weight(2.5f)) {
                                Text(row.method, color = if (i == 0) QuantumCyan else Color(0xFFCBD5E1),
                                    fontSize = 10.sp, fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal)
                                Text(row.complexity, color = Color(0xFF64748B),
                                    fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            }
                            Text(String.format("%.2f", row.sharpe),
                                color = if (i == 0) QuantumGreen else Color(0xFF94A3B8),
                                fontSize = 11.sp, modifier = Modifier.weight(1f),
                                fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center)
                            Text("${String.format("%.1f", row.annualReturnPct)}%",
                                color = Color(0xFF94A3B8), fontSize = 11.sp,
                                modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("${row.timeSec}s",
                                color = Color(0xFF94A3B8), fontSize = 11.sp,
                                modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }

            // Quantum advantage note
            item {
                QuantumCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🔬", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            res.quantumAdvantageNote,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Runtime: ${String.format("%.3f", res.runtimeSeconds)}s  |  " +
                        "QUBO Energy: ${String.format("%.4f", res.quboEnergy)}  |  " +
                        "Convergence: ${String.format("%.2f", res.convergenceQuality * 100)}%",
                        color = QuantumPurple,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun QuantumCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun QuantumPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = color, fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun MetricBlock(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Text(label, color = Color(0xFF94A3B8), fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(text, color = Color(0xFF64748B), fontSize = 10.sp,
        fontWeight = FontWeight.Bold, modifier = modifier,
        textAlign = TextAlign.Center)
}
