package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val system by viewModel.bmiSystem.collectAsState()
    val weight by viewModel.bmiWeight.collectAsState()
    val height by viewModel.bmiHeight.collectAsState()
    val bmiVal by viewModel.bmiValue.collectAsState()
    val category by viewModel.bmiCategory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BMI Calculator", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Units Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf("Metric", "Imperial").forEach { label ->
                    val isSelected = system == label
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickableSingle(isSelected = isSelected) {
                                viewModel.setBmiSystem(label)
                            }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .padding(vertical = 12.dp)
                            .testTag("btn_bmi_unit_$label"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Input Fields Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Height Input
                    val heightUnitLabel = if (system == "Metric") "cm" else "inches"
                    OutlinedTextField(
                        value = height,
                        onValueChange = { viewModel.setBmiHeight(it) },
                        label = { Text("Height ($heightUnitLabel)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_bmi_height")
                    )

                    // Weight Input
                    val weightUnitLabel = if (system == "Metric") "kg" else "lbs"
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { viewModel.setBmiWeight(it) },
                        label = { Text("Weight ($weightUnitLabel)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_bmi_weight")
                    )

                    // Compute Buttons
                    Button(
                        onClick = { viewModel.calculateBmi() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_calculate_bmi"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Calculate BMI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Calculations Result Sheet
            AnimatedVisibility(visible = bmiVal.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your BMI Score",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = bmiVal,
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("bmi_value_label")
                        )

                        val categoryColor = when (category) {
                            "Underweight" -> Color(0xFF03A9F4)
                            "Normal weight" -> Color(0xFF4CAF50)
                            "Overweight" -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }

                        Text(
                            text = category.uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            ),
                            color = categoryColor,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .testTag("bmi_category_label")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Bar Indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(18.5f)
                                    .fillMaxHeight()
                                    .background(
                                        if (category == "Underweight") Color(0xFF03A9F4)
                                        else Color(0xFF03A9F4).copy(alpha = 0.3f)
                                    )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(
                                modifier = Modifier
                                    .weight(6.5f)
                                    .fillMaxHeight()
                                    .background(
                                        if (category == "Normal weight") Color(0xFF4CAF50)
                                        else Color(0xFF4CAF50).copy(alpha = 0.3f)
                                    )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(
                                modifier = Modifier
                                    .weight(5f)
                                    .fillMaxHeight()
                                    .background(
                                        if (category == "Overweight") Color(0xFFFF9800)
                                        else Color(0xFFFF9800).copy(alpha = 0.3f)
                                    )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(
                                modifier = Modifier
                                    .weight(10f)
                                    .fillMaxHeight()
                                    .background(
                                        if (category == "Obese") Color(0xFFF44336)
                                        else Color(0xFFF44336).copy(alpha = 0.3f)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("<18.5", style = MaterialTheme.typography.bodySmall)
                            Text("18.5 - 24.9", style = MaterialTheme.typography.bodySmall)
                            Text("25.0 - 29.9", style = MaterialTheme.typography.bodySmall)
                            Text("30+", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// Helper Extension for elegant clickable tabs
@Composable
fun Modifier.clickableSingle(isSelected: Boolean, onClick: () -> Unit): Modifier {
    return this.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick)
}
