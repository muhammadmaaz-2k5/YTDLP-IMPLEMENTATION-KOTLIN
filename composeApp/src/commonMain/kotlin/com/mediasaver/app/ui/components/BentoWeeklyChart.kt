package com.mediasaver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.ui.theme.BentoPurplePrimary
import com.mediasaver.app.ui.theme.BentoSkyContainer
import com.mediasaver.app.ui.theme.BentoTextPrimaryLight
import com.mediasaver.app.ui.theme.BentoTextSecondaryLight

data class ChartDayData(
    val dayLabel: String,
    val valuePercent: Float, // 0.1f to 1.0f
    val isHighlighted: Boolean = false
)

/**
 * Metric & Activity card matching the "Energy Consumption today" card in the reference UI mockup.
 */
@Composable
fun BentoWeeklyChart(
    title: String,
    subtitle: String,
    metricValue: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Bolt,
    iconBgColor: Color = BentoPurplePrimary,
    days: List<ChartDayData> = defaultWeeklyData()
) {
    BentoSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Icon + Title + Metric Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular icon badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimaryLight
                    )
                    Text(
                        text = subtitle,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondaryLight
                    )
                }

                Text(
                    text = metricValue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BentoTextPrimaryLight
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Bar Chart Row ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        // Vertical bar with fully rounded ends
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .fillMaxHeight(item.valuePercent.coerceIn(0.18f, 1f))
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (item.isHighlighted) BentoPurplePrimary
                                    else BentoSkyContainer
                                )
                        )

                        Spacer(Modifier.height(8.dp))

                        // Day label below
                        Text(
                            text = item.dayLabel,
                            fontSize = 11.sp,
                            fontWeight = if (item.isHighlighted) FontWeight.Bold else FontWeight.Medium,
                            color = if (item.isHighlighted) BentoPurplePrimary else BentoTextSecondaryLight
                        )
                    }
                }
            }
        }
    }
}

private fun defaultWeeklyData(): List<ChartDayData> = listOf(
    ChartDayData("Sun", 0.35f),
    ChartDayData("Mon", 0.50f),
    ChartDayData("Tue", 0.40f),
    ChartDayData("Wed", 0.70f),
    ChartDayData("Thu", 0.95f, isHighlighted = true),
    ChartDayData("Fri", 0.45f)
)
