package com.example.ui.screens.stats.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.stats.DailyStatItem
import com.example.ui.screens.stats.StatsPeriod
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
fun DailyExpenseBarChart(
    dailyStats: List<DailyStatItem>,
    period: StatsPeriod,
    selectedBarItem: DailyStatItem?,
    onSelectBar: (DailyStatItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (period == StatsPeriod.DAY) {
        // Daily chart not needed for single day view
        return
    }

    val maxAmount = dailyStats.maxOfOrNull { it.amount } ?: 0L
    val hasData = maxAmount > 0L

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chart_daily_bars"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (period == StatsPeriod.WEEK) "Chi tiêu theo ngày trong tuần" else "Chi tiêu từng ngày trong tháng",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    if (hasData) {
                        Text(
                            text = "Cao nhất: ${CurrencyFormatter.formatVnd(maxAmount)}/ngày",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                if (selectedBarItem != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceHighlight)
                            .clickable { onSelectBar(null) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Bỏ chọn",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Bar Info Banner
            AnimatedVisibility(
                visible = selectedBarItem != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (selectedBarItem != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${selectedBarItem.dayOfWeekFull} - Ngày ${selectedBarItem.dateFormatted}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = CurrencyFormatter.formatVnd(selectedBarItem.amount),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = if (selectedBarItem.amount > 0L) CoralExpense else TextTertiary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    if (selectedBarItem.amount > 0L) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "(${selectedBarItem.transactionCount} giao dịch)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextSecondary
                                            )
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onSelectBar(null) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Đóng",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!hasData || dailyStats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Chưa có dữ liệu chi tiêu các ngày.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary
                            )
                        )
                    }
                }
            } else {
                val scrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(vertical = 8.dp),
                        horizontalArrangement = if (period == StatsPeriod.WEEK) Arrangement.SpaceEvenly else Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxBarHeight = 110.dp

                        dailyStats.forEach { item ->
                            val isSelected = selectedBarItem == item
                            val animatedHeightRatio by animateFloatAsState(
                                targetValue = if (hasData && item.amount > 0L) item.percentageOfMax else 0f,
                                animationSpec = tween(durationMillis = 400),
                                label = "bar_height"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .testTag("bar_item_${item.dayOfMonth}")
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectBar(item) }
                                    .padding(horizontal = if (period == StatsPeriod.WEEK) 8.dp else 3.dp)
                            ) {
                                // Amount text on top if selected
                                if (isSelected && item.amount > 0L) {
                                    Text(
                                        text = if (item.amount >= 1_000_000L) {
                                            String.format(Locale.US, "%.1fTr", item.amount / 1_000_000.0)
                                        } else if (item.amount >= 1_000L) {
                                            "${item.amount / 1000}k"
                                        } else {
                                            "${item.amount}"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            color = EmeraldLight,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }

                                // The Vertical Bar
                                Box(
                                    modifier = Modifier
                                        .width(if (period == StatsPeriod.WEEK) 28.dp else 16.dp)
                                        .height(maxBarHeight),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    // Background Track
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(maxBarHeight)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isSelected) DarkSurfaceHighlight else DarkSurface
                                            )
                                    )

                                    // Fill Bar
                                    if (item.amount > 0L) {
                                        val barActualHeight = (maxBarHeight * animatedHeightRatio).coerceAtLeast(6.dp)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(barActualHeight)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isSelected) {
                                                        Brush.verticalGradient(
                                                            listOf(EmeraldLight, EmeraldPrimary)
                                                        )
                                                    } else {
                                                        Brush.verticalGradient(
                                                            listOf(CoralExpense, CoralExpense.copy(alpha = 0.75f))
                                                        )
                                                    }
                                                )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Day Label
                                Text(
                                    text = if (period == StatsPeriod.WEEK) {
                                        item.dayOfWeekShort
                                    } else {
                                        String.format(Locale.US, "%02d", item.dayOfMonth)
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = if (period == StatsPeriod.WEEK) 11.sp else 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) EmeraldLight else if (item.amount > 0L) TextPrimary else TextTertiary
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Chạm vào một cột để xem số tiền chi tiêu của ngày đó",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
