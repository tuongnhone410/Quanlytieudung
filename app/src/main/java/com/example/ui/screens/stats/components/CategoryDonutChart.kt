package com.example.ui.screens.stats.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.stats.CategoryStatItem
import com.example.ui.theme.AppTheme
import com.example.ui.theme.EmeraldLight
import com.example.util.CurrencyFormatter
import java.util.Locale
import kotlin.math.atan2

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
    categoryStats: List<CategoryStatItem>,
    totalExpense: Long,
    selectedCategory: CategoryStatItem?,
    onSelectCategory: (CategoryStatItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentColors = AppTheme.colors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chart_donut_categories"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = currentColors.surfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, currentColors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tỷ lệ theo danh mục",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = currentColors.textPrimary
                    )
                )

                if (selectedCategory != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(currentColors.surfaceHighlight)
                            .clickable { onSelectCategory(null) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Xem tất cả",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EmeraldLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (totalExpense == 0L || categoryStats.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(currentColors.surfaceHighlight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = currentColors.textTertiary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Chưa có dữ liệu chi tiêu.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = currentColors.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            } else {
                val animationProgress = remember { Animatable(0f) }
                LaunchedEffect(categoryStats, totalExpense) {
                    animationProgress.snapTo(0f)
                    animationProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                    )
                }

                val trackRingColor = currentColors.surfaceHighlight

                // Donut Chart Box with center text
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(categoryStats, totalExpense) {
                                detectTapGestures { tapOffset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = tapOffset.x - center.x
                                    val dy = tapOffset.y - center.y
                                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                                    val outerRadius = size.width / 2f
                                    val innerRadius = outerRadius - 32.dp.toPx()

                                    if (distance in innerRadius..outerRadius) {
                                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                        // Normalize angle so -90 deg (top) is 0 deg
                                        angle = (angle + 90f + 360f) % 360f

                                        var currentAngle = 0f
                                        var found: CategoryStatItem? = null
                                        for (item in categoryStats) {
                                            val sweep = (item.percentage / 100f) * 360f
                                            if (angle >= currentAngle && angle <= currentAngle + sweep) {
                                                found = item
                                                break
                                            }
                                            currentAngle += sweep
                                        }

                                        if (found != null) {
                                            onSelectCategory(if (selectedCategory == found) null else found)
                                        }
                                    } else if (distance < innerRadius) {
                                        // Tapped center: reset selection
                                        onSelectCategory(null)
                                    }
                                }
                            }
                    ) {
                        val strokeWidth = 28.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val arcSize = Size(diameter, diameter)

                        var startAngle = -90f

                        // Draw background track ring
                        drawArc(
                            color = trackRingColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )

                        categoryStats.forEach { item ->
                            val fullSweep = (item.percentage / 100f) * 360f
                            val animatedSweep = fullSweep * animationProgress.value

                            // Determine if this segment is highlighted or dimmed
                            val isSelected = selectedCategory == null || selectedCategory == item
                            val segmentColor = if (isSelected) item.color else item.color.copy(alpha = 0.35f)
                            val currentStrokeWidth = if (selectedCategory == item) strokeWidth + 4.dp.toPx() else strokeWidth

                            if (animatedSweep > 0.5f) {
                                // Draw arc with small spacing gap
                                val gap = if (categoryStats.size > 1) 2.5f else 0f
                                val actualSweep = (animatedSweep - gap).coerceAtLeast(0.5f)
                                val actualStart = startAngle + (gap / 2f)

                                drawArc(
                                    color = segmentColor,
                                    startAngle = actualStart,
                                    sweepAngle = actualSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = currentStrokeWidth, cap = StrokeCap.Butt)
                                )
                            }
                            startAngle += fullSweep
                        }
                    }

                    // Center Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(28.dp)
                    ) {
                        if (selectedCategory != null) {
                            Text(
                                text = selectedCategory.category.icon,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedCategory.category.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = currentColors.textSecondary,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = CurrencyFormatter.formatVnd(selectedCategory.amount),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = currentColors.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f%%", selectedCategory.percentage),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = selectedCategory.color,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        } else {
                            Text(
                                text = "Tổng chi",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = currentColors.textTertiary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatVnd(totalExpense),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = currentColors.textPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${categoryStats.size} danh mục",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = currentColors.textSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Legend Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    categoryStats.forEach { item ->
                        val isSelected = selectedCategory == item
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) currentColors.surfaceHighlight else currentColors.surface)
                                .clickable {
                                    onSelectCategory(if (isSelected) null else item)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(item.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${item.category.icon} ${item.category.name}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) currentColors.textPrimary else currentColors.textSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f%%", item.percentage),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = item.color,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
