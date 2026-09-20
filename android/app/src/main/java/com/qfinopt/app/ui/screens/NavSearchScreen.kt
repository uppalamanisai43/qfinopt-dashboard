package com.qfinopt.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.ui.components.*
import com.qfinopt.app.ui.theme.*
import com.qfinopt.app.ui.viewmodel.MainViewModel

@Composable
fun NavSearchScreen(viewModel: MainViewModel) {
    val searchResult by viewModel.navSearchResult.collectAsState()
    val isLoading by viewModel.isNavSearchLoading.collectAsState()
    var searchInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "📡 Live AMFI NAV Search",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Official AMFI India database updated daily at 11 PM IST.",
            fontSize = 12.sp,
            color = TextSecondary
        )

        // Search Bar
        OutlinedTextField(
            value = searchInput,
            onValueChange = {
                searchInput = it
                viewModel.searchLiveNav(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by name (e.g. SBI, HDFC, Quant, Axis)...", fontSize = 13.sp, color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            trailingIcon = {
                if (searchInput.isNotEmpty()) {
                    IconButton(onClick = {
                        searchInput = ""
                        viewModel.searchLiveNav("")
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Statistics Cards
        if (searchResult != null) {
            val res = searchResult!!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(label = "TOTAL FUNDS", value = "${res.totalFundsInAmfi}", modifier = Modifier.weight(1f))
                MetricCard(label = "LOWEST NAV", value = "₹${res.lowestNav}", modifier = Modifier.weight(1f))
                MetricCard(label = "HIGHEST NAV", value = "₹${res.highestNav}", modifier = Modifier.weight(1f))
            }
            Text(
                text = "Showing ${res.results.size} matches (${res.totalMatches} total)",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        // Search Results List
        if (isLoading) {
            LoadingView(message = "Searching AMFI database...")
        } else if (searchResult != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResult!!.results) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Code: ${item.code} • Date: ${item.date}",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${String.format("%.4f", item.nav)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BullishGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
