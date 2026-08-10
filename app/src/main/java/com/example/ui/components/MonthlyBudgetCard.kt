package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.overview.BudgetStatus
import com.example.ui.screens.overview.MonthlyBudgetUiState
import com.example.ui.theme.AppTheme
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
fun MonthlyBudgetCard(
    budgetState: MonthlyBudgetUiState,
    monthHeader: String,
    onSetBudgetClick: () -> Unit,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_budget_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        if (!budgetState.hasBudget) {
            // Case 1: NO Budget set ("Chưa đặt ngân sách")
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
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.surfaceHighlight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = AppTheme.colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "NGÂN SÁCH THÁNG",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = AppTheme.colors.textSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Chưa đặt ngân sách",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textTertiary
                            ),
                            modifier = Modifier.testTag("budget_not_set_text")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Đặt hạn mức để kiểm soát chi tiêu tốt hơn",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AppTheme.colors.textTertiary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onSetBudgetClick,
                        modifier = Modifier.testTag("btn_set_budget"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color(0xFF0F1015)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Đặt ngân sách",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        } else {
            // Case 2: HAS Budget set
            val progressColor = when (budgetState.status) {
                BudgetStatus.OVER -> CoralExpense
                BudgetStatus.WARNING -> AppTheme.colors.amberAccent
                else -> EmeraldPrimary
            }

            val statusBadgeBg = when (budgetState.status) {
                BudgetStatus.OVER -> AppTheme.colors.coralExpenseBadge
                BudgetStatus.WARNING -> AppTheme.colors.amberAccent.copy(alpha = 0.15f)
                else -> EmeraldPrimary.copy(alpha = 0.12f)
            }

            val statusBadgeText = when (budgetState.status) {
                BudgetStatus.OVER -> "Đã vượt ngân sách"
                BudgetStatus.WARNING -> "Gần đạt ngân sách"
                else -> "Đang kiểm soát tốt"
            }

            val statusTextColor = when (budgetState.status) {
                BudgetStatus.OVER -> CoralExpense
                BudgetStatus.WARNING -> AppTheme.colors.amberAccent
                else -> EmeraldLight
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                AppTheme.colors.surfaceHighlight.copy(alpha = 0.4f),
                                AppTheme.colors.surfaceElevated
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "NGÂN SÁCH THÁNG",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = AppTheme.colors.textSecondary
                                )
                            )
                        }

                        // Edit Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.surfaceHighlight)
                                .clickable(onClick = onEditBudgetClick)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("btn_edit_budget")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Chỉnh sửa",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EmeraldLight,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Budget Amount & Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = CurrencyFormatter.formatVnd(budgetState.budgetAmount),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = AppTheme.colors.textPrimary,
                                fontSize = 28.sp
                            ),
                            modifier = Modifier.testTag("budget_total_amount")
                        )

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(statusBadgeBg)
                                .border(1.dp, statusTextColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("budget_status_badge")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (budgetState.status == BudgetStatus.WARNING || budgetState.status == BudgetStatus.OVER) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = statusTextColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = statusBadgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = statusTextColor
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar (Clamped to 100%)
                    LinearProgressIndicator(
                        progress = { (budgetState.usagePercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .testTag("budget_progress_bar"),
                        color = progressColor,
                        trackColor = AppTheme.colors.surfaceHighlight
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats Grid: Đã chi | Còn lại / Đã vượt | Đã sử dụng %
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppTheme.colors.surface.copy(alpha = 0.7f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Đã chi
                        Column {
                            Text(
                                text = "Đã chi",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = AppTheme.colors.textTertiary,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatVnd(budgetState.spentAmount),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary
                                ),
                                modifier = Modifier.testTag("budget_spent_amount")
                            )
                        }

                        // Còn lại OR Đã vượt
                        Column {
                            if (budgetState.overAmount > 0L) {
                                Text(
                                    text = "Đã vượt",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CoralExpense,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = CurrencyFormatter.formatVnd(budgetState.overAmount),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CoralExpense
                                    ),
                                    modifier = Modifier.testTag("budget_over_amount")
                                )
                            } else {
                                Text(
                                    text = "Còn lại",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AppTheme.colors.textTertiary,
                                        fontSize = 11.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = CurrencyFormatter.formatVnd(budgetState.remainingAmount),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldLight
                                    ),
                                    modifier = Modifier.testTag("budget_remaining_amount")
                                )
                            }
                        }

                        // Đã sử dụng %
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Đã sử dụng",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = AppTheme.colors.textTertiary,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val formattedPercent = String.format(Locale.GERMAN, "%.1f%%", budgetState.usagePercentage)
                            Text(
                                text = formattedPercent,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = progressColor
                                ),
                                modifier = Modifier.testTag("budget_usage_percent")
                            )
                        }
                    }
                }
            }
        }
    }
}

