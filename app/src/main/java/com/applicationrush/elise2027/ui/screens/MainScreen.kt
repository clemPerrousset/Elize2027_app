package com.applicationrush.elise2027.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.applicationrush.elise2027.data.model.CANDIDATES
import com.applicationrush.elise2027.ui.theme.Background
import com.applicationrush.elise2027.ui.theme.OnSurface
import com.applicationrush.elise2027.ui.theme.OnSurfaceMuted
import com.applicationrush.elise2027.ui.theme.Surface
import com.applicationrush.elise2027.ui.viewmodel.VoteViewModel

private enum class MainTab { VOTES, PROGRESSION }

// Écran racine une fois l'onboarding terminé : bottom nav à deux onglets,
// "Votes" (écran existant, inchangé) et "Progression" (nouveau, historique des votes).
// La vue Programme s'ouvre par-dessus, depuis le bouton "Programme" d'une carte candidat ;
// la vue Comparer, par-dessus encore, depuis "Comparer" (carte ou vue Programme).
@Composable
fun MainScreen(viewModel: VoteViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.VOTES) }
    var programmeCandidateId by rememberSaveable { mutableStateOf<String?>(null) }
    var compareCandidateId by rememberSaveable { mutableStateOf<String?>(null) }
    val disclaimerHidden by viewModel.programmeDisclaimerHidden.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
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
                    MainTab.VOTES -> VoteScreen(
                        viewModel = viewModel,
                        onOpenProgramme = { programmeCandidateId = it.id },
                        onCompare = { compareCandidateId = it.id },
                    )
                    MainTab.PROGRESSION -> ProgressionScreen(viewModel = viewModel)
                }
            }
        }

        // Vue Programme par-dessus les onglets : la liste des votes reste montée, le scroll est conservé au retour.
        val programmeCandidate = programmeCandidateId?.let { id -> CANDIDATES.firstOrNull { it.id == id } }
        AnimatedContent(
            targetState = programmeCandidate,
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally { it } togetherWith fadeOut()
                } else {
                    fadeIn() togetherWith slideOutHorizontally { it }
                }
            },
            modifier = Modifier.fillMaxSize(),
            label = "programme_overlay",
        ) { candidate ->
            if (candidate != null) {
                ProgrammeScreen(
                    info = candidate,
                    onBack = { programmeCandidateId = null },
                    onCompare = { compareCandidateId = candidate.id },
                    showDisclaimer = disclaimerHidden == false,
                    onDisclaimerAccepted = { dontShowAgain ->
                        if (dontShowAgain) viewModel.hideProgrammeDisclaimer()
                    },
                )
            }
        }

        // Vue Comparer : au retour, on retrouve l'écran d'où elle a été ouverte (liste ou programme).
        AnimatedContent(
            targetState = compareCandidateId,
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally { it } togetherWith fadeOut()
                } else {
                    fadeIn() togetherWith slideOutHorizontally { it }
                }
            },
            modifier = Modifier.fillMaxSize(),
            label = "compare_overlay",
        ) { candidateId ->
            if (candidateId != null) {
                ProgrammeCompareScreen(
                    initialLeftId = candidateId,
                    onBack = { compareCandidateId = null },
                    // Ouverte depuis la vue Programme, l'avertissement vient d'y être vu.
                    showDisclaimer = disclaimerHidden == false && programmeCandidateId == null,
                    onDisclaimerAccepted = { dontShowAgain ->
                        if (dontShowAgain) viewModel.hideProgrammeDisclaimer()
                    },
                )
            }
        }
    }
}
