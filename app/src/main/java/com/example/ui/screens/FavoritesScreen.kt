package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalculationRecord
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.historyRecords.collectAsState()
    val favorites by viewModel.favoriteRecords.collectAsState()

    var activeTabState by remember { mutableStateOf(0) } // 0 = Favorites, 1 = History
    val tabs = remember { listOf("My Favorites", "Calculation Logs") }

    var showNoteDialogItem by remember { mutableStateOf<CalculationRecord?>(null) }
    var noteInputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites & Logs", fontWeight = FontWeight.Bold) },
                actions = {
                    if (activeTabState == 1 && history.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearHistoryOnly() },
                            modifier = Modifier.testTag("btn_clear_history_only")
                        ) {
                            Text("Clear History", color = MaterialTheme.colorScheme.error)
                        }
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
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Section Switch Tab Row
            TabRow(
                selectedTabIndex = activeTabState,
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth().testTag("fav_log_tabs")
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = activeTabState == idx,
                        onClick = { activeTabState = idx },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("fav_tab_button_$idx")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val activeList = if (activeTabState == 0) favorites else history

            if (activeList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (activeTabState == 0) Icons.Default.StarBorder else Icons.Default.History,
                            contentDescription = "Empty View",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (activeTabState == 0) "No favorites saved yet" else "No conversion or calculations logs",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (activeTabState == 0) "Star calculations or save GPA term indices to see them here"
                            else "Calculations from basic, sci, BMI and others are logged here offline",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .testTag("calc_records_list")
                ) {
                    items(activeList, key = { it.id }) { item ->
                        CalculationRecordRow(
                            record = item,
                            onToggleFav = { viewModel.toggleFavorite(item) },
                            onDelete = { viewModel.deleteRecord(item) },
                            onEditNote = {
                                showNoteDialogItem = item
                                noteInputText = item.notes
                            }
                        )
                    }
                }
            }
        }

        // Note Edit Dialog
        if (showNoteDialogItem != null) {
            AlertDialog(
                onDismissRequest = { showNoteDialogItem = null },
                title = { Text("Update Label Notes") },
                text = {
                    Column {
                        Text("Add reference label details or calculation descriptions.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = noteInputText,
                            onValueChange = { noteInputText = it },
                            placeholder = { Text("e.g. mortgage loan principal, term 1 GPA") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("dialog_note_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showNoteDialogItem?.let {
                                viewModel.updateRecordNotes(it, noteInputText)
                            }
                            showNoteDialogItem = null
                        },
                        modifier = Modifier.testTag("dialog_confirm_note")
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNoteDialogItem = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun CalculationRecordRow(
    record: CalculationRecord,
    onToggleFav: () -> Unit,
    onDelete: () -> Unit,
    onEditNote: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MM-dd HH:mm", Locale.US) }
    val dateStr = formatter.format(Date(record.timestamp))

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row (Type label, Note Badge and Timestamp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val badgeColor = when (record.type) {
                        "BASIC", "SCIENTIFIC" -> MaterialTheme.colorScheme.primary
                        "GPA" -> MaterialTheme.colorScheme.tertiary
                        "BMI" -> Color(0xFFE91E63)
                        "AGE" -> Color(0xFF673AB7)
                        "EMI" -> Color(0xFF3F51B5)
                        else -> MaterialTheme.colorScheme.secondary
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = record.type,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = badgeColor
                        )
                    }

                    if (record.notes.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = record.notes,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Values
            Column(modifier = Modifier.fillMaxWidth().padding(end = 40.dp)) {
                Text(
                    text = record.input,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = record.result,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(4.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add notes
                IconButton(onClick = onEditNote, modifier = Modifier.size(36.dp).testTag("btn_edit_note_${record.id}")) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit note",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Toggle Favorite
                IconButton(onClick = onToggleFav, modifier = Modifier.size(36.dp).testTag("btn_toggle_fav_${record.id}")) {
                    Icon(
                        imageVector = if (record.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Toggle favorite",
                        modifier = Modifier.size(20.dp),
                        tint = if (record.isFavorite) Color(0xFFFFD600) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Delete calculation
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp).testTag("btn_delete_calc_${record.id}")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
