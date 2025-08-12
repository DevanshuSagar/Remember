package com.example.remember.ui.navigation

import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings

sealed class ParentScreens(val route: String) {
    data object Main: ParentScreens("main_screen")
    data object AddEditCard: ParentScreens("add_edit_card_screen/{cardId}") {
        fun routeToAdd(): String {
            return "add_edit_card_screen/-1"
        }
        fun routeToEdit(cardId: Int): String {
            return "add_edit_card_screen/$cardId"
        }
    }
    data object CardDetail : ParentScreens("card_detail_screen/{cardId}") {
        fun withArgs(cardId: Int): String {
            return "card_detail_screen/$cardId"
        }
    }
    data object PdfViewer: ParentScreens("pdf_viewer_screen/{pdfUri}") {
        fun withArgs(pdfUri: String): String {
            return "pdf_viewer_screen/$pdfUri"
        }
    }
}

sealed class ChildScreens(val route: String, val label: String? = null, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    data object Today : ChildScreens("today_screen", "Today", androidx.compose.material.icons.Icons.Default.CheckCircle)
    data object AllTasks : ChildScreens("all_tasks_screen", "All Cards", androidx.compose.material.icons.Icons.Default.DateRange)
    data object Settings : ChildScreens("settings_screen", "Settings", androidx.compose.material.icons.Icons.Default.Settings)
}

