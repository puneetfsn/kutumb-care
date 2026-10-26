package com.puneetfsn.kutumbcare.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.puneetfsn.kutumbcare.MainActivity
import com.puneetfsn.kutumbcare.data.AppDatabase
import com.puneetfsn.kutumbcare.data.ChecklistRepository
import com.puneetfsn.kutumbcare.data.SettingsRepository

class ReadinessWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ReadinessWidget()
}

class ReadinessWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val items = AppDatabase.getInstance(context).checklistDao().getAll()
        val snapshot = ChecklistRepository.snapshotOf(items, SettingsRepository(context).snapshot())
        provideContent {
            GlanceTheme {
                WidgetContent(snapshot.readinessPercent, snapshot.completedCount, snapshot.totalCount, snapshot.missingNomineeItems.size)
            }
        }
    }
}

@Composable
private fun WidgetContent(
    percent: Int,
    completed: Int,
    total: Int,
    missingNominees: Int
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF134E4A)))
            .clickable(onClick = actionStartActivity<MainActivity>())
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "KUTUMB CARE",
            style = TextStyle(color = ColorProvider(Color(0xFFE8D5B5)), fontSize = 11.sp)
        )
        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = "$percent% ready",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = "$completed of $total records  ·  $missingNominees nominee alerts",
            style = TextStyle(color = ColorProvider(Color(0xFFD4C4A8)), fontSize = 12.sp)
        )
    }
}
