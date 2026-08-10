package com.example.ui.screens.stats

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.stats.components.CategoryBreakdownSection
import com.example.ui.screens.stats.components.CategoryDonutChart
import com.example.ui.screens.stats.components.DailyExpenseBarChart
import com.example.ui.screens.stats.components.KeyMetricsHighlightsSection
import com.example.ui.screens.stats.components.BudgetStatsCard
import com.example.ui.screens.stats.components.TotalExpenseCard
import com.example.ui.theme.AppTheme
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val periodTitle by viewModel.periodTitle.collectAsStateWithLifecycle()
    val selectedDateTimestamp by viewModel.selectedDateTimestamp.collectAsStateWithLifecycle()
    val selectedDonutCategory by viewModel.selectedDonutCategory.collectAsStateWithLifecycle()
    val selectedBarItem by viewModel.selectedBarItem.collectAsStateWithLifecycle()
    val statsData by viewModel.statsData.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val calendar = remember(selectedDateTimestamp) {
        Calendar.getInstance().apply { timeInMillis = selectedDateTimestamp }
    }

    val datePickerDialog = remember(context, selectedDateTimestamp) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                viewModel.selectDate(cal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("stats_screen"),
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thống kê",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Period Selector Tabs (Ngày | Tuần | Tháng)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_period_selector"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatsPeriod.entries.forEach { period ->
                        val isSelected = selectedPeriod == period
                        val testTagKey = when (period) {
                            StatsPeriod.DAY -> "tab_period_day"
                            StatsPeriod.WEEK -> "tab_period_week"
                            StatsPeriod.MONTH -> "tab_period_month"
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) EmeraldPrimary else AppTheme.colors.surfaceElevated
                                )
                                .clickable { viewModel.setPeriod(period) }
                                .padding(vertical = 10.dp)
                                .testTag(testTagKey),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AppTheme.colors.background else AppTheme.colors.textSecondary,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time Navigator Header (← Trước | Period Title | Sau →)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_period_navigator"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
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
                        onClick = { viewModel.goToPreviousPeriod() },
                        modifier = Modifier.testTag("btn_prev_period")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Trước",
                            tint = EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = selectedPeriod == StatsPeriod.DAY) {
                                datePickerDialog.show()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        if (selectedPeriod == StatsPeriod.DAY) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Chọn ngày",
                                tint = EmeraldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = periodTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("txt_period_header")
                        )
                    }

                    IconButton(
                        onClick = { viewModel.goToNextPeriod() },
                        modifier = Modifier.testTag("btn_next_period")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Sau",
                            tint = EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. TỔNG CHI Card + Comparison with previous period
            TotalExpenseCard(
                totalExpense = statsData.totalExpense,
                transactionCount = statsData.transactionCount,
                period = selectedPeriod,
                comparison = statsData.comparison
            )

            // 2. NGÂN SÁCH Card (if available in month mode)
            if (statsData.budgetStat.hasBudget) {
                Spacer(modifier = Modifier.height(16.dp))
                BudgetStatsCard(budgetStat = statsData.budgetStat)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. BIỂU ĐỒ TRÒN / DONUT Phân bổ danh mục
            CategoryDonutChart(
                categoryStats = statsData.categoryStats,
                totalExpense = statsData.totalExpense,
                selectedCategory = selectedDonutCategory,
                onSelectCategory = { viewModel.selectDonutCategory(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. CHI THEO DANH MỤC (Sorted Chi nhiều -> chi ít)
            CategoryBreakdownSection(
                categoryStats = statsData.categoryStats,
                selectedCategory = selectedDonutCategory,
                onSelectCategory = { viewModel.selectDonutCategory(it) }
            )

            // 5. BIỂU ĐỒ CỘT THEO NGÀY (For Month and Week modes)
            if (selectedPeriod != StatsPeriod.DAY) {
                Spacer(modifier = Modifier.height(16.dp))
                DailyExpenseBarChart(
                    dailyStats = statsData.dailyStats,
                    period = selectedPeriod,
                    selectedBarItem = selectedBarItem,
                    onSelectBar = { viewModel.selectBarItem(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. CÁC CHỈ SỐ PHỤ & ĐIỂM NỔI BẬT
            KeyMetricsHighlightsSection(
                highestDay = statsData.highestDay,
                lowestDay = statsData.lowestDay,
                averageDailyExpense = statsData.averageExpense,
                activeSpendingDaysCount = statsData.activeSpendingDaysCount,
                highestCategory = statsData.highestCategory,
                lowestCategory = statsData.lowestCategory,
                transactionCount = statsData.transactionCount,
                period = selectedPeriod
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
