package com.example.remember

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.remember.data.db.Theme
import com.example.remember.ui.add_edit_card.AddEditCardScreen
import com.example.remember.ui.card_detail.CardDetailScreen
import com.example.remember.ui.navigation.MainScreen
import com.example.remember.ui.navigation.ParentScreens
import com.example.remember.ui.pdf_viewer.PdfViewerScreen
import com.example.remember.ui.settings.SettingsViewModel
import com.example.remember.ui.theme.RememberTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // We can handle if the permission is granted or denied here if needed
        }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU is API 33
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        askNotificationPermission()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                ParentScreen()
            }
        }
    }
}

@Composable
fun ParentScreen() {
    val navController = rememberNavController()

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val settings = settingsState.settings

    RememberTheme(theme = settings?.theme ?: Theme.SYSTEM) {
        NavHost(navController, startDestination = ParentScreens.Main.route) {
            composable(ParentScreens.Main.route) { MainScreen(parentNavController = navController) }

            composable(
                route = ParentScreens.AddEditCard.route,
                arguments = listOf(
                    navArgument("cardId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) {
                AddEditCardScreen(parentNavController = navController)
            }

            composable(
                route = ParentScreens.CardDetail.route,
                arguments = listOf(
                    navArgument("cardId") {
                        type = NavType.IntType
                    }
                )
            ) {
                CardDetailScreen(parentNavController = navController)
            }

            composable(
                route = ParentScreens.PdfViewer.route,
                arguments = listOf(navArgument("pdfUri") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedUri = backStackEntry.arguments?.getString("pdfUri") ?: ""
                val pdfUri = URLDecoder.decode(encodedUri, StandardCharsets.UTF_8.toString())
                PdfViewerScreen(parentNavController = navController, pdfUri = pdfUri)
            }

        }
    }
}

