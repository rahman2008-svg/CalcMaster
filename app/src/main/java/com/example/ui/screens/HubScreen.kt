package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CalculatorTool(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubScreen(
    onNavigateToTool: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    val tools = remember {
        listOf(
            CalculatorTool(
                id = "BASIC",
                title = "Basic Calculator",
                description = "Standard operators with memory",
                icon = Icons.Default.Calculate
            ),
            CalculatorTool(
                id = "SCIENTIFIC",
                title = "Scientific",
                description = "Trig, Logs, Powers",
                icon = Icons.Default.Functions
            ),
            CalculatorTool(
                id = "CONVERTER_UNIT",
                title = "Units",
                description = "Length, weight, conversions",
                icon = Icons.Default.CompareArrows
            ),
            CalculatorTool(
                id = "CONVERTER_CURRENCY",
                title = "Currency",
                description = "Exchange rate conversions",
                icon = Icons.Default.CurrencyExchange
            ),
            CalculatorTool(
                id = "BMI",
                title = "BMI Tool",
                description = "Health indicator metrics",
                icon = Icons.Default.Favorite
            ),
            CalculatorTool(
                id = "GPA",
                title = "GPA",
                description = "Academic score calculation",
                icon = Icons.Default.School
            ),
            CalculatorTool(
                id = "AGE",
                title = "Age",
                description = "Exact age and count down",
                icon = Icons.Default.CalendarToday
            ),
            CalculatorTool(
                id = "EMI",
                title = "EMI",
                description = "Loan installment calculation",
                icon = Icons.Default.AccountBalanceWallet
            )
        )
    }

    val filteredTools = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            tools
        } else {
            tools.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcoming Title Section (Bento Theme Header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CalcMaster",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "SMART TOOLKIT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.8.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        // Modern Pill Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search calculators...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .testTag("search_tools")
        )

        if (searchQuery.isNotBlank()) {
            Text(
                text = "Search Results",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (filteredTools.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Not found",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No tools matched your query",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                SearchResultsLayout(
                    filteredTools = filteredTools,
                    isDark = isDark,
                    onNavigateToTool = onNavigateToTool
                )
            }
        } else {
            // Bento Grid Title
            Text(
                text = "My Utilities",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // The main beautiful Bento Grid Layout
            BentoGridLayout(
                isDark = isDark,
                onNavigateToTool = onNavigateToTool
            )
        }
    }
}

@Composable
fun BentoGridLayout(
    isDark: Boolean,
    onNavigateToTool: (String) -> Unit
) {
    // Style configurations matching the HTML and dynamic modes perfectly
    // Featured Basic card uses Purple
    val cardBasicBg = if (isDark) Color(0xFF2D1454) else Color(0xFFEADDFF)
    val cardBasicText = if (isDark) Color(0xFFEADDFF) else Color(0xFF21005D)
    val cardBasicIconBg = if (isDark) Color(0xFFEADDFF) else Color(0xFF21005D)
    val cardBasicIconTint = if (isDark) Color(0xFF21005D) else Color(0xFFFFFFFF)

    // Scientific card uses lighter purple
    val cardSciBg = if (isDark) Color(0xFF251F33) else Color(0xFFE8DEF8)
    val cardSciText = if (isDark) Color(0xFFE8DEF8) else Color(0xFF1D192B)
    val cardSciIconBg = if (isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4)
    val cardSciIconTint = if (isDark) Color(0xFF21005D) else Color(0xFFFFFFFF)

    // Units card uses pink
    val cardUnitBg = if (isDark) Color(0xFF4C1D2B) else Color(0xFFFFD8E4)
    val cardUnitText = if (isDark) Color(0xFFFFD8E4) else Color(0xFF31111D)

    // Secondary Bento boxes use Gray/White with borders
    val cardBorderColor = if (isDark) Color(0xFF35303D) else Color(0xFFCAC4D0)
    val cardSecondaryBg = if (isDark) Color(0xFF1E1A24) else Color(0xFFF3EDF7)
    val cardSecondaryText = if (isDark) Color(0xFFE6E1E5) else Color(0xFF1D1B1E)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // ROW 1: Basic Calculator (Featured, large) & Scientific Calculator (medium, tall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Basic Calculator Card (Featured)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBasicBg),
                modifier = Modifier
                    .weight(1.8f)
                    .fillMaxHeight()
                    .clickable { onNavigateToTool("BASIC") }
                    .testTag("tool_card_BASIC")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(cardBasicIconBg, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "%",
                                color = cardBasicIconTint,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(cardBasicText.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "FEATURED",
                                color = cardBasicText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Column {
                        Text(
                            "Basic Calculator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = cardBasicText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Arithmetic & Memory",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = cardBasicText.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Scientific Calculator Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardSciBg),
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .clickable { onNavigateToTool("SCIENTIFIC") }
                    .testTag("tool_card_SCIENTIFIC")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(cardSciIconBg, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "sin",
                            color = cardSciIconTint,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }

                    Column {
                        Text(
                            "Scientific",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = cardSciText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Trig, Log, Power",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
                            color = cardSciText.copy(alpha = 0.72f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // ROW 2: Unit Converter (Pinkish) & Currency Rate (Gray/Borders)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Unit Converter (Pinkish Bento Tile)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardUnitBg),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onNavigateToTool("CONVERTER_UNIT") }
                    .testTag("tool_card_CONVERTER_UNIT")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(cardUnitText.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📏", fontSize = 18.sp)
                    }
                    Text(
                        "Units",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = cardUnitText
                    )
                }
            }

            // Currency Rate (Soft lavender outline tile)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardSecondaryBg),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onNavigateToTool("CONVERTER_CURRENCY") }
                    .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
                    .testTag("tool_card_CONVERTER_CURRENCY")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(cardSecondaryText.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💱", fontSize = 18.sp)
                    }
                    Text(
                        "Currency",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = cardSecondaryText
                    )
                }
            }
        }

        // ROW 3: BMI Tool (Outline, Centered) & GPA Tool (Featured-tint, Centered)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // BMI
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardSecondaryBg),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
                    .clickable { onNavigateToTool("BMI") }
                    .testTag("tool_card_BMI")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(cardSciIconBg, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚖️", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "BMI Tool",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = cardSecondaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Health metrics",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = cardSecondaryText.copy(alpha = 0.65f)
                    )
                }
            }

            // GPA
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBasicBg),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onNavigateToTool("GPA") }
                    .testTag("tool_card_GPA")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(cardBasicIconBg, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "A+",
                            color = cardBasicIconTint,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "GPA Score",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = cardBasicText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Academic Score",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = cardBasicText.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // ROW 4: Age Calculator & EMI Calculator (Smaller pill tiles)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Age
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardSecondaryBg),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                    .clickable { onNavigateToTool("AGE") }
                    .testTag("tool_card_AGE")
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🎂", fontSize = 16.sp)
                        Text(
                            "Age",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = cardSecondaryText
                        )
                    }
                }
            }

            // EMI
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardSecondaryBg),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                    .clickable { onNavigateToTool("EMI") }
                    .testTag("tool_card_EMI")
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🏠", fontSize = 16.sp)
                        Text(
                            "EMI Calculator",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = cardSecondaryText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultsLayout(
    filteredTools: List<CalculatorTool>,
    isDark: Boolean,
    onNavigateToTool: (String) -> Unit
) {
    val cardBorderColor = if (isDark) Color(0xFF35303D) else Color(0xFFCAC4D0)
    val cardSecondaryBg = if (isDark) Color(0xFF1E1A24) else Color(0xFFF3EDF7)
    val cardSecondaryText = if (isDark) Color(0xFFE6E1E5) else Color(0xFF1D1B1E)

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        filteredTools.forEach { tool ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardSecondaryBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                    .clickable { onNavigateToTool(tool.id) }
                    .testTag("tool_card_${tool.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(cardSecondaryText.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tool.icon,
                            contentDescription = tool.title,
                            tint = cardSecondaryText,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            tool.title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = cardSecondaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            tool.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = cardSecondaryText.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Navigate",
                        tint = cardSecondaryText.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
