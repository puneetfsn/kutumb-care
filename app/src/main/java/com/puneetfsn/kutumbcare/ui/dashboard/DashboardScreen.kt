package com.puneetfsn.kutumbcare.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puneetfsn.kutumbcare.data.AssetCategory
import com.puneetfsn.kutumbcare.data.CategoryProgress
import com.puneetfsn.kutumbcare.ui.LocalAppLanguage
import com.puneetfsn.kutumbcare.ui.LocalStrings
import com.puneetfsn.kutumbcare.ui.components.NomineeAlertCard
import com.puneetfsn.kutumbcare.ui.components.PrivacyBanner
import com.puneetfsn.kutumbcare.ui.components.ReadinessRing
import com.puneetfsn.kutumbcare.ui.components.StatChip
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenCategory: (AssetCategory) -> Unit,
    onOpenSettings: () -> Unit
) {
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    val hindi = LocalAppLanguage.current.isHindi
    val extras by viewModel.extras.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(extras.message) {
        if (extras.message != null) {
            delay(3500)
            viewModel.consumeMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text("Kutumb Care", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        Text(snapshot.preferences.familyName(hindi), style = MaterialTheme.typography.headlineMedium)
        Text(
            strings.tagline,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            ReadinessRing(snapshot.readinessPercent)
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatChip(strings.recorded, "${snapshot.completedCount}/${snapshot.totalCount}", Modifier.weight(1f))
            StatChip(strings.nomineeAlerts, snapshot.missingNomineeItems.size.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        NomineeAlertCard(snapshot.missingNomineeItems.size)
        Spacer(Modifier.height(14.dp))
        PrivacyBanner()
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.generateAndShare(context) },
            enabled = !extras.generating,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
            Spacer(Modifier.padding(6.dp))
            Text(if (extras.generating) strings.preparing else strings.generateDossier)
        }
        extras.message?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(strings.categories, style = MaterialTheme.typography.titleLarge)
            Text(
                strings.familyProfileLink,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onOpenSettings)
            )
        }
        Spacer(Modifier.height(10.dp))
        snapshot.categoryProgress.forEach { progress ->
            CategoryRow(progress, onClick = { onOpenCategory(progress.category) })
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CategoryRow(progress: CategoryProgress, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(progress.category.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.padding(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(LocalStrings.current.category(progress.category), style = MaterialTheme.typography.titleMedium)
                    Text(
                        LocalStrings.current.categorySubtitle(progress.category),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("${progress.completed}/${progress.total}", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress.percent / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
