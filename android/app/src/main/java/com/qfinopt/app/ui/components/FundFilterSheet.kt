package com.qfinopt.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FundFilterSheet(
    categories: List<String>,
    selectedCategories: List<String>,
    riskLevels: List<String>,
    selectedRiskLevels: List<String>,
    funds: List<String>,
    selectedFund: String,
    onCategoryToggle: (String) -> Unit,
    onRiskLevelToggle: (String) -> Unit,
    onSelectAllCategories: () -> Unit,
    onClearCategories: () -> Unit,
    onSelectAllRiskLevels: () -> Unit,
    onFundSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredFunds = remember(funds, searchQuery) {
        if (searchQuery.isBlank()) funds
        else funds.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextSecondary) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 Filter & Select Fund",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Categories Section with Select All / Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category (${if (selectedCategories.isEmpty()) "All ${categories.size}" else "${selectedCategories.size} selected"})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Row {
                    TextButton(
                        onClick = onSelectAllCategories,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text("Select All", fontSize = 11.sp, color = PrimaryBlue)
                    }
                    TextButton(
                        onClick = onClearCategories,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text("Clear", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategories.isEmpty() || selectedCategories.contains(cat)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryToggle(cat) },
                        label = { Text(formatCategoryLabel(cat), fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.25f),
                            selectedLabelColor = PrimaryBlue
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Risk Level Section with Select All
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Risk Level",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                TextButton(
                    onClick = onSelectAllRiskLevels,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Text("Select All", fontSize = 11.sp, color = AccentGold)
                }
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                riskLevels.forEach { risk ->
                    val isSelected = selectedRiskLevels.isEmpty() || selectedRiskLevels.contains(risk)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onRiskLevelToggle(risk) },
                        label = { Text(risk, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGold.copy(alpha = 0.25f),
                            selectedLabelColor = AccentGold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search funds by name...", fontSize = 13.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${filteredFunds.size} funds available (tap to select)",
                fontSize = 11.sp,
                color = if (filteredFunds.isEmpty()) BearishRed else TextMuted
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredFunds) { fund ->
                    val isCurrent = fund == selectedFund
                    Surface(
                        color = if (isCurrent) PrimaryBlue.copy(alpha = 0.2f) else CardDark,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrent) PrimaryBlue else CardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onFundSelect(fund)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fund,
                                fontSize = 13.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) PrimaryBlue else TextPrimary,
                                maxLines = 2,
                                modifier = Modifier.weight(1f)
                            )
                            if (isCurrent) {
                                Text("✓", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatCategoryLabel(raw: String): String {
    return when (raw) {
        "Sectoral___Thematic_Mutual_F" -> "Sectoral / Thematic"
        else -> raw
            .replace("___", " ")
            .replace("__", " ")
            .replace("_", " ")
            .trim()
    }
}

