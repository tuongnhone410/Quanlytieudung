package com.example.ui.screens.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EmptyExpenseState
import com.example.ui.components.ExpenseCardItem
import com.example.ui.components.ExpenseDetailBottomSheet
import com.example.ui.components.MonthlyBudgetCard
import com.example.ui.components.SetMonthlyBudgetDialog
import com.example.ui.theme.AppTheme
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToEditExpense: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMonthHeader by viewModel.selectedMonthHeader.collectAsStateWithLifecycle()
    val selectedMonthTotal by viewModel.selectedMonthTotal.collectAsStateWithLifecycle()
    val todayTotal by viewModel.todayTotal.collectAsStateWithLifecycle()
    val weekTotal by viewModel.weekTotal.collectAsStateWithLifecycle()
    val categorySpendings by viewModel.categorySpendings.collectAsStateWithLifecycle()
    val selectedMonthExpenses by viewModel.selectedMonthExpenses.collectAsStateWithLifecycle()
    val selectedExpenseForDetail by viewModel.selectedExpenseForDetail.collectAsStateWithLifecycle()
    val budgetUiState by viewModel.budgetUiState.collectAsStateWithLifecycle()
    val selectedMonthBudget by viewModel.selectedMonthBudget.collectAsStateWithLifecycle()
    val isBudgetDialogOpen by viewModel.isBudgetDialogOpen.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("overview_screen"),
        containerColor = AppTheme.colors.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddExpense,
                modifier = Modifier.testTag("fab_add_expense"),
                shape = CircleShape,
                containerColor = EmeraldPrimary,
                contentColor = Color(0xFF0F1015)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm chi tiêu",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Selector Bar: ← Tháng trước | Tháng 8, 2026 | Tháng sau →
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("month_navigation_bar"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.goToPreviousMonth() },
                            modifier = Modifier.testTag("btn_prev_month")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Tháng trước",
                                tint = AppTheme.colors.textSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.goToCurrentMonth() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedMonthHeader,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary
                                ),
                                modifier = Modifier.testTag("current_month_header")
                            )
                        }

                        IconButton(
                            onClick = { viewModel.goToNextMonth() },
                            modifier = Modifier.testTag("btn_next_month")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Tháng sau",
                                tint = AppTheme.colors.textSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Section: NGÂN SÁCH THÁNG (Optional Monthly Budget Card)
            item {
                MonthlyBudgetCard(
                    budgetState = budgetUiState,
                    monthHeader = selectedMonthHeader,
                    onSetBudgetClick = { viewModel.showBudgetDialog() },
                    onEditBudgetClick = { viewModel.showBudgetDialog() }
                )
            }

            // Hero Card: ĐÃ CHI THÁNG NÀY
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("total_spent_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        AppTheme.colors.surfaceHighlight.copy(alpha = 0.6f),
                                        AppTheme.colors.surfaceElevated
                                    )
                                )
                            )
                            .padding(22.dp)
                    ) {
                        Column {
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
                                            .background(AppTheme.colors.coralExpenseBadge),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                            contentDescription = null,
                                            tint = CoralExpense,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "ĐÃ CHI THÁNG NÀY",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = AppTheme.colors.textSecondary
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppTheme.colors.surfaceHighlight)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (selectedMonthExpenses.isEmpty()) "0 giao dịch" else "${selectedMonthExpenses.size} giao dịch",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = AppTheme.colors.textTertiary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = CurrencyFormatter.formatVnd(selectedMonthTotal),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppTheme.colors.textPrimary,
                                    fontSize = 36.sp
                                ),
                                modifier = Modifier.testTag("total_spent_amount")
                            )

                            if (selectedMonthExpenses.isEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Chưa có khoản chi trong tháng này",
                                    style = MaterialTheme.typography.bodySmall.copy(color = AppTheme.colors.textTertiary)
                                )
                            }
                        }
                    }
                }
            }

            // Secondary Row: HÔM NAY & TUẦN NÀY
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // HÔM NAY Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("today_spent_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "HÔM NAY",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        color = AppTheme.colors.textTertiary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = CurrencyFormatter.formatVnd(todayTotal),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                    fontSize = 20.sp
                                ),
                                modifier = Modifier.testTag("today_total_amount")
                            )
                        }
                    }

                    // TUẦN NÀY Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("week_spent_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TUẦN NÀY",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        color = AppTheme.colors.textTertiary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = CurrencyFormatter.formatVnd(weekTotal),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                    fontSize = 20.sp
                                ),
                                modifier = Modifier.testTag("week_total_amount")
                            )
                        }
                    }
                }
            }

            // Section: CHI THEO DANH MỤC (Only categories with > 0 spending in this month)
            if (categorySpendings.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CHI THEO DANH MỤC",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = AppTheme.colors.textPrimary
                                )
                            )
                            Text(
                                text = "${categorySpendings.size} danh mục",
                                style = MaterialTheme.typography.labelMedium.copy(color = AppTheme.colors.textTertiary)
                            )
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_breakdown_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                categorySpendings.forEachIndexed { index, item ->
                                    val formattedPercent = String.format(Locale.GERMAN, "%.1f%%", item.percentage)

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = item.category.icon, fontSize = 20.sp)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = item.category.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = AppTheme.colors.textPrimary
                                                    )
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = CurrencyFormatter.formatVnd(item.amount),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = AppTheme.colors.textPrimary
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = formattedPercent,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = EmeraldLight
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Progress bar representation
                                        LinearProgressIndicator(
                                            progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = EmeraldPrimary,
                                            trackColor = AppTheme.colors.surfaceHighlight
                                        )
                                    }

                                    if (index < categorySpendings.size - 1) {
                                        HorizontalDivider(
                                            color = AppTheme.colors.border.copy(alpha = 0.5f),
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section: Giao dịch trong tháng
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Giao dịch trong tháng",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                    )
                }
            }

            // List of Expenses or Empty State
            if (selectedMonthExpenses.isEmpty()) {
                item {
                    EmptyExpenseState(
                        onAddExpenseClick = onNavigateToAddExpense,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                items(
                    items = selectedMonthExpenses,
                    key = { it.expense.id }
                ) { expenseItem ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        ExpenseCardItem(
                            item = expenseItem,
                            onClick = { viewModel.onExpenseClicked(expenseItem) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Budget Setting Dialog
    if (isBudgetDialogOpen) {
        SetMonthlyBudgetDialog(
            initialAmount = selectedMonthBudget?.amount ?: 0L,
            monthHeader = selectedMonthHeader,
            onDismiss = { viewModel.hideBudgetDialog() },
            onSave = { amount ->
                viewModel.saveBudget(amount)
            },
            onDelete = {
                viewModel.deleteCurrentMonthBudget()
            }
        )
    }

    // Detail Bottom Sheet
    if (selectedExpenseForDetail != null) {
        ExpenseDetailBottomSheet(
            item = selectedExpenseForDetail!!,
            onDismiss = { viewModel.dismissExpenseDetail() },
            onEdit = { expenseId ->
                onNavigateToEditExpense(expenseId)
            },
            onDelete = { expense ->
                viewModel.deleteExpense(expense)
            }
        )
    }
}
