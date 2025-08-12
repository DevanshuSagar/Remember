package com.example.remember.ui.add_edit_card

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.remember.data.db.ContentType
import com.example.remember.ui.utils.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    parentNavController: NavController,
    viewModel: AddEditCardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showIntervalDialog by remember { mutableStateOf(false) }

    val titleText = if (uiState.originalCard == null) "Add Card" else "Edit"

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris -> if (uris.isNotEmpty()) viewModel.addImages(uris) }
    )
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success -> if (success) tempImageUri?.let { viewModel.addImage(it) } }
    )

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.addPdf(it) }
        }
    )

    if (showIntervalDialog) {
        IntervalsDialog(
            currentSet = uiState.selectedIntervalSet,
            allSets = uiState.availableIntervalSets,
            onDismiss = { showIntervalDialog = false },
            onSelect = {
                viewModel.onIntervalSetSelected(it)
                showIntervalDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$titleText Note") },
                navigationIcon = {
                    IconButton(onClick = { parentNavController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.saveCard()
                    parentNavController.navigateUp()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Save")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.topic,
                onValueChange = viewModel::onTopicChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Topic") },
                placeholder = { Text("Enter your Topic here") },
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.selectedIntervalSet.name,
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showIntervalDialog = true },
                label = { Text("Intervals") },
                readOnly = true,
                enabled = false,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Intervals") },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 150.dp),
                label = { Text("Notes") },
                placeholder = { Text("Write your notes...") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    tempImageUri = FileUtils.createTempImageUri(context)
                    cameraLauncher.launch(tempImageUri!!)
                }) {
                    Icon(Icons.Default.Add, "Take Photo")
                }
                IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.ArrowDropDown, "Add Photos from Gallery")
                }
                IconButton(onClick = { filePickerLauncher.launch("application/pdf") }) {
                    Icon(Icons.Default.AccountBox, "Add File")
                }
            }

            if (uiState.draftContentItems.isNotEmpty()) {
                Text("Attachments:", style = MaterialTheme.typography.titleSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.draftContentItems) { item ->
                        when (item.type) {
                            ContentType.IMAGE -> {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = item.persistentUri.toUri()),
                                        contentDescription = "Selected image",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline,
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeDraftContentItem(item) },
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Image",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .background(
                                                    Color.Black.copy(alpha = 0.5f),
                                                    CircleShape
                                                )
                                                .padding(4.dp)
                                                .size(16.dp)
                                        )
                                    }
                                }
                            }

                            ContentType.TEXT -> TODO()
                            ContentType.PDF -> {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBox,
                                        contentDescription = "PDF",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .align(Alignment.Center)
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeDraftContentItem(item) },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove PDF",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .background(
                                                    Color.Black.copy(alpha = 0.5f),
                                                    CircleShape
                                                )
                                                .padding(4.dp)
                                                .size(16.dp)
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
}

@Composable
private fun IntervalsDialog(
    currentSet: IntervalSet,
    allSets: List<IntervalSet>,
    onDismiss: () -> Unit,
    onSelect: (IntervalSet) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Interval Set") },
        text = {
            Column {
                allSets.forEach { set ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (set == currentSet),
                                onClick = { onSelect(set) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (set == currentSet), onClick = null)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(set.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                set.intervals.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}