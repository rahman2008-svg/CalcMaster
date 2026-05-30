package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicCalcScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val input by viewModel.calcInput.collectAsState()
    val result by viewModel.calcResult.collectAsState()

    val buttons = remember {
        listOf(
            "C", "(", ")", "÷",
            "7", "8", "9", "×",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "0", ".", "⌫", "="
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Basic Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Display Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                // Active Infix Formula
                Text(
                    text = input.ifEmpty { "0" },
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = if (input.length > 12) 32.sp else 44.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("calc_display_input"),
                    maxLines = 3
                )

                // Computed Result Below
                if (result.isNotEmpty()) {
                    Text(
                        text = result,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End
                        ),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_display_result")
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(bottom = 16.dp))

            // Keypad Grid Section
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(buttons) { char ->
                    CalcKeypadButton(
                        text = char,
                        onClick = {
                            when (char) {
                                "C" -> viewModel.onCalcClear()
                                "⌫" -> viewModel.onCalcBackspace()
                                "=" -> viewModel.onCalcEquals("BASIC")
                                "×" -> viewModel.onCalcInputStr("×")
                                "÷" -> viewModel.onCalcInputStr("÷")
                                else -> viewModel.onCalcInput(char[0])
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalcKeypadButton(
    text: String,
    onClick: () -> Unit
) {
    val isOperator = text in listOf("+", "-", "×", "÷", "=")
    val isClearOrBack = text in listOf("C", "⌫")

    val containerColor = when {
        text == "=" -> MaterialTheme.colorScheme.primary
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        isClearOrBack -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        text == "=" -> MaterialTheme.colorScheme.onPrimary
        isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
        isClearOrBack -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() }
            .testTag("btn_calc_$text")
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (text == "⌫") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = contentColor
            )
        }
    }
}
