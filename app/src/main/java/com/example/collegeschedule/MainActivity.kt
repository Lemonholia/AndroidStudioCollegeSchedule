package com.example.collegeschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.collegeschedule.data.repository.ScheduleRepository
import com.example.collegeschedule.ui.favorites.FavoritesScreen
import com.example.collegeschedule.ui.schedule.ScheduleScreen
import com.example.collegeschedule.ui.theme.CollegeScheduleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CollegeScheduleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CollegeScheduleApp()
                }
            }
        }
    }
}

@Composable
fun CollegeScheduleApp() {
    val repository = ScheduleRepository()
    val navController = rememberNavController()

    // Общее состояние для избранных групп
    val favoriteGroups = remember { mutableStateOf(setOf<String>()) }

    // Функции для работы с избранным
    val toggleFavorite: (String) -> Unit = { group ->
        favoriteGroups.value = if (favoriteGroups.value.contains(group)) {
            favoriteGroups.value - group
        } else {
            favoriteGroups.value + group
        }
    }

    val isFavorite: (String) -> Boolean = { group ->
        favoriteGroups.value.contains(group)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Schedule.route
    ) {
        composable(Screen.Schedule.route) {
            ScheduleScreen(
                repository = repository,
                isFavorite = isFavorite,
                toggleFavorite = toggleFavorite,
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                repository = repository,
                favoriteGroups = favoriteGroups.value,
                onNavigateToSchedule = {
                    navController.navigate(Screen.Schedule.route) {
                        popUpTo(Screen.Schedule.route) { inclusive = true }
                    }
                },
                toggleFavorite = toggleFavorite
            )
        }
    }
}

// Определение экранов навигации
sealed class Screen(val route: String) {
    object Schedule : Screen("schedule")
    object Favorites : Screen("favorites")
}