package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Dates states
    var birthDateState by remember { mutableStateOf(Calendar.getInstance().apply { set(1998, 4, 15) }) }
    var targetDateState by remember { mutableStateOf(Calendar.getInstance()) }

    val ageYears by viewModel.ageResultYears.collectAsState()
    val ageMonths by viewModel.ageResultMonths.collectAsState()
    val ageDays by viewModel.ageResultDays.collectAsState()
    val nextBdayDays by viewModel.nextBirthdayDays.collectAsState()

    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

    val dobPickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val newCal = Calendar.getInstance().apply { set(year, month, day) }
                birthDateState = newCal
                viewModel.setBirthDate(year, month, day)
            },
            birthDateState.get(Calendar.YEAR),
            birthDateState.get(Calendar.MONTH),
            birthDateState.get(Calendar.DAY_OF_MONTH)
        )
    }

    val targetPickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val newCal = Calendar.getInstance().apply { set(year, month, day) }
                targetDateState = newCal
                viewModel.setTargetDate(year, month, day)
            },
            targetDateState.get(Calendar.YEAR),
            targetDateState.get(Calendar.MONTH),
            targetDateState.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Auto-calculate on initial render
    LaunchedEffect(Unit) {
        viewModel.setBirthDate(
            birthDateState.get(Calendar.YEAR),
            birthDateState.get(Calendar.MONTH),
            birthDateState.get(Calendar.DAY_OF_MONTH)
        )
        viewModel.setTargetDate(
            targetDateState.get(Calendar.YEAR),
            targetDateState.get(Calendar.MONTH),
            targetDateState.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Age Calculator", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pick Dates Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // DOB Picker
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { dobPickerDialog.show() }
                        .testTag("btn_pick_dob"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Birthdate",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sdf.format(birthDateState.time),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Target Date Picker
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { targetPickerDialog.show() }
                        .testTag("btn_pick_target_date"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Age at Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sdf.format(targetDateState.time),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Central Age Display Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Calculated Age",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AgeBox(value = ageYears, unit = "Years")
                        AgeBox(value = ageMonths, unit = "Months")
                        AgeBox(value = ageDays, unit = "Days")
                    }
                }
            }

            // Next Birthday indicator
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cake,
                        contentDescription = "Birthday",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            text = "Next Birthday In",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "$nextBdayDays days remaining",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f),
                            modifier = Modifier.testTag("label_next_birthday")
                        )
                    }
                }
            }

            // Total Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Detailed Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Details formula calculations
                    val totalMonthsRounded = (ageYears * 12) + ageMonths
                    val totalDaysNum = ((targetDateState.timeInMillis - birthDateState.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                    val totalWeeksNum = totalDaysNum / 7
                    val totalHoursNum = totalDaysNum.toLong() * 24

                    DetailRow(label = "Total Months", value = "$totalMonthsRounded months")
                    DetailRow(label = "Total Weeks", value = "$totalWeeksNum weeks")
                    DetailRow(label = "Total Days", value = "$totalDaysNum days")
                    DetailRow(label = "Total Hours", value = "$totalHoursNum hours")
                }
            }
        }
    }
}

@Composable
fun AgeBox(value: Int, unit: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("age_box_$unit")
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
