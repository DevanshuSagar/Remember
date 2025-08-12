package com.example.remember.ui.card_detail
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.remember.data.db.ContentType
import com.example.remember.ui.navigation.ParentScreens
import com.example.remember.ui.today.ZoomableImage
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    parentNavController: NavController,
    viewModel: CardDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cardWithContent = uiState.cardWithContent
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    if (selectedImageUri != null) {
        Dialog(
            onDismissRequest = { selectedImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.8f)) {
                ZoomableImage(imageUri = selectedImageUri!!)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(cardWithContent?.card?.topic ?: "Loading...")
                },
                navigationIcon = {
                    IconButton(onClick = { parentNavController.navigateUp() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (cardWithContent != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (cardWithContent.card.notes.isNotBlank()) {
                    Text(cardWithContent.card.notes, style = MaterialTheme.typography.bodyLarge)
                }

                if (cardWithContent.contentItems.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text("Attachments", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cardWithContent.contentItems) { item ->
                            when(item.type) {
                                ContentType.TEXT -> TODO()
                                ContentType.IMAGE -> {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = item.content.toUri()),
                                        contentDescription = "Attachment",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedImageUri = item.content.toUri() },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                ContentType.PDF -> {
                                    IconButton(
                                        onClick = {
                                            val encodedUri = URLEncoder.encode(item.content, StandardCharsets.UTF_8.toString())
                                            parentNavController.navigate(ParentScreens.PdfViewer.withArgs(encodedUri))
                                        },
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline,
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Icon(Icons.Default.AccountBox, "PDF")
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