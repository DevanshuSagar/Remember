package com.example.remember.ui.all_tasks
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.remember.data.db.CardWithContent
import com.example.remember.ui.navigation.ParentScreens
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

@Composable
fun AllTasksScreen(
//    childNavController: NavController,
    parentNavController: NavController,
    viewModel: AllTasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.cards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active cards.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.cards) { cardWithContent ->
                AllTasksCardItem(
                    cardWithContent = cardWithContent,
                    parentNavController = parentNavController,
                    onDelete = { viewModel.deleteCard(cardWithContent) }
                )
            }
        }
    }
}

@Composable
fun AllTasksCardItem(
    cardWithContent: CardWithContent,
    parentNavController: NavController,
    onDelete: () -> Unit
) {
    var showOptionsDialog by remember { mutableStateOf(false) }
    val card = cardWithContent.card

    val totalIntervals = card.intervals.size
    val currentIntervalIndex = card.intervals.indexOf(card.currentInterval)
    val progress = if (currentIntervalIndex != -1 && totalIntervals > 0) {
        (currentIntervalIndex + 1).toFloat() / totalIntervals.toFloat()
    } else {
        0f
    }
    val progressText = if (currentIntervalIndex != -1) {
        "Step ${currentIntervalIndex + 1}/$totalIntervals"
    } else {
        "Step 1/$totalIntervals"
    }
    val daysUntilReviewText = calculateDaysUntil(card.nextReviewDate)

    if (showOptionsDialog) {
        Dialog(onDismissRequest = { showOptionsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${card.topic} - $progressText",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showOptionsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = {
                            showOptionsDialog = false
                            parentNavController.navigate(ParentScreens.CardDetail.withArgs(cardId = card.id))
                        }) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Open")
                        }

                        Row {
                            IconButton(onClick = {
                                showOptionsDialog = false
                                parentNavController.navigate(ParentScreens.AddEditCard.routeToEdit(card.id))
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Card")
                            }
                            IconButton(onClick = {
                                onDelete()
                                showOptionsDialog = false
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Card")
                            }
                        }
                    }
                }
            }
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showOptionsDialog = true }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = card.topic, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cardWithContent.contentItems.isNotEmpty()) {
                    Text(
                        text = "${cardWithContent.contentItems.size} attachment(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(Modifier.height(1.dp))
                }
                Text(
                    text = daysUntilReviewText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (daysUntilReviewText.contains("Overdue")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f)
                )
                Text(text = progressText, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun calculateDaysUntil(date: Date): String {
    val today = LocalDate.now()
    val reviewDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val days = ChronoUnit.DAYS.between(today, reviewDate)

    return when {
        days < 0 -> "Overdue"
        days == 0L -> "Today"
        days == 1L -> "Tomorrow"
        else -> "in $days days"
    }
}