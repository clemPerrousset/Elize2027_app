package com.applicationrush.elise2027.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.applicationrush.elise2027.ui.theme.Background
import com.applicationrush.elise2027.ui.theme.OnSurface
import com.applicationrush.elise2027.ui.theme.OnSurfaceMuted
import com.applicationrush.elise2027.ui.theme.Surface
import com.applicationrush.elise2027.ui.viewmodel.VoteViewModel

private enum class MainTab { VOTES, PROGRESSION }

// Écran racine une fois l'onboarding terminé : bottom nav à deux onglets,
// "Votes" (écran existant, inchangé) et "Progression" (nouveau, historique des votes).
@Composable
fun MainScreen(viewModel: VoteViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.VOTES) }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(containerColor = Surface) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.VOTES,
                    onClick = { selectedTab = MainTab.VOTES },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Votes") },
                    label = { Text("Votes") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnSurface,
                        selectedTextColor = OnSurface,
                        unselectedIconColor = OnSurfaceMuted,
                        unselectedTextColor = OnSurfaceMuted,
                        indicatorColor = Background,
                    ),
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.PROGRESSION,
                    onClick = {
                        selectedTab = MainTab.PROGRESSION
                        viewModel.loadHistoryIfNeeded()
                    },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Progression") },
                    label = { Text("Progression") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnSurface,
                        selectedTextColor = OnSurface,
                        unselectedIconColor = OnSurfaceMuted,
                        unselectedTextColor = OnSurfaceMuted,
                        indicatorColor = Background,
                    ),
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                MainTab.VOTES -> VoteScreen(viewModel = viewModel)
                MainTab.PROGRESSION -> ProgressionScreen(viewModel = viewModel)
            }
        }
    }
}
