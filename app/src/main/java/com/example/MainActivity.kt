package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.CalculationRepository
import com.example.ui.screens.AgeScreen
import com.example.ui.screens.BasicCalcScreen
import com.example.ui.screens.BmiScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.EmiScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.GpaScreen
import com.example.ui.screens.HubScreen
import com.example.ui.screens.ScientificCalcScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
  @OptIn(ExperimentalAnimationApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val database = remember { AppDatabase.getDatabase(applicationContext) }
      val repository = remember { CalculationRepository(database.calculationDao()) }
      val viewModel: AppViewModel = viewModel(
        factory = AppViewModelFactory(application, repository)
      )

      val themeState by viewModel.themeState.collectAsState()
      val darkTheme = when (themeState) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
      }

      MyApplicationTheme(darkTheme = darkTheme) {
        var currentTab by remember { mutableStateOf(0) } // 0 = Tools Hub, 1 = Favorites/History, 2 = Settings
        var activeTool by remember { mutableStateOf<String?>(null) } // Route to active full calculators

        Scaffold(
          bottomBar = {
            if (activeTool == null) {
              NavigationBar {
                NavigationBarItem(
                  selected = currentTab == 0,
                  onClick = { currentTab = 0 },
                  icon = {
                    Icon(
                      imageVector = if (currentTab == 0) Icons.Filled.Category else Icons.Outlined.Category,
                      contentDescription = "Utilities"
                    )
                  },
                  label = { Text("Tools") }
                )
                NavigationBarItem(
                  selected = currentTab == 1,
                  onClick = { currentTab = 1 },
                  icon = {
                    Icon(
                      imageVector = if (currentTab == 1) Icons.Filled.History else Icons.Outlined.History,
                      contentDescription = "Favorites"
                    )
                  },
                  label = { Text("Favorites") }
                )
                NavigationBarItem(
                  selected = currentTab == 2,
                  onClick = { currentTab = 2 },
                  icon = {
                    Icon(
                      imageVector = if (currentTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                      contentDescription = "Settings"
                    )
                  },
                  label = { Text("Settings") }
                )
              }
            }
          },
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(
              targetState = activeTool,
              transitionSpec = {
                fadeIn() with fadeOut()
              }
            ) { tool ->
              when (tool) {
                "BASIC" -> BasicCalcScreen(
                  viewModel = viewModel,
                  onNavigateBack = { activeTool = null }
                )
                "SCIENTIFIC" -> ScientificCalcScreen(
                  viewModel = viewModel,
                  onNavigateBack = { activeTool = null }
                )
                "GPA" -> GpaScreen(
                  viewModel = viewModel,
                  onNavigateBack = { activeTool = null }
                )
                "BMI" -> BmiScreen(
                  viewModel = viewModel,
                  onNavigateBack = { activeTool = null }
                )
                "AGE" -> AgeScreen(
                  viewModel = viewModel,
                  onNavigateBack = { activeTool = null }
                )
                "EMI" -> EmiScreen(
                  viewModel = viewModel,
                  onNavigateBack = { activeTool = null }
                )
                "CONVERTER_UNIT" -> ConverterScreen(
                  viewModel = viewModel,
                  initialTab = 0,
                  onNavigateBack = { activeTool = null }
                )
                "CONVERTER_CURRENCY" -> ConverterScreen(
                  viewModel = viewModel,
                  initialTab = 1,
                  onNavigateBack = { activeTool = null }
                )
                else -> {
                  // Standard Root Navigation Bar views
                  when (currentTab) {
                    0 -> HubScreen(
                      onNavigateToTool = { activeTool = it }
                    )
                    1 -> FavoritesScreen(
                      viewModel = viewModel
                    )
                    2 -> SettingsScreen(
                      viewModel = viewModel
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
