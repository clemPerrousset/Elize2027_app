package com.applicationrush.elise2027

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.applicationrush.elise2027.ui.screens.OnboardingScreen
import com.applicationrush.elise2027.ui.screens.VoteScreen
import com.applicationrush.elise2027.ui.theme.ElizeTheme
import com.applicationrush.elise2027.ui.viewmodel.VoteViewModel
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : ComponentActivity() {

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { /* mise à jour annulée ou échouée — on laisse l'utilisateur continuer */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkForUpdate()
        setContent {
            ElizeTheme {
                val vm: VoteViewModel = viewModel()
                val onboardingDone by vm.onboardingDone.collectAsState()

                AnimatedContent(
                    targetState = onboardingDone,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition",
                ) { done ->
                    if (done) {
                        VoteScreen(viewModel = vm)
                    } else {
                        OnboardingScreen(onStart = { vm.markOnboardingDone() })
                    }
                }
            }
        }
    }

    private fun checkForUpdate() {
        val manager = AppUpdateManagerFactory.create(this)
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                manager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                )
            }
        }
    }
}
