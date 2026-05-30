package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.GpaCourse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpaScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val termGpa by viewModel.calculatedGpa.collectAsState()
    val courses = viewModel.gpaCourses
    var labelText by remember { mutableStateOf("Term 1 Cum") }
    var showSaveDialog by remember { mutableStateOf(false) }

    val gradeOptions = remember {
        listOf("A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPA Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.testTag("btn_save_gpa")
                    ) {
                        Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Save Term")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.addGpaCourse() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("btn_add_course")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Course")
            }
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
            // Metrics Widget
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Semester GPA",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = termGpa,
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.testTag("gpa_score_label")
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val totalCredits = courses.sumOf { it.creditHours }
                        Text(
                            text = "Total Credits",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = totalCredits.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Courses Column Header
            Text(
                text = "Course Catalog & Grades",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Course List
            if (courses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap + to add courses and compute active GPA",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("gpa_courses_list")
                ) {
                    items(courses, key = { it.id }) { course ->
                        GpaCourseRow(
                            course = course,
                            gradeOptions = gradeOptions,
                            onUpdate = { updated -> viewModel.updateGpaCourse(updated) },
                            onDelete = { viewModel.removeGpaCourse(course.id) }
                        )
                    }
                }
            }
        }

        // Save Dialog
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Save GPA Record") },
                text = {
                    Column {
                        Text("Give this GPA record a labels descriptor (e.g. sophomore, Fall 2026).")
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = labelText,
                            onValueChange = { labelText = it },
                            label = { Text("Label") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("gpa_save_label")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.saveGpaToFavorites(labelText)
                            showSaveDialog = false
                        },
                        modifier = Modifier.testTag("gpa_dialog_confirm")
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpaCourseRow(
    course: GpaCourse,
    gradeOptions: List<String>,
    onUpdate: (GpaCourse) -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Course input Name
            OutlinedTextField(
                value = course.name,
                onValueChange = { onUpdate(course.copy(name = it)) },
                placeholder = { Text("Course Name") },
                singleLine = true,
                modifier = Modifier
                    .weight(1.5f)
                    .padding(end = 8.dp)
                    .testTag("gpa_course_name_${course.id}"),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // Credit hour picker input
            OutlinedTextField(
                value = if (course.creditHours == 0.0) "" else course.creditHours.toString(),
                onValueChange = {
                    val doubleVal = it.toDoubleOrNull() ?: 0.0
                    onUpdate(course.copy(creditHours = doubleVal))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("Cr") },
                singleLine = true,
                modifier = Modifier
                    .weight(0.7f)
                    .padding(end = 8.dp)
                    .testTag("gpa_course_credits_${course.id}"),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            // Grade Select Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = course.grade,
                        onValueChange = {},
                        label = { Text("Grade") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        modifier = Modifier
                            .menuAnchor()
                            .testTag("gpa_course_grade_${course.id}")
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        gradeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onUpdate(course.copy(grade = option))
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Delete item
            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("btn_delete_course_${course.id}")
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Course")
            }
        }
    }
}
