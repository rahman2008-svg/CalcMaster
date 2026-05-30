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
fun ScientificCalcScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val input by viewModel.calcInput.collectAsState()
    val result by viewModel.calcResult.collectAsState()

    val buttons = remember {
        listOf(
            "sin", "cos", "tan", "log", "ln",
            "sqrt", "^", "π", "e", "C",
            "7", "8", "9", "(", ")",
            "4", "5", "6", "×", "÷",
            "1", "2", "3", "+", "-",
            "0", ".", "⌫", "=", ""
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scientific Calculator", fontWeight = FontWeight.Bold) },
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
            // Display Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = input.ifEmpty { "0" },
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = if (input.length > 15) 24.sp else 36.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("sci_calc_display_input"),
                    maxLines = 4
                )

                if (result.isNotEmpty()) {
                    Text(
                        text = result,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sci_calc_display_result")
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(bottom = 12.dp))

            // 5-Column Grid Layout for Scientific Density
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2.2f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(buttons) { item ->
                    if (item.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize())
                    } else {
                        ScientificKeypadButton(
                            text = item,
                            onClick = {
                                when (item) {
                                    "C" -> viewModel.onCalcClear()
                                    "⌫" -> viewModel.onCalcBackspace()
                                    "=" -> viewModel.onCalcEquals("SCIENTIFIC")
                                    "×" -> viewModel.onCalcInputStr("×")
                                    "÷" -> viewModel.onCalcInputStr("÷")
                                    "sin", "cos", "tan", "log", "ln", "sqrt" -> viewModel.onCalcInputStr("$item(")
                                    else -> viewModel.onCalcInput(item[0])
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScientificKeypadButton(
    text: String,
    onClick: () -> Unit
) {
    val isSciFunc = text in listOf("sin", "cos", "tan", "log", "ln", "sqrt", "^", "π", "e")
    val isOperator = text in listOf("+", "-", "×", "÷", "=")
    val isClearOrBack = text in listOf("C", "⌫")

    val containerColor = when {
        text == "=" -> MaterialTheme.colorScheme.secondary
        isSciFunc -> MaterialTheme.colorScheme.secondaryContainer
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        isClearOrBack -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        text == "=" -> MaterialTheme.colorScheme.onSecondary
        isSciFunc -> MaterialTheme.colorScheme.onSecondaryContainer
        isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
        isClearOrBack -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val fontSize = if (text.length > 2) 13.sp else 16.sp

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() }
            .testTag("btn_sci_$text")
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (text == "⌫") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    textAlign = TextAlign.Center
                ),
                color = contentColor
            )
        }
    }
}
