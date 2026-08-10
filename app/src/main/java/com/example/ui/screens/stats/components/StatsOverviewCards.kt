package com.example.ui.screens.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.stats.CategoryStatHighlight
import com.example.ui.screens.stats.CategoryStatItem
import com.example.ui.screens.stats.DayStatHighlight
import com.example.ui.screens.stats.MonthlyBudgetStat
import com.example.ui.screens.stats.PeriodComparison
import com.example.ui.screens.stats.StatsPeriod
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.CoralExpenseBadge
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
fun TotalExpenseCard(
    totalExpense: Long,
    transactionCount: Int,
    period: StatsPeriod,
    comparison: PeriodComparison,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_total_expense"),
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
                Text(
                    text = "TỔNG CHI",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceHighlight)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$transactionCount giao dịch",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = CurrencyFormatter.formatVnd(totalExpense),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (totalExpense > 0L) CoralExpense else TextPrimary,
                    fontSize = 32.sp
                ),
                modifier = Modifier.testTag("txt_total_expense")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Period Comparison Sub-section
            val periodName = when (period) {
                StatsPeriod.MONTH -> "tháng"
                StatsPeriod.WEEK -> "tuần"
                StatsPeriod.DAY -> "ngày"
            }

            if (comparison.hasPreviousData) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
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
                                text = "So với $periodName trước (${CurrencyFormatter.formatVnd(comparison.previousAmount)})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val diffText = if (comparison.difference > 0) {
                                "+${CurrencyFormatter.formatVnd(comparison.difference)}"
                            } else if (comparison.difference < 0) {
                                "-${CurrencyFormatter.formatVnd(kotlin.math.abs(comparison.difference))}"
                            } else {
                                "0đ"
                            }

                            val diffDesc = when {
                                comparison.difference > 0 -> "Chi nhiều hơn $periodName trước"
                                comparison.difference < 0 -> "Chi ít hơn $periodName trước"
                                else -> "Bằng $periodName trước"
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = diffText,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (comparison.difference > 0) CoralExpense else if (comparison.difference < 0) EmeraldLight else TextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• $diffDesc",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        if (comparison.percentageChange != null) {
                            val isIncrease = comparison.difference > 0
                            val pct = comparison.percentageChange
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isIncrease) CoralExpenseBadge else EmeraldGlow)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = if (isIncrease) CoralExpense else EmeraldLight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = String.format(Locale.US, "%+.1f%%", pct),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isIncrease) CoralExpense else EmeraldLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Không có dữ liệu $periodName trước để so sánh.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun BudgetStatsCard(
    budgetStat: MonthlyBudgetStat,
    modifier: Modifier = Modifier
) {
    if (!budgetStat.hasBudget) {
        // Do not display if no budget configured
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_budget_stats"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceHighlight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "NGÂN SÁCH THÁNG",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextSecondary
                        )
                    )
                }

                val pctColor = when {
                    budgetStat.usagePercentage >= 100f -> CoralExpense
                    budgetStat.usagePercentage >= 80f -> AmberAccent
                    else -> EmeraldLight
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                budgetStat.usagePercentage >= 100f -> CoralExpenseBadge
                                budgetStat.usagePercentage >= 80f -> AmberGlow
                                else -> EmeraldGlow
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "Đã dùng %.1f%%", budgetStat.usagePercentage),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = pctColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            val progress = (budgetStat.usagePercentage / 100f).coerceIn(0f, 1f)
            val progressColor = when {
                budgetStat.usagePercentage >= 100f -> CoralExpense
                budgetStat.usagePercentage >= 80f -> AmberAccent
                else -> EmeraldPrimary
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = DarkSurfaceHighlight
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Ngân sách",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                    )
                    Text(
                        text = CurrencyFormatter.formatVnd(budgetStat.budgetAmount),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Đã chi",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                    )
                    Text(
                        text = CurrencyFormatter.formatVnd(budgetStat.spentAmount),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = CoralExpense
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    val isOver = budgetStat.spentAmount > budgetStat.budgetAmount
                    Text(
                        text = if (isOver) "Vượt ngân sách" else "Còn lại",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isOver) CoralExpense else TextTertiary
                        )
                    )
                    Text(
                        text = if (isOver) {
                            "+${CurrencyFormatter.formatVnd(budgetStat.overAmount)}"
                        } else {
                            CurrencyFormatter.formatVnd(budgetStat.remainingAmount)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isOver) CoralExpense else EmeraldLight
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownSection(
    categoryStats: List<CategoryStatItem>,
    selectedCategory: CategoryStatItem?,
    onSelectCategory: (CategoryStatItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_category_breakdown"),
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
                Text(
                    text = "Chi theo danh mục",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Text(
                    text = "Sắp xếp: Chi nhiều nhất",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (categoryStats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có chi tiêu theo danh mục.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categoryStats.forEachIndexed { index, item ->
                        val isSelected = selectedCategory == item

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("item_category_stat_${item.category.id}")
                                .clickable { onSelectCategory(if (isSelected) null else item) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) DarkSurfaceHighlight else DarkSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) item.color.copy(alpha = 0.6f) else DarkBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Rank badge
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(DarkSurfaceHighlight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (index == 0) AmberAccent else TextTertiary
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Icon Box
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(item.color.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = item.category.icon,
                                                fontSize = 18.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = item.category.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TextPrimary
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${item.transactionCount} giao dịch",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = TextTertiary,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyFormatter.formatVnd(item.amount),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = String.format(Locale.US, "%.1f%%", item.percentage),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = item.color
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Progress Indicator for Category
                                LinearProgressIndicator(
                                    progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = item.color,
                                    trackColor = DarkSurfaceHighlight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyMetricsHighlightsSection(
    highestDay: DayStatHighlight?,
    lowestDay: DayStatHighlight?,
    averageDailyExpense: Long,
    activeSpendingDaysCount: Int,
    highestCategory: CategoryStatHighlight?,
    lowestCategory: CategoryStatHighlight?,
    transactionCount: Int,
    period: StatsPeriod,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_highlights_stats"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Chỉ số & Điểm nổi bật",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Row 1: Highest Day & Lowest Day
            if (period != StatsPeriod.DAY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricHighlightBox(
                        title = "Ngày chi nhiều nhất",
                        value = if (highestDay != null) CurrencyFormatter.formatVnd(highestDay.amount) else "Chưa có dữ liệu",
                        subtitle = if (highestDay != null) highestDay.dateFormatted else "",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = CoralExpense,
                        modifier = Modifier.weight(1f)
                    )

                    MetricHighlightBox(
                        title = "Ngày chi ít nhất",
                        value = if (lowestDay != null) CurrencyFormatter.formatVnd(lowestDay.amount) else "Chưa có dữ liệu",
                        subtitle = if (lowestDay != null) "${lowestDay.dateFormatted} (>0đ)" else "",
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        iconTint = EmeraldLight,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Row 2: Top Category & Lowest Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricHighlightBox(
                    title = "Danh mục chi nhiều nhất",
                    value = if (highestCategory != null) {
                        "${highestCategory.category.icon} ${highestCategory.category.name}"
                    } else {
                        "Chưa có dữ liệu"
                    },
                    subtitle = if (highestCategory != null) {
                        "${CurrencyFormatter.formatVnd(highestCategory.amount)} (${String.format(Locale.US, "%.1f%%", highestCategory.percentage)})"
                    } else "",
                    icon = Icons.Default.Category,
                    iconTint = highestCategory?.color ?: CoralExpense,
                    modifier = Modifier.weight(1f)
                )

                MetricHighlightBox(
                    title = "Danh mục chi ít nhất",
                    value = if (lowestCategory != null) {
                        "${lowestCategory.category.icon} ${lowestCategory.category.name}"
                    } else {
                        "Chưa có dữ liệu"
                    },
                    subtitle = if (lowestCategory != null) {
                        "${CurrencyFormatter.formatVnd(lowestCategory.amount)} (${String.format(Locale.US, "%.1f%%", lowestCategory.percentage)})"
                    } else "",
                    icon = Icons.Default.Category,
                    iconTint = lowestCategory?.color ?: EmeraldLight,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Average Daily Expense & Total Transactions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricHighlightBox(
                    title = "Trung bình chi tiêu",
                    value = if (averageDailyExpense > 0L) "${CurrencyFormatter.formatVnd(averageDailyExpense)}/ngày" else "0đ/ngày",
                    subtitle = if (activeSpendingDaysCount > 0) "Tính trên $activeSpendingDaysCount ngày có chi" else "Chưa có ngày chi tiêu",
                    icon = Icons.Default.DateRange,
                    iconTint = AmberAccent,
                    modifier = Modifier.weight(1f)
                )

                MetricHighlightBox(
                    title = "Tổng giao dịch",
                    value = "$transactionCount giao dịch",
                    subtitle = if (transactionCount > 0) "Đã ghi nhận" else "Chưa có giao dịch",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    iconTint = EmeraldLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetricHighlightBox(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextTertiary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
