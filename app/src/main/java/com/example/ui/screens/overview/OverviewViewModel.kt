package com.example.ui.screens.overview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Expense
import com.example.data.model.ExpenseWithCategory
import com.example.data.model.MonthlyBudget
import com.example.data.repository.ExpenseRepository
import com.example.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BudgetStatus {
    NONE,
    NORMAL,
    WARNING,
    OVER
}

data class MonthlyBudgetUiState(
    val hasBudget: Boolean = false,
    val budgetAmount: Long = 0L,
    val spentAmount: Long = 0L,
    val remainingAmount: Long = 0L,
    val overAmount: Long = 0L,
    val usagePercentage: Float = 0f,
    val status: BudgetStatus = BudgetStatus.NONE
)

data class CategorySpending(
    val category: Category,
    val amount: Long,
    val percentage: Float
)

class OverviewViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository = ExpenseRepository.getInstance(
        AppDatabase.getInstance(application)
    )

    private val initialYearAndMonth = DateUtils.getYearAndMonth()
    private val _selectedYear = MutableStateFlow(initialYearAndMonth.first)
    private val _selectedMonth = MutableStateFlow(initialYearAndMonth.second)

    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    val selectedMonthHeader: StateFlow<String> = combine(_selectedYear, _selectedMonth) { year, month ->
        DateUtils.formatMonthHeader(year, month)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DateUtils.getCurrentMonthHeader()
    )

    // Today's total (00:00:00 to 23:59:59 of current day)
    val todayTotal: StateFlow<Long> = repository.getTotalExpenseAmountBetween(
        startDate = DateUtils.getStartOfDay(),
        endDate = DateUtils.getEndOfDay()
    ).map { it ?: 0L }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    // This week's total (Monday to Sunday)
    val weekTotal: StateFlow<Long> = repository.getTotalExpenseAmountBetween(
        startDate = DateUtils.getStartOfWeek(),
        endDate = DateUtils.getEndOfWeek()
    ).map { it ?: 0L }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    // Selected Month's total
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedMonthTotal: StateFlow<Long> = combine(_selectedYear, _selectedMonth) { year, month ->
        Pair(DateUtils.getStartOfMonth(year, month), DateUtils.getEndOfMonth(year, month))
    }.flatMapLatest { (startDate, endDate) ->
        repository.getTotalExpenseAmountBetween(startDate, endDate)
    }.map { it ?: 0L }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    // Selected Month's Budget Entity
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedMonthBudget: StateFlow<MonthlyBudget?> = combine(_selectedYear, _selectedMonth) { year, month ->
        Pair(year, month)
    }.flatMapLatest { (year, month) ->
        repository.getBudgetForMonth(year, month)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Derived Budget UI State
    val budgetUiState: StateFlow<MonthlyBudgetUiState> = combine(
        selectedMonthBudget,
        selectedMonthTotal
    ) { budget, spentTotal ->
        if (budget == null || budget.amount <= 0L) {
            MonthlyBudgetUiState(
                hasBudget = false,
                budgetAmount = 0L,
                spentAmount = spentTotal,
                remainingAmount = 0L,
                overAmount = 0L,
                usagePercentage = 0f,
                status = BudgetStatus.NONE
            )
        } else {
            val budgetAmount = budget.amount
            val remaining = (budgetAmount - spentTotal).coerceAtLeast(0L)
            val over = if (spentTotal > budgetAmount) spentTotal - budgetAmount else 0L
            val percentage = (spentTotal.toDouble() / budgetAmount.toDouble() * 100.0).toFloat()
            val status = when {
                percentage >= 100f -> BudgetStatus.OVER
                percentage >= 80f -> BudgetStatus.WARNING
                else -> BudgetStatus.NORMAL
            }

            MonthlyBudgetUiState(
                hasBudget = true,
                budgetAmount = budgetAmount,
                spentAmount = spentTotal,
                remainingAmount = remaining,
                overAmount = over,
                usagePercentage = percentage,
                status = status
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyBudgetUiState()
    )

    // Selected Month's expenses
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedMonthExpenses: StateFlow<List<ExpenseWithCategory>> = combine(_selectedYear, _selectedMonth) { year, month ->
        Pair(DateUtils.getStartOfMonth(year, month), DateUtils.getEndOfMonth(year, month))
    }.flatMapLatest { (startDate, endDate) ->
        repository.getExpensesByDateRange(startDate, endDate)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Category Spendings in selected month (only categories with > 0 spending)
    val categorySpendings: StateFlow<List<CategorySpending>> = combine(
        selectedMonthExpenses,
        repository.allCategories
    ) { expenses, categories ->
        val monthTotal = expenses.sumOf { it.expense.amount }
        if (monthTotal <= 0L) {
            emptyList()
        } else {
            val categoryMap = categories.associateBy { it.id }
            val amountByCategory = expenses.groupBy { it.expense.categoryId }
                .mapValues { entry -> entry.value.sumOf { it.expense.amount } }

            amountByCategory
                .filter { it.value > 0L }
                .mapNotNull { (categoryId, totalAmount) ->
                    val category = categoryMap[categoryId]
                        ?: expenses.firstOrNull { it.expense.categoryId == categoryId }?.category
                        ?: Category(id = categoryId, name = "Khác", icon = "📦")
                    val percentage = (totalAmount.toDouble() / monthTotal.toDouble() * 100.0).toFloat()
                    CategorySpending(
                        category = category,
                        amount = totalAmount,
                        percentage = percentage
                    )
                }
                .sortedByDescending { it.amount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dialog States
    private val _isBudgetDialogOpen = MutableStateFlow(false)
    val isBudgetDialogOpen: StateFlow<Boolean> = _isBudgetDialogOpen.asStateFlow()

    private val _isDeleteBudgetConfirmOpen = MutableStateFlow(false)
    val isDeleteBudgetConfirmOpen: StateFlow<Boolean> = _isDeleteBudgetConfirmOpen.asStateFlow()

    private val _selectedExpenseForDetail = MutableStateFlow<ExpenseWithCategory?>(null)
    val selectedExpenseForDetail: StateFlow<ExpenseWithCategory?> = _selectedExpenseForDetail.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultCategories()
        }
    }

    fun goToPreviousMonth() {
        val (newYear, newMonth) = DateUtils.getPreviousMonth(_selectedYear.value, _selectedMonth.value)
        _selectedYear.value = newYear
        _selectedMonth.value = newMonth
    }

    fun goToNextMonth() {
        val (newYear, newMonth) = DateUtils.getNextMonth(_selectedYear.value, _selectedMonth.value)
        _selectedYear.value = newYear
        _selectedMonth.value = newMonth
    }

    fun goToCurrentMonth() {
        val (currentYear, currentMonth) = DateUtils.getYearAndMonth()
        _selectedYear.value = currentYear
        _selectedMonth.value = currentMonth
    }

    fun showBudgetDialog() {
        _isBudgetDialogOpen.value = true
    }

    fun hideBudgetDialog() {
        _isBudgetDialogOpen.value = false
    }

    fun showDeleteBudgetConfirm() {
        _isDeleteBudgetConfirmOpen.value = true
    }

    fun hideDeleteBudgetConfirm() {
        _isDeleteBudgetConfirmOpen.value = false
    }

    fun saveBudget(amount: Long) {
        viewModelScope.launch {
            if (amount > 0L) {
                repository.setBudgetForMonth(_selectedYear.value, _selectedMonth.value, amount)
                _isBudgetDialogOpen.value = false
            }
        }
    }

    fun deleteCurrentMonthBudget() {
        viewModelScope.launch {
            repository.deleteBudgetForMonth(_selectedYear.value, _selectedMonth.value)
            _isDeleteBudgetConfirmOpen.value = false
            _isBudgetDialogOpen.value = false
        }
    }

    fun onExpenseClicked(item: ExpenseWithCategory) {
        _selectedExpenseForDetail.value = item
    }

    fun dismissExpenseDetail() {
        _selectedExpenseForDetail.value = null
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            if (_selectedExpenseForDetail.value?.expense?.id == expense.id) {
                _selectedExpenseForDetail.value = null
            }
        }
    }
}
