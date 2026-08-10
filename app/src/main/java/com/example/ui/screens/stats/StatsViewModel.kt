package com.example.ui.screens.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.model.ExpenseWithCategory
import com.example.data.model.MonthlyBudget
import com.example.data.repository.ExpenseRepository
import com.example.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository = ExpenseRepository.getInstance(
        AppDatabase.getInstance(application)
    )

    private val initialYearAndMonth = DateUtils.getYearAndMonth()
    private val _selectedPeriod = MutableStateFlow(StatsPeriod.MONTH)
    val selectedPeriod: StateFlow<StatsPeriod> = _selectedPeriod.asStateFlow()

    private val _selectedYear = MutableStateFlow(initialYearAndMonth.first)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(initialYearAndMonth.second)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedDateTimestamp = MutableStateFlow(System.currentTimeMillis())
    val selectedDateTimestamp: StateFlow<Long> = _selectedDateTimestamp.asStateFlow()

    private val _selectedDonutCategory = MutableStateFlow<CategoryStatItem?>(null)
    val selectedDonutCategory: StateFlow<CategoryStatItem?> = _selectedDonutCategory.asStateFlow()

    private val _selectedBarItem = MutableStateFlow<DailyStatItem?>(null)
    val selectedBarItem: StateFlow<DailyStatItem?> = _selectedBarItem.asStateFlow()

    // Date range for current selected period
    data class DateRange(
        val startDate: Long,
        val endDate: Long,
        val prevStartDate: Long,
        val prevEndDate: Long,
        val title: String
    )

    private val currentRangeState: StateFlow<DateRange> = combine(
        _selectedPeriod,
        _selectedYear,
        _selectedMonth,
        _selectedDateTimestamp
    ) { period, year, month, timestamp ->
        when (period) {
            StatsPeriod.MONTH -> {
                val start = DateUtils.getStartOfMonth(year, month)
                val end = DateUtils.getEndOfMonth(year, month)
                val (prevYear, prevMonth) = DateUtils.getPreviousMonth(year, month)
                val prevStart = DateUtils.getStartOfMonth(prevYear, prevMonth)
                val prevEnd = DateUtils.getEndOfMonth(prevYear, prevMonth)
                DateRange(
                    startDate = start,
                    endDate = end,
                    prevStartDate = prevStart,
                    prevEndDate = prevEnd,
                    title = "Tháng $month, $year"
                )
            }
            StatsPeriod.WEEK -> {
                val start = DateUtils.getStartOfWeek(timestamp)
                val end = DateUtils.getEndOfWeek(timestamp)
                val prevStart = start - 7 * 24 * 60 * 60 * 1000L
                val prevEnd = start - 1L
                val isCurrentWeek = DateUtils.getStartOfWeek(System.currentTimeMillis()) == start
                val prefix = if (isCurrentWeek) "Tuần này" else "Tuần"
                DateRange(
                    startDate = start,
                    endDate = end,
                    prevStartDate = prevStart,
                    prevEndDate = prevEnd,
                    title = "$prefix (${DateUtils.formatWeekHeader(start, end)})"
                )
            }
            StatsPeriod.DAY -> {
                val start = DateUtils.getStartOfDay(timestamp)
                val end = DateUtils.getEndOfDay(timestamp)
                val prevStart = start - 24 * 60 * 60 * 1000L
                val prevEnd = start - 1L
                val isToday = DateUtils.isSameDay(timestamp, System.currentTimeMillis())
                val prefix = if (isToday) "Hôm nay, " else ""
                DateRange(
                    startDate = start,
                    endDate = end,
                    prevStartDate = prevStart,
                    prevEndDate = prevEnd,
                    title = "$prefix${DateUtils.formatDate(timestamp)}"
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DateRange(
            startDate = DateUtils.getStartOfMonth(initialYearAndMonth.first, initialYearAndMonth.second),
            endDate = DateUtils.getEndOfMonth(initialYearAndMonth.first, initialYearAndMonth.second),
            prevStartDate = 0L,
            prevEndDate = 0L,
            title = DateUtils.getCurrentMonthHeader()
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val periodTitle: StateFlow<String> = currentRangeState.flatMapLatest { range ->
        MutableStateFlow(range.title)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DateUtils.getCurrentMonthHeader()
    )

    // Current period expenses
    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentExpensesFlow: Flow<List<ExpenseWithCategory>> = currentRangeState.flatMapLatest { range ->
        repository.getExpensesByDateRange(range.startDate, range.endDate)
    }

    // Previous period total
    @OptIn(ExperimentalCoroutinesApi::class)
    private val previousTotalFlow: Flow<Long?> = currentRangeState.flatMapLatest { range ->
        repository.getTotalExpenseAmountBetween(range.prevStartDate, range.prevEndDate)
    }

    // Monthly Budget Flow
    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentBudgetFlow: Flow<MonthlyBudget?> = combine(_selectedPeriod, _selectedYear, _selectedMonth) { period, year, month ->
        if (period == StatsPeriod.MONTH) {
            Pair(year, month)
        } else {
            null
        }
    }.flatMapLatest { pair ->
        if (pair != null) {
            repository.getBudgetForMonth(pair.first, pair.second)
        } else {
            MutableStateFlow<MonthlyBudget?>(null)
        }
    }

    // Comprehensive Stats Calculations
    data class CalculatedStats(
        val totalExpense: Long = 0L,
        val transactionCount: Int = 0,
        val averageExpense: Long = 0L,
        val activeSpendingDaysCount: Int = 0,
        val categoryStats: List<CategoryStatItem> = emptyList(),
        val dailyStats: List<DailyStatItem> = emptyList(),
        val highestDay: DayStatHighlight? = null,
        val lowestDay: DayStatHighlight? = null,
        val highestCategory: CategoryStatHighlight? = null,
        val lowestCategory: CategoryStatHighlight? = null,
        val comparison: PeriodComparison = PeriodComparison(),
        val budgetStat: MonthlyBudgetStat = MonthlyBudgetStat()
    )

    private data class IntermediateExpenseData(
        val expenses: List<ExpenseWithCategory>,
        val prevTotal: Long,
        val budget: MonthlyBudget?
    )

    private val intermediateFlow: Flow<IntermediateExpenseData> = combine(
        currentExpensesFlow,
        previousTotalFlow,
        currentBudgetFlow
    ) { expenses, prevTotalNullable, budgetEntity ->
        IntermediateExpenseData(
            expenses = expenses,
            prevTotal = prevTotalNullable ?: 0L,
            budget = budgetEntity
        )
    }

    val statsData: StateFlow<CalculatedStats> = combine(
        intermediateFlow,
        currentRangeState,
        _selectedPeriod,
        repository.allCategories
    ) { intermediate, range, period, categories ->
        val expenses = intermediate.expenses
        val prevTotal = intermediate.prevTotal
        val budgetEntity = intermediate.budget

        val total = expenses.sumOf { it.expense.amount }
        val count = expenses.size

        // Comparison calculation
        val difference = total - prevTotal
        val percentageChange = if (prevTotal > 0L) {
            ((difference.toDouble() / prevTotal.toDouble()) * 100.0).toFloat()
        } else {
            null
        }
        val comparison = PeriodComparison(
            currentAmount = total,
            previousAmount = prevTotal,
            difference = difference,
            percentageChange = percentageChange,
            hasPreviousData = prevTotal > 0L,
            isIncrease = difference > 0L,
            isDecrease = difference < 0L
        )

        // Budget calculation (active for MONTH mode)
        val budgetStat = if (period == StatsPeriod.MONTH && budgetEntity != null && budgetEntity.amount > 0L) {
            val budgetAmount = budgetEntity.amount
            val remaining = (budgetAmount - total).coerceAtLeast(0L)
            val over = if (total > budgetAmount) total - budgetAmount else 0L
            val usagePct = (total.toDouble() / budgetAmount.toDouble() * 100.0).toFloat()
            MonthlyBudgetStat(
                hasBudget = true,
                budgetAmount = budgetAmount,
                spentAmount = total,
                remainingAmount = remaining,
                overAmount = over,
                usagePercentage = usagePct
            )
        } else {
            MonthlyBudgetStat(
                hasBudget = false,
                budgetAmount = 0L,
                spentAmount = total,
                remainingAmount = 0L,
                overAmount = 0L,
                usagePercentage = 0f
            )
        }

        // Category breakdown
        val categoryMap = categories.associateBy { it.id }
        val expensesByCategory = expenses.groupBy { it.expense.categoryId }
        val categoryStats = if (total > 0L) {
            expensesByCategory.mapNotNull { (catId, items) ->
                val catSum = items.sumOf { it.expense.amount }
                if (catSum > 0L) {
                    val category = categoryMap[catId]
                        ?: items.firstOrNull()?.category
                        ?: Category(id = catId, name = "Khác", icon = "📦")
                    val pct = (catSum.toDouble() / total.toDouble() * 100.0).toFloat()
                    val color = CategoryChartColors.getColorForCategory(catId)
                    CategoryStatItem(
                        category = category,
                        amount = catSum,
                        percentage = pct,
                        color = color,
                        transactionCount = items.size
                    )
                } else null
            }.sortedByDescending { it.amount }
        } else {
            emptyList()
        }

        val highestCategory = categoryStats.firstOrNull()?.let {
            CategoryStatHighlight(
                category = it.category,
                amount = it.amount,
                percentage = it.percentage,
                color = it.color
            )
        }

        val lowestCategory = categoryStats.lastOrNull()?.let {
            CategoryStatHighlight(
                category = it.category,
                amount = it.amount,
                percentage = it.percentage,
                color = it.color
            )
        }

        // Daily breakdown calculation
        val dailyItems = mutableListOf<DailyStatItem>()
        when (period) {
            StatsPeriod.MONTH -> {
                val cal = Calendar.getInstance().apply { timeInMillis = range.startDate }
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH) + 1
                val daysInMonth = DateUtils.getDaysInMonth(year, month)

                // Group expenses by day of month
                val dayMap = expenses.groupBy {
                    val c = Calendar.getInstance().apply { timeInMillis = it.expense.date }
                    c.get(Calendar.DAY_OF_MONTH)
                }

                var maxDailyAmount = 0L
                val rawDailyList = (1..daysInMonth).map { day ->
                    val dayCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month - 1)
                        set(Calendar.DAY_OF_MONTH, day)
                        set(Calendar.HOUR_OF_DAY, 12)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val timestamp = dayCal.timeInMillis
                    val dayExpenses = dayMap[day] ?: emptyList()
                    val dayAmount = dayExpenses.sumOf { it.expense.amount }
                    if (dayAmount > maxDailyAmount) {
                        maxDailyAmount = dayAmount
                    }
                    Triple(day, timestamp, dayExpenses)
                }

                rawDailyList.forEach { (day, timestamp, dayExpenses) ->
                    val dayAmount = dayExpenses.sumOf { it.expense.amount }
                    val pct = if (maxDailyAmount > 0L) (dayAmount.toFloat() / maxDailyAmount.toFloat()) else 0f
                    dailyItems.add(
                        DailyStatItem(
                            dateTimestamp = timestamp,
                            dayOfMonth = day,
                            dayOfWeekShort = DateUtils.getDayOfWeekShort(timestamp),
                            dayOfWeekFull = DateUtils.getDayOfWeekFull(timestamp),
                            dateFormatted = DateUtils.formatDate(timestamp),
                            amount = dayAmount,
                            percentageOfMax = pct,
                            transactionCount = dayExpenses.size
                        )
                    )
                }
            }
            StatsPeriod.WEEK -> {
                var maxDailyAmount = 0L
                val rawWeekDays = (0..6).map { offset ->
                    val dayTimestamp = range.startDate + offset * 24 * 60 * 60 * 1000L
                    val dayStart = DateUtils.getStartOfDay(dayTimestamp)
                    val dayEnd = DateUtils.getEndOfDay(dayTimestamp)
                    val dayExpenses = expenses.filter { it.expense.date in dayStart..dayEnd }
                    val dayAmount = dayExpenses.sumOf { it.expense.amount }
                    if (dayAmount > maxDailyAmount) {
                        maxDailyAmount = dayAmount
                    }
                    Pair(dayTimestamp, dayExpenses)
                }

                rawWeekDays.forEach { (timestamp, dayExpenses) ->
                    val dayCal = Calendar.getInstance().apply { timeInMillis = timestamp }
                    val dayAmount = dayExpenses.sumOf { it.expense.amount }
                    val pct = if (maxDailyAmount > 0L) (dayAmount.toFloat() / maxDailyAmount.toFloat()) else 0f
                    dailyItems.add(
                        DailyStatItem(
                            dateTimestamp = timestamp,
                            dayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH),
                            dayOfWeekShort = DateUtils.getDayOfWeekShort(timestamp),
                            dayOfWeekFull = DateUtils.getDayOfWeekFull(timestamp),
                            dateFormatted = DateUtils.formatDate(timestamp),
                            amount = dayAmount,
                            percentageOfMax = pct,
                            transactionCount = dayExpenses.size
                        )
                    )
                }
            }
            StatsPeriod.DAY -> {
                val cal = Calendar.getInstance().apply { timeInMillis = range.startDate }
                dailyItems.add(
                    DailyStatItem(
                        dateTimestamp = range.startDate,
                        dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                        dayOfWeekShort = DateUtils.getDayOfWeekShort(range.startDate),
                        dayOfWeekFull = DateUtils.getDayOfWeekFull(range.startDate),
                        dateFormatted = DateUtils.formatDate(range.startDate),
                        amount = total,
                        percentageOfMax = 1f,
                        transactionCount = count
                    )
                )
            }
        }

        // Active spending days calculation (days with amount > 0)
        val activeDays = dailyItems.filter { it.amount > 0L }
        val activeDaysCount = activeDays.size
        val avgExpense = if (activeDaysCount > 0) {
            total / activeDaysCount
        } else {
            0L
        }

        // Highest and lowest expense day
        val highestDay = if (activeDays.isNotEmpty()) {
            val maxDay = activeDays.maxByOrNull { it.amount }!!
            DayStatHighlight(
                dateTimestamp = maxDay.dateTimestamp,
                dateFormatted = maxDay.dateFormatted,
                dayOfWeek = maxDay.dayOfWeekFull,
                amount = maxDay.amount
            )
        } else {
            null
        }

        val lowestDay = if (activeDays.isNotEmpty()) {
            val minDay = activeDays.minByOrNull { it.amount }!!
            DayStatHighlight(
                dateTimestamp = minDay.dateTimestamp,
                dateFormatted = minDay.dateFormatted,
                dayOfWeek = minDay.dayOfWeekFull,
                amount = minDay.amount
            )
        } else {
            null
        }

        CalculatedStats(
            totalExpense = total,
            transactionCount = count,
            averageExpense = avgExpense,
            activeSpendingDaysCount = activeDaysCount,
            categoryStats = categoryStats,
            dailyStats = dailyItems,
            highestDay = highestDay,
            lowestDay = lowestDay,
            highestCategory = highestCategory,
            lowestCategory = lowestCategory,
            comparison = comparison,
            budgetStat = budgetStat
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalculatedStats()
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultCategories()
        }
    }

    fun setPeriod(period: StatsPeriod) {
        _selectedPeriod.value = period
        _selectedDonutCategory.value = null
        _selectedBarItem.value = null
    }

    fun goToPreviousPeriod() {
        _selectedDonutCategory.value = null
        _selectedBarItem.value = null
        when (_selectedPeriod.value) {
            StatsPeriod.MONTH -> {
                val (newYear, newMonth) = DateUtils.getPreviousMonth(_selectedYear.value, _selectedMonth.value)
                _selectedYear.value = newYear
                _selectedMonth.value = newMonth
            }
            StatsPeriod.WEEK -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = _selectedDateTimestamp.value
                    add(Calendar.DAY_OF_YEAR, -7)
                }
                _selectedDateTimestamp.value = cal.timeInMillis
                _selectedYear.value = cal.get(Calendar.YEAR)
                _selectedMonth.value = cal.get(Calendar.MONTH) + 1
            }
            StatsPeriod.DAY -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = _selectedDateTimestamp.value
                    add(Calendar.DAY_OF_YEAR, -1)
                }
                _selectedDateTimestamp.value = cal.timeInMillis
                _selectedYear.value = cal.get(Calendar.YEAR)
                _selectedMonth.value = cal.get(Calendar.MONTH) + 1
            }
        }
    }

    fun goToNextPeriod() {
        _selectedDonutCategory.value = null
        _selectedBarItem.value = null
        when (_selectedPeriod.value) {
            StatsPeriod.MONTH -> {
                val (newYear, newMonth) = DateUtils.getNextMonth(_selectedYear.value, _selectedMonth.value)
                _selectedYear.value = newYear
                _selectedMonth.value = newMonth
            }
            StatsPeriod.WEEK -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = _selectedDateTimestamp.value
                    add(Calendar.DAY_OF_YEAR, 7)
                }
                _selectedDateTimestamp.value = cal.timeInMillis
                _selectedYear.value = cal.get(Calendar.YEAR)
                _selectedMonth.value = cal.get(Calendar.MONTH) + 1
            }
            StatsPeriod.DAY -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = _selectedDateTimestamp.value
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                _selectedDateTimestamp.value = cal.timeInMillis
                _selectedYear.value = cal.get(Calendar.YEAR)
                _selectedMonth.value = cal.get(Calendar.MONTH) + 1
            }
        }
    }

    fun selectDate(timestamp: Long) {
        _selectedDateTimestamp.value = timestamp
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        _selectedYear.value = cal.get(Calendar.YEAR)
        _selectedMonth.value = cal.get(Calendar.MONTH) + 1
        _selectedDonutCategory.value = null
        _selectedBarItem.value = null
    }

    fun selectDonutCategory(item: CategoryStatItem?) {
        _selectedDonutCategory.value = if (_selectedDonutCategory.value == item) null else item
    }

    fun selectBarItem(item: DailyStatItem?) {
        _selectedBarItem.value = if (_selectedBarItem.value == item) null else item
    }
}
