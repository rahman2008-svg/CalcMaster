package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Star
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
fun ConverterScreen(
    viewModel: AppViewModel,
    initialTab: Int = 0, // 0 = Unit, 1 = Currency
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabState by remember { mutableStateOf(initialTab) }
    val tabTitles = remember { listOf("Unit Converter", "Currency Exchange") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Converters", fontWeight = FontWeight.Bold) },
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
        ) {
            // Tab Header Row
            TabRow(
                selectedTabIndex = selectedTabState,
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth().testTag("tabs_row_converters")
            ) {
                tabTitles.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTabState == idx,
                        onClick = { selectedTabState = idx },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("tab_button_$idx")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                if (selectedTabState == 0) {
                    UnitConverterLayout(viewModel = viewModel)
                } else {
                    CurrencyConverterLayout(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun UnitConverterLayout(viewModel: AppViewModel) {
    val category by viewModel.unitCategory.collectAsState()
    val fromUnit by viewModel.unitFrom.collectAsState()
    val toUnit by viewModel.unitTo.collectAsState()
    val valInput by viewModel.unitValue.collectAsState()
    val valConverted by viewModel.convertedUnitValue.collectAsState()

    val categories = remember {
        listOf("Length", "Weight", "Area", "Temperature", "Speed", "Volume")
    }

    val unitMap = remember {
        mapOf(
            "Length" to listOf("m", "km", "cm", "mi", "yd", "ft", "in"),
            "Weight" to listOf("g", "kg", "lbs", "oz"),
            "Area" to listOf("m²", "km²", "ft²", "ac", "ha"),
            "Temperature" to listOf("°C", "°F", "K"),
            "Speed" to listOf("m/s", "km/h", "mph", "knots"),
            "Volume" to listOf("L", "mL", "gal", "cups")
        )
    }

    val activeUnits = unitMap[category] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Horizontal Categories Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            categories.forEach { cat ->
                val active = category == cat
                FilterChip(
                    selected = active,
                    onClick = { viewModel.setUnitCategory(cat) },
                    label = { Text(cat, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("chip_unit_category_$cat")
                )
            }
        }

        // Live calculation metrics Card
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
                    text = "Converted Output",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "$valInput $fromUnit = $valConverted $toUnit",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = if (valConverted.length > 12) 22.sp else 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("unit_result_display")
                )
            }
        }

        // Input Details Panel
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Numeric Input
                OutlinedTextField(
                    value = valInput,
                    onValueChange = { viewModel.setUnitValue(it) },
                    label = { Text("Enter Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_unit_val")
                )

                // Selection rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Unit FROM Select
                    Box(modifier = Modifier.weight(1f)) {
                        var fromExpanded by remember { mutableStateOf(false) }
                        @OptIn(ExperimentalMaterial3Api::class)
                        ExposedDropdownMenuBox(
                            expanded = fromExpanded,
                            onExpandedChange = { fromExpanded = !fromExpanded }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = fromUnit,
                                onValueChange = {},
                                label = { Text("From") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().testTag("dropdown_unit_from")
                            )
                            ExposedDropdownMenu(
                                expanded = fromExpanded,
                                onDismissRequest = { fromExpanded = false }
                            ) {
                                activeUnits.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            viewModel.setUnitFrom(option)
                                            fromExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Swap Icon
                    IconButton(
                        onClick = {
                            val temp = fromUnit
                            viewModel.setUnitFrom(toUnit)
                            viewModel.setUnitTo(temp)
                        },
                        modifier = Modifier.align(Alignment.CenterVertically).size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CompareArrows, contentDescription = "Swap units")
                    }

                    // Unit TO Select
                    Box(modifier = Modifier.weight(1f)) {
                        var toExpanded by remember { mutableStateOf(false) }
                        @OptIn(ExperimentalMaterial3Api::class)
                        ExposedDropdownMenuBox(
                            expanded = toExpanded,
                            onExpandedChange = { toExpanded = !toExpanded }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = toUnit,
                                onValueChange = {},
                                label = { Text("To") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().testTag("dropdown_unit_to")
                            )
                            ExposedDropdownMenu(
                                expanded = toExpanded,
                                onDismissRequest = { toExpanded = false }
                            ) {
                                activeUnits.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            viewModel.setUnitTo(option)
                                            toExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyConverterLayout(viewModel: AppViewModel) {
    val currencyFrom by viewModel.currencyFrom.collectAsState()
    val currencyTo by viewModel.currencyTo.collectAsState()
    val valInput by viewModel.currencyValue.collectAsState()
    val valConverted by viewModel.convertedCurrencyValue.collectAsState()

    val currencyList = remember {
        listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "INR", "CNY", "SGD")
    }

    var showEditRatesDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Exchange Display Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Live Currency Conversion",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "$valInput $currencyFrom = $valConverted $currencyTo",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = if (valConverted.length > 12) 22.sp else 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.testTag("currency_result_display")
                )
            }
        }

        // Inputs Card
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Numeric input
                OutlinedTextField(
                    value = valInput,
                    onValueChange = { viewModel.setCurrencyValue(it) },
                    label = { Text("Enter Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_currency_val")
                )

                // Selectors row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Currency FROM
                    Box(modifier = Modifier.weight(1f)) {
                        var fromCExpanded by remember { mutableStateOf(false) }
                        @OptIn(ExperimentalMaterial3Api::class)
                        ExposedDropdownMenuBox(
                            expanded = fromCExpanded,
                            onExpandedChange = { fromCExpanded = !fromCExpanded }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = currencyFrom,
                                onValueChange = {},
                                label = { Text("From") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromCExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().testTag("dropdown_currency_from")
                            )
                            ExposedDropdownMenu(
                                expanded = fromCExpanded,
                                onDismissRequest = { fromCExpanded = false }
                            ) {
                                currencyList.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            viewModel.setCurrencyFrom(option)
                                            fromCExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Swap Button
                    IconButton(
                        onClick = {
                            val temp = currencyFrom
                            viewModel.setCurrencyFrom(currencyTo)
                            viewModel.setCurrencyTo(temp)
                        },
                        modifier = Modifier.align(Alignment.CenterVertically).size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CompareArrows, contentDescription = "Swap currencies")
                    }

                    // Currency TO
                    Box(modifier = Modifier.weight(1f)) {
                        var toCExpanded by remember { mutableStateOf(false) }
                        @OptIn(ExperimentalMaterial3Api::class)
                        ExposedDropdownMenuBox(
                            expanded = toCExpanded,
                            onExpandedChange = { toCExpanded = !toCExpanded }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = currencyTo,
                                onValueChange = {},
                                label = { Text("To") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toCExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().testTag("dropdown_currency_to")
                            )
                            ExposedDropdownMenu(
                                expanded = toCExpanded,
                                onDismissRequest = { toCExpanded = false }
                            ) {
                                currencyList.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            viewModel.setCurrencyTo(option)
                                            toCExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Offline configuration tools button
        Button(
            onClick = { showEditRatesDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_offline_rates"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.CurrencyExchange, contentDescription = "Edit Offline Rates")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Adjust Offline Rates (Base: USD)", fontWeight = FontWeight.Bold)
        }

        // Expanded Rate Table List Dialog
        if (showEditRatesDialog) {
            AlertDialog(
                onDismissRequest = { showEditRatesDialog = false },
                title = { Text("Configure Exchange Rates") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Edit local conversation rates relative to USD ($1 USD value). Changes will immediately update calculations.", style = MaterialTheme.typography.bodySmall)

                        currencyList.forEach { code ->
                            // Rates editor row inputs
                            var textValue by remember(code) { mutableStateOf(viewModel.exchangeRates[code].toString()) }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(code, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp))
                                OutlinedTextField(
                                    value = textValue,
                                    onValueChange = {
                                        textValue = it
                                        viewModel.updateExchangeRateStr(code, it)
                                    },
                                    modifier = Modifier.weight(1f).testTag("rate_input_$code"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showEditRatesDialog = false }) {
                        Text("Done")
                    }
                }
            )
        }
    }
}
