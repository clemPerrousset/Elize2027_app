package com.applicationrush.elise2027.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applicationrush.elise2027.ui.theme.Background
import com.applicationrush.elise2027.ui.theme.OnSurface
import com.applicationrush.elise2027.ui.theme.OnSurfaceMuted
import com.applicationrush.elise2027.ui.theme.SurfaceVariant
import kotlinx.coroutines.delay

private const val URL_BACKEND = "https://github.com/clemPerrousset/Elyze_backend"
private const val URL_APP = "https://github.com/clemPerrousset/Elize2027_app"

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800),
        label = "onboarding_fade",
    )

    LaunchedEffect(Unit) {
        delay(200)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D0D2E), Background)))
            .alpha(alpha),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {

            // ── Header ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF7B7BFF)),
                contentAlignment = Alignment.Center,
            ) {
                Text("É", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Élysée 2027",
                color = OnSurface,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Le sondage présidentiel participatif",
                color = OnSurfaceMuted,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            // ── Feature chips 2×2 ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FeatureChip("🔒 Pseudonyme & sécurisé", Modifier.weight(1f))
                FeatureChip("🎁 Gratuit, sans pub", Modifier.weight(1f))
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FeatureChip("🇫🇷 Hébergé en France", Modifier.weight(1f))
                FeatureChip("📖 Code open source", Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            // ── Description ──────────────────────────────────────────────
            Text(
                "Votez pour votre candidat préféré et découvrez en temps réel l'opinion " +
                "des autres participants. Aucune donnée personnelle n'est collectée — " +
                "votre vote est associé à un identifiant technique de votre appareil, " +
                "non lié à votre identité.",
                color = OnSurfaceMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp,
            )

            Spacer(Modifier.height(20.dp))

            // ── Open source links ────────────────────────────────────────
            Text(
                "Code source librement vérifiable :",
                color = OnSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(8.dp))

            GitHubLink(label = "Backend Rust (serveur)", url = URL_BACKEND)
            Spacer(Modifier.height(4.dp))
            GitHubLink(label = "Application Android", url = URL_APP)

            Spacer(Modifier.height(28.dp))

            // ── Disclaimer ───────────────────────────────────────────────
            HorizontalDivider(color = SurfaceVariant)

            Spacer(Modifier.height(20.dp))

            Text(
                "À SAVOIR",
                color = OnSurfaceMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
            )

            Spacer(Modifier.height(12.dp))

            DisclaimerItem(
                "Les candidats affichés ont annoncé ou fortement manifesté leur intention " +
                "de se présenter. La liste évolue avec les officialisations de candidatures."
            )

            Spacer(Modifier.height(10.dp))

            DisclaimerItem(
                "Les participants ne représentent pas le corps électoral français. " +
                "Ces résultats reflètent l'opinion des utilisateurs de l'application, " +
                "pas une projection électorale."
            )

            Spacer(Modifier.height(10.dp))

            DisclaimerItem(
                "L'application ne peut garantir l'absence totale de manipulation. " +
                "Elle mise sur une participation massive pour en réduire l'impact à la marge."
            )

            Spacer(Modifier.height(10.dp))

            DisclaimerItem(
                "Les candidats à égalité de votes sont classés par ordre alphabétique."
            )

            Spacer(Modifier.height(10.dp))

            DisclaimerItem(
                "Élysée 2027 ne prétend annoncer aucun résultat électoral. " +
                "Il donne uniquement un aperçu de l'avis de ses participants."
            )

            Spacer(Modifier.height(32.dp))

            // ── CTA ──────────────────────────────────────────────────────
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B7BFF)),
            ) {
                Text(
                    "Commencer",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeatureChip(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = OnSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp,
        )
    }
}

@Composable
private fun GitHubLink(label: String, url: String) {
    val annotated = buildAnnotatedString {
        withLink(LinkAnnotation.Url(url)) {
            withStyle(
                SpanStyle(
                    color = Color(0xFF7B7BFF),
                    textDecoration = TextDecoration.Underline,
                    fontSize = 13.sp,
                )
            ) {
                append(label)
            }
        }
    }
    Text(text = annotated)
}

@Composable
private fun DisclaimerItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("·", color = OnSurfaceMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 1.dp))
        Text(
            text = text,
            color = OnSurfaceMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )
    }
}
