package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.BirthDataScreen
import com.example.ui.TransitScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BirthDataViewModel
import com.example.viewmodel.TransitViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "transit"
                    ) {
                        composable("transit") {
                            val transitViewModel: TransitViewModel = viewModel()
                            val uiState = transitViewModel.uiState.collectAsState().value

                            TransitScreen(
                                state = uiState,
                                viewModel = transitViewModel,
                                onNavigateToBirthData = {
                                    navController.navigate("birth_data")
                                }
                            )
                        }

                        composable("birth_data") {
                            val birthViewModel: BirthDataViewModel = viewModel()

                            BirthDataScreen(
                                viewModel = birthViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
