package com.example.remember.ui.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.remember.data.db.CardWithContent
import com.example.remember.ui.navigation.ParentScreens

@Composable
fun TodayScreen(
    parentNavController: NavController,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dueCards = uiState.dueCards

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (dueCards.isEmpty()) {
        AllDoneState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dueCards) { cardWithContent ->
                TodayCardItem(
                    cardWithContent = cardWithContent,
                    onRemember = { viewModel.onCardRemembered(cardWithContent) },
                    onForget = { viewModel.onCardForgotten(cardWithContent) },
                    onClick = {
                        parentNavController.navigate(ParentScreens.CardDetail.withArgs(cardWithContent.card.id))
                    }
                )
            }
        }
    }
}

@Composable
fun TodayCardItem(
    cardWithContent: CardWithContent,
    onRemember: () -> Unit,
    onForget: () -> Unit,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(cardWithContent.card.topic, style = MaterialTheme.typography.titleLarge)

            if (cardWithContent.card.notes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = cardWithContent.card.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4, // Limit the description to 4 lines
                    overflow = TextOverflow.Ellipsis // Add "..." at the end if it overflows
                )
            }

            if (cardWithContent.contentItems.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${cardWithContent.contentItems.size} attachment(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onForget,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("I Forgot") }

                Button(
                    onClick = onRemember,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("I Remembered") }
            }
        }
    }
}

@Composable
fun AllDoneState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "All done for today! 🎉",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
    }
}