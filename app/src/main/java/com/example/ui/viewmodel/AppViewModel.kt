package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationRecord
import com.example.data.CalculationRepository
import com.example.util.MathEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.Calendar
import kotlin.math.pow

// GPA Course definition
data class GpaCourse(
    val id: String,
    val name: String,
    val creditHours: Double,
    val grade: String
)

class AppViewModel(
    application: Application,
    private val repository: CalculationRepository
) : AndroidViewModel(application) {

    // Theme Management
    private val _themeState = MutableStateFlow("System") // System, Dark, Light
    val themeState: StateFlow<String> = _themeState.asStateFlow()

    fun setTheme(theme: String) {
        _themeState.value = theme
    }

    // -------------------------------------------------------------
    // DATA LAYER INTEGRATION (History & Favorites)
    // -------------------------------------------------------------
    val historyRecords: StateFlow<List<CalculationRecord>> = repository.allCalculations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteRecords: StateFlow<List<CalculationRecord>> = repository.favoriteCalculations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveCalculation(type: String, input: String, result: String, notes: String = "", isFavorite: Boolean = false) {
        viewModelScope.launch {
            repository.insert(
                CalculationRecord(
                    type = type,
                    input = input,
                    result = result,
                    isFavorite = isFavorite,
                    notes = notes
                )
            )
        }
    }

    fun toggleFavorite(record: CalculationRecord) {
        viewModelScope.launch {
            repository.update(record.copy(isFavorite = !record.isFavorite))
        }
    }

    fun deleteRecord(record: CalculationRecord) {
        viewModelScope.launch {
            repository.delete(record)
        }
    }

    fun updateRecordNotes(record: CalculationRecord, notes: String) {
        viewModelScope.launch {
            repository.update(record.copy(notes = notes))
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun clearHistoryOnly() {
        viewModelScope.launch {
            repository.clearHistoryOnly()
        }
    }

    // -------------------------------------------------------------
    // 1. BASIC & 2. SCIENTIFIC CALCULATOR STATE
    // -------------------------------------------------------------
    private val _calcInput = MutableStateFlow("")
    val calcInput: StateFlow<String> = _calcInput.asStateFlow()

    private val _calcResult = MutableStateFlow("")
    val calcResult: StateFlow<String> = _calcResult.asStateFlow()

    fun onCalcInput(char: Char) {
        _calcInput.value = _calcInput.value + char
        evaluateExpressionRealtime()
    }

    fun onCalcInputStr(str: String) {
        _calcInput.value = _calcInput.value + str
        evaluateExpressionRealtime()
    }

    fun onCalcBackspace() {
        val current = _calcInput.value
        if (current.isNotEmpty()) {
            _calcInput.value = current.dropLast(1)
            evaluateExpressionRealtime()
        }
    }

    fun onCalcClear() {
        _calcInput.value = ""
        _calcResult.value = ""
    }

    fun onCalcEquals(type: String = "BASIC") {
        val expr = _calcInput.value
        if (expr.isEmpty()) return
        try {
            val res = MathEvaluator.evaluate(expr)
            val formatted = formatResult(res)
            _calcResult.value = formatted
            // Store to database
            saveCalculation(type = type, input = expr, result = formatted)
            _calcInput.value = formatted
        } catch (e: Exception) {
            _calcResult.value = "Error"
        }
    }

    private fun evaluateExpressionRealtime() {
        val expr = _calcInput.value
        if (expr.isEmpty()) {
            _calcResult.value = ""
            return
        }
        try {
            // Check if standard operators or parentheses exist
            if (expr.any { it in "+-×÷^()" } || expr.contains("sin") || expr.contains("cos") || expr.contains("tan")) {
                val res = MathEvaluator.evaluate(expr)
                _calcResult.value = formatResult(res)
            }
        } catch (e: Exception) {
            // Silent error for incomplete realtime calculations
        }
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return "∞"
        val df = DecimalFormat("#.########")
        return df.format(value)
    }

    // -------------------------------------------------------------
    // 3. GPA CALCULATOR STATE
    // -------------------------------------------------------------
    val gpaCourses = mutableStateListOf<GpaCourse>(
        GpaCourse("1", "Semester 1 Item A", 3.0, "A"),
        GpaCourse("2", "Semester 1 Item B", 4.0, "B")
    )

    private val _calculatedGpa = MutableStateFlow("0.0")
    val calculatedGpa: StateFlow<String> = _calculatedGpa.asStateFlow()

    fun addGpaCourse() {
        val newId = System.currentTimeMillis().toString()
        gpaCourses.add(GpaCourse(newId, "Course ${gpaCourses.size + 1}", 3.0, "A"))
        calculateGpa()
    }

    fun updateGpaCourse(course: GpaCourse) {
        val idx = gpaCourses.indexOfFirst { it.id == course.id }
        if (idx != -1) {
            gpaCourses[idx] = course
            calculateGpa()
        }
    }

    fun removeGpaCourse(courseId: String) {
        gpaCourses.removeAll { it.id == courseId }
        calculateGpa()
    }

    fun calculateGpa() {
        if (gpaCourses.isEmpty()) {
            _calculatedGpa.value = "0.00"
            return
        }
        var totalPoints = 0.0
        var totalCredits = 0.0
        for (c in gpaCourses) {
            val pointValue = when (c.grade) {
                "A" -> 4.0
                "A-" -> 3.7
                "B+" -> 3.3
                "B" -> 3.0
                "B-" -> 2.7
                "C+" -> 2.3
                "C" -> 2.0
                "C-" -> 1.7
                "D+" -> 1.3
                "D" -> 1.0
                "F" -> 0.0
                else -> 4.0
            }
            totalPoints += pointValue * c.creditHours
            totalCredits += c.creditHours
        }
        val gpa = if (totalCredits == 0.0) 0.0 else totalPoints / totalCredits
        val df = DecimalFormat("#.##")
        _calculatedGpa.value = df.format(gpa)
    }

    fun saveGpaToFavorites(termLabel: String) {
        val totalCredits = gpaCourses.sumOf { it.creditHours }
        val gpaVal = _calculatedGpa.value
        val summaryInput = "Courses: ${gpaCourses.size}, Credits: $totalCredits"
        val resultString = "GPA: $gpaVal"
        saveCalculation("GPA", summaryInput, resultString, termLabel, isFavorite = true)
    }

    // -------------------------------------------------------------
    // 4. BMI CALCULATOR STATE
    // -------------------------------------------------------------
    private val _bmiSystem = MutableStateFlow("Metric") // Metric, Imperial
    val bmiSystem: StateFlow<String> = _bmiSystem.asStateFlow()

    private val _bmiWeight = MutableStateFlow("70")
    val bmiWeight: StateFlow<String> = _bmiWeight.asStateFlow()

    private val _bmiHeight = MutableStateFlow("175")
    val bmiHeight: StateFlow<String> = _bmiHeight.asStateFlow()

    private val _bmiValue = MutableStateFlow("")
    val bmiValue: StateFlow<String> = _bmiValue.asStateFlow()

    private val _bmiCategory = MutableStateFlow("")
    val bmiCategory: StateFlow<String> = _bmiCategory.asStateFlow()

    fun setBmiSystem(sys: String) {
        _bmiSystem.value = sys
        if (sys == "Metric") {
            _bmiWeight.value = "70"
            _bmiHeight.value = "175"
        } else {
            _bmiWeight.value = "154" // lbs
            _bmiHeight.value = "69"  // inches
        }
        _bmiValue.value = ""
        _bmiCategory.value = ""
    }

    fun setBmiWeight(w: String) {
        _bmiWeight.value = w
    }

    fun setBmiHeight(h: String) {
        _bmiHeight.value = h
    }

    fun calculateBmi() {
        val wVal = _bmiWeight.value.toDoubleOrNull() ?: 0.0
        val hVal = _bmiHeight.value.toDoubleOrNull() ?: 0.0
        if (wVal <= 0.0 || hVal <= 0.0) return

        val bmi = if (_bmiSystem.value == "Metric") {
            val meters = hVal / 100.0
            wVal / (meters * meters)
        } else {
            703 * wVal / (hVal * hVal)
        }

        val df = DecimalFormat("#.#")
        val formatted = df.format(bmi)
        _bmiValue.value = formatted

        _bmiCategory.value = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }

        // Auto save to history
        val inputSum = if (_bmiSystem.value == "Metric") {
            "Weight: $wVal kg, Height: $hVal cm"
        } else {
            "Weight: $wVal lbs, Height: $hVal in"
        }
        val resultSum = "BMI: $formatted (${_bmiCategory.value})"
        saveCalculation("BMI", inputSum, resultSum)
    }

    // -------------------------------------------------------------
    // 5. AGE CALCULATOR STATE
    // -------------------------------------------------------------
    private val _birthYear = MutableStateFlow(1998)
    private val _birthMonth = MutableStateFlow(5) // (0 - 11)
    private val _birthDay = MutableStateFlow(15)

    private val _targetYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _targetMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _targetDay = MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))

    private val _ageResultYears = MutableStateFlow(0)
    val ageResultYears: StateFlow<Int> = _ageResultYears.asStateFlow()

    private val _ageResultMonths = MutableStateFlow(0)
    val ageResultMonths: StateFlow<Int> = _ageResultMonths.asStateFlow()

    private val _ageResultDays = MutableStateFlow(0)
    val ageResultDays: StateFlow<Int> = _ageResultDays.asStateFlow()

    private val _nextBirthdayDays = MutableStateFlow(0)
    val nextBirthdayDays: StateFlow<Int> = _nextBirthdayDays.asStateFlow()

    fun setBirthDate(year: Int, month: Int, day: Int) {
        _birthYear.value = year
        _birthMonth.value = month
        _birthDay.value = day
        calculateAge()
    }

    fun setTargetDate(year: Int, month: Int, day: Int) {
        _targetYear.value = year
        _targetMonth.value = month
        _targetDay.value = day
        calculateAge()
    }

    fun calculateAge() {
        val dob = Calendar.getInstance().apply {
            set(_birthYear.value, _birthMonth.value, _birthDay.value, 0, 0, 0)
        }
        val target = Calendar.getInstance().apply {
            set(_targetYear.value, _targetMonth.value, _targetDay.value, 0, 0, 0)
        }

        if (target.before(dob)) {
            _ageResultYears.value = 0
            _ageResultMonths.value = 0
            _ageResultDays.value = 0
            _nextBirthdayDays.value = 0
            return
        }

        var years = target.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        var months = target.get(Calendar.MONTH) - dob.get(Calendar.MONTH)
        var days = target.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months--
            val prevMonthCal = Calendar.getInstance().apply {
                time = target.time
                add(Calendar.MONTH, -1)
            }
            days += prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

        if (months < 0) {
            years--
            months += 12
        }

        _ageResultYears.value = years
        _ageResultMonths.value = months
        _ageResultDays.value = days

        // Days until next birthday
        val nextBday = Calendar.getInstance().apply {
            set(Calendar.MONTH, _birthMonth.value)
            set(Calendar.DAY_OF_MONTH, _birthDay.value)
            set(Calendar.YEAR, target.get(Calendar.YEAR))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (nextBday.before(target) || nextBday.equals(target)) {
            nextBday.add(Calendar.YEAR, 1)
        }

        val diffInMillis = nextBday.timeInMillis - target.timeInMillis
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        _nextBirthdayDays.value = diffInDays

        // Auto Save to History
        val dobStr = "${_birthYear.value}-${_birthMonth.value + 1}-${_birthDay.value}"
        val targetStr = "${_targetYear.value}-${_targetMonth.value + 1}-${_targetDay.value}"
        val inputSum = "DOB: $dobStr at Target: $targetStr"
        val resultSum = "$years Y, $months M, $days D"
        saveCalculation("AGE", inputSum, resultSum)
    }

    // -------------------------------------------------------------
    // 6. EMI CALCULATOR STATE
    // -------------------------------------------------------------
    private val _emiAmount = MutableStateFlow("100000")
    val emiAmount: StateFlow<String> = _emiAmount.asStateFlow()

    private val _emiRate = MutableStateFlow("8.5")
    val emiRate: StateFlow<String> = _emiRate.asStateFlow()

    private val _emiTenure = MutableStateFlow("5")
    val emiTenure: StateFlow<String> = _emiTenure.asStateFlow()

    private val _emiTenureUnit = MutableStateFlow("Years") // Years, Months
    val emiTenureUnit: StateFlow<String> = _emiTenureUnit.asStateFlow()

    private val _monthlyEmi = MutableStateFlow("0.0")
    val monthlyEmi: StateFlow<String> = _monthlyEmi.asStateFlow()

    private val _totalInterest = MutableStateFlow("0.0")
    val totalInterest: StateFlow<String> = _totalInterest.asStateFlow()

    private val _totalAmount = MutableStateFlow("0.0")
    val totalAmount: StateFlow<String> = _totalAmount.asStateFlow()

    fun setEmiAmount(v: String) = run { _emiAmount.value = v }
    fun setEmiRate(v: String) = run { _emiRate.value = v }
    fun setEmiTenure(v: String) = run { _emiTenure.value = v }
    fun setEmiTenureUnit(v: String) = run { _emiTenureUnit.value = v }

    fun calculateEmi() {
        val P = _emiAmount.value.toDoubleOrNull() ?: 0.0
        val annualRate = _emiRate.value.toDoubleOrNull() ?: 0.0
        val valTenure = _emiTenure.value.toDoubleOrNull() ?: 0.0

        if (P <= 0.0 || annualRate <= 0.0 || valTenure <= 0.0) return

        val n = if (_emiTenureUnit.value == "Years") valTenure * 12 else valTenure
        val r = (annualRate / 12) / 100.0

        // EMI Formula: [P * r * (1+r)^n] / [((1+r)^n) - 1]
        val emi = (P * r * (1 + r).pow(n)) / ((1 + r).pow(n) - 1)
        val totalPayment = emi * n
        val totInterest = totalPayment - P

        val df = DecimalFormat("#.##")
        _monthlyEmi.value = df.format(emi)
        _totalInterest.value = df.format(totInterest)
        _totalAmount.value = df.format(totalPayment)

        // Save
        val tenureLabel = if (_emiTenureUnit.value == "Years") "$valTenure Years" else "$valTenure Months"
        val inputSum = "Principal: $P, Rate: $annualRate%, Tenure: $tenureLabel"
        val resultSum = "Monthly EMI: ${_monthlyEmi.value}, Total Interest: ${_totalInterest.value}"
        saveCalculation("EMI", inputSum, resultSum)
    }

    // -------------------------------------------------------------
    // 7. UNIT CONVERTER STATE
    // -------------------------------------------------------------
    private val _unitCategory = MutableStateFlow("Length") // Length, Weight, Area, Temperature, Speed, Volume
    val unitCategory: StateFlow<String> = _unitCategory.asStateFlow()

    private val _unitFrom = MutableStateFlow("m")
    val unitFrom: StateFlow<String> = _unitFrom.asStateFlow()

    private val _unitTo = MutableStateFlow("km")
    val unitTo: StateFlow<String> = _unitTo.asStateFlow()

    private val _unitValue = MutableStateFlow("1.0")
    val unitValue: StateFlow<String> = _unitValue.asStateFlow()

    private val _convertedUnitValue = MutableStateFlow("0.001")
    val convertedUnitValue: StateFlow<String> = _convertedUnitValue.asStateFlow()

    fun setUnitCategory(cat: String) {
        _unitCategory.value = cat
        val defaultUnits = when (cat) {
            "Length" -> Pair("m", "km")
            "Weight" -> Pair("kg", "g")
            "Area" -> Pair("m²", "ha")
            "Temperature" -> Pair("°C", "°F")
            "Speed" -> Pair("km/h", "mph")
            "Volume" -> Pair("L", "mL")
            else -> Pair("m", "km")
        }
        _unitFrom.value = defaultUnits.first
        _unitTo.value = defaultUnits.second
        _unitValue.value = "1.0"
        calculateUnitConversion()
    }

    fun setUnitFrom(v: String) {
        _unitFrom.value = v
        calculateUnitConversion()
    }

    fun setUnitTo(v: String) {
        _unitTo.value = v
        calculateUnitConversion()
    }

    fun setUnitValue(v: String) {
        _unitValue.value = v
        calculateUnitConversion()
    }

    fun calculateUnitConversion() {
        val num = _unitValue.value.toDoubleOrNull() ?: 0.0
        val from = _unitFrom.value
        val to = _unitTo.value
        val cat = _unitCategory.value

        val res = when (cat) {
            "Length" -> {
                val meters = when (from) {
                    "m" -> num
                    "km" -> num * 1000
                    "cm" -> num / 100
                    "mi" -> num * 1609.34
                    "yd" -> num * 0.9144
                    "ft" -> num * 0.3048
                    "in" -> num * 0.0254
                    else -> num
                }
                when (to) {
                    "m" -> meters
                    "km" -> meters / 1000
                    "cm" -> meters * 100
                    "mi" -> meters / 1609.34
                    "yd" -> meters / 0.9144
                    "ft" -> meters / 0.3048
                    "in" -> meters / 0.0254
                    else -> meters
                }
            }
            "Weight" -> {
                val grams = when (from) {
                    "g" -> num
                    "kg" -> num * 1000
                    "lbs" -> num * 453.592
                    "oz" -> num * 28.3495
                    else -> num
                }
                when (to) {
                    "g" -> grams
                    "kg" -> grams / 1000
                    "lbs" -> grams / 453.592
                    "oz" -> grams / 28.3495
                    else -> grams
                }
            }
            "Area" -> {
                val sqMeters = when (from) {
                    "m²" -> num
                    "km²" -> num * 1_000_000
                    "ft²" -> num * 0.092903
                    "ac" -> num * 4046.86
                    "ha" -> num * 10000
                    else -> num
                }
                when (to) {
                    "m²" -> sqMeters
                    "km²" -> sqMeters / 1_000_000
                    "ft²" -> sqMeters / 0.092903
                    "ac" -> sqMeters / 4046.86
                    "ha" -> sqMeters / 10000
                    else -> sqMeters
                }
            }
            "Temperature" -> {
                val celsius = when (from) {
                    "°C" -> num
                    "°F" -> (num - 32) * 5 / 9
                    "K" -> num - 273.15
                    else -> num
                }
                when (to) {
                    "°C" -> celsius
                    "°F" -> celsius * 9 / 5 + 32
                    "K" -> celsius + 273.15
                    else -> celsius
                }
            }
            "Speed" -> {
                val mPerSec = when (from) {
                    "m/s" -> num
                    "km/h" -> num / 3.6
                    "mph" -> num * 0.44704
                    "knots" -> num * 0.514444
                    else -> num
                }
                when (to) {
                    "m/s" -> mPerSec
                    "km/h" -> mPerSec * 3.6
                    "mph" -> mPerSec / 0.44704
                    "knots" -> mPerSec / 0.514444
                    else -> mPerSec
                }
            }
            "Volume" -> {
                val liters = when (from) {
                    "L" -> num
                    "mL" -> num / 1000
                    "gal" -> num * 3.78541
                    "cups" -> num * 0.236588
                    else -> num
                }
                when (to) {
                    "L" -> liters
                    "mL" -> liters * 1000
                    "gal" -> liters / 3.78541
                    "cups" -> liters / 0.236588
                    else -> liters
                }
            }
            else -> num
        }

        val df = DecimalFormat("#.######")
        _convertedUnitValue.value = df.format(res)
    }

    // -------------------------------------------------------------
    // 8. CURRENCY CONVERTER STATE (Real and Offline Customizable exchange rates)
    // -------------------------------------------------------------
    val exchangeRates = mutableMapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "GBP" to 0.79,
        "JPY" to 156.5,
        "CAD" to 1.36,
        "AUD" to 1.50,
        "INR" to 83.3,
        "CNY" to 7.24,
        "SGD" to 1.35
    )

    private val _currencyFrom = MutableStateFlow("USD")
    val currencyFrom: StateFlow<String> = _currencyFrom.asStateFlow()

    private val _currencyTo = MutableStateFlow("EUR")
    val currencyTo: StateFlow<String> = _currencyTo.asStateFlow()

    private val _currencyValue = MutableStateFlow("1.0")
    val currencyValue: StateFlow<String> = _currencyValue.asStateFlow()

    private val _convertedCurrencyValue = MutableStateFlow("0.92")
    val convertedCurrencyValue: StateFlow<String> = _convertedCurrencyValue.asStateFlow()

    fun setCurrencyFrom(v: String) {
        _currencyFrom.value = v
        calculateCurrency()
    }

    fun setCurrencyTo(v: String) {
        _currencyTo.value = v
        calculateCurrency()
    }

    fun setCurrencyValue(v: String) {
        _currencyValue.value = v
        calculateCurrency()
    }

    fun updateExchangeRateStr(currency: String, rateString: String) {
        val rate = rateString.toDoubleOrNull() ?: return
        if (rate > 0.0) {
            exchangeRates[currency] = rate
            calculateCurrency()
        }
    }

    fun calculateCurrency() {
        val amount = _currencyValue.value.toDoubleOrNull() ?: 0.0
        val from = _currencyFrom.value
        val to = _currencyTo.value

        val rateFrom = exchangeRates[from] ?: 1.0
        val rateTo = exchangeRates[to] ?: 1.0

        // Convert amount from base back to target
        val inUsd = amount / rateFrom
        val converted = inUsd * rateTo

        val df = DecimalFormat("#.####")
        _convertedCurrencyValue.value = df.format(converted)

        // Save
        val inputSum = "$amount $from to $to"
        val resultSum = "${_convertedCurrencyValue.value} $to"
        saveCalculation("CURRENCY", inputSum, resultSum)
    }

    init {
        // Initial setup/calculations
        calculateGpa()
    }
}

// ViewModel Factory Provider for simple injection
class AppViewModelFactory(
    private val application: Application,
    private val repository: CalculationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
