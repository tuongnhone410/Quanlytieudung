package com.example.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Expense
import com.example.data.model.ExpenseWithCategory
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

enum class HistoryFilter(val displayName: String) {
    ALL("Tất cả"),
    TODAY("Hôm nay"),
    THIS_WEEK("Tuần này"),
    THIS_MONTH("Tháng này"),
    LAST_MONTH("Tháng trước"),
    CUSTOM_DATE("Chọn ngày")
}

enum class HistorySortOption(val displayName: String) {
    NEWEST_FIRST("Mới nhất"),
    OLDEST_FIRST("Cũ nhất"),
    AMOUNT_DESC("Số tiền cao → thấp"),
    AMOUNT_ASC("Số tiền thấp → cao")
}

data class DateGroup(
    val dateString: String,
    val totalAmount: Long,
    val items: List<ExpenseWithCategory>
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository = ExpenseRepository.getInstance(
        AppDatabase.getInstance(application)
    )

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedFilter = MutableStateFlow(HistoryFilter.ALL)
    val selectedFilter: StateFlow<HistoryFilter> = _selectedFilter.asStateFlow()

    private val _customSelectedDate = MutableStateFlow(System.currentTimeMillis())
    val customSelectedDate: StateFlow<Long> = _customSelectedDate.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSort = MutableStateFlow(HistorySortOption.NEWEST_FIRST)
    val selectedSort: StateFlow<HistorySortOption> = _selectedSort.asStateFlow()

    private val _selectedExpenseForDetail = MutableStateFlow<ExpenseWithCategory?>(null)
    val selectedExpenseForDetail: StateFlow<ExpenseWithCategory?> = _selectedExpenseForDetail.asStateFlow()

    // 1. Time range filtered stream
    @OptIn(ExperimentalCoroutinesApi::class)
    private val timeFilteredExpenses: StateFlow<List<ExpenseWithCategory>> = combine(
        _selectedFilter,
        _customSelectedDate
    ) { filter, customDate ->
        Pair(filter, customDate)
    }.flatMapLatest { (filter, customDate) ->
        val now = System.currentTimeMillis()
        val (curYear, curMonth) = DateUtils.getYearAndMonth(now)
        val (lastMonthYear, lastMonthVal) = DateUtils.getPreviousMonth(curYear, curMonth)

        when (filter) {
            HistoryFilter.ALL -> repository.allExpenses
            HistoryFilter.TODAY -> repository.getExpensesByDateRange(
                startDate = DateUtils.getStartOfDay(now),
                endDate = DateUtils.getEndOfDay(now)
            )
            HistoryFilter.THIS_WEEK -> repository.getExpensesByDateRange(
                startDate = DateUtils.getStartOfWeek(now),
                endDate = DateUtils.getEndOfWeek(now)
            )
            HistoryFilter.THIS_MONTH -> repository.getExpensesByDateRange(
                startDate = DateUtils.getStartOfMonth(curYear, curMonth),
                endDate = DateUtils.getEndOfMonth(curYear, curMonth)
            )
            HistoryFilter.LAST_MONTH -> repository.getExpensesByDateRange(
                startDate = DateUtils.getStartOfMonth(lastMonthYear, lastMonthVal),
                endDate = DateUtils.getEndOfMonth(lastMonthYear, lastMonthVal)
            )
            HistoryFilter.CUSTOM_DATE -> repository.getExpensesByDateRange(
                startDate = DateUtils.getStartOfDay(customDate),
                endDate = DateUtils.getEndOfDay(customDate)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 2. Fully filtered and sorted list (Time + Category + Search + Sort)
    val filteredExpenses: StateFlow<List<ExpenseWithCategory>> = combine(
        timeFilteredExpenses,
        _selectedCategoryId,
        _searchQuery,
        _selectedSort
    ) { expenses, categoryId, query, sortOption ->
        var result = expenses

        // Category Filter
        if (categoryId != null) {
            result = result.filter { it.expense.categoryId == categoryId }
        }

        // Search Query (Note & Category name, case-insensitive)
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isNotBlank()) {
            result = result.filter { item ->
                item.expense.note.lowercase().contains(cleanQuery) ||
                (item.category?.name?.lowercase()?.contains(cleanQuery) == true)
            }
        }

        // Sorting
        result = when (sortOption) {
            HistorySortOption.NEWEST_FIRST -> result.sortedByDescending { it.expense.date }
            HistorySortOption.OLDEST_FIRST -> result.sortedBy { it.expense.date }
            HistorySortOption.AMOUNT_DESC -> result.sortedByDescending { it.expense.amount }
            HistorySortOption.AMOUNT_ASC -> result.sortedBy { it.expense.amount }
        }

        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 3. Filtered Total Amount
    val filteredTotal: StateFlow<Long> = filteredExpenses.map { list ->
        list.sumOf { it.expense.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    // 4. Date Groups for Clean Visual Scanning
    val dateGroups: StateFlow<List<DateGroup>> = combine(
        filteredExpenses,
        _selectedSort
    ) { list, sortOption ->
        if (list.isEmpty()) {
            emptyList()
        } else if (sortOption == HistorySortOption.AMOUNT_DESC || sortOption == HistorySortOption.AMOUNT_ASC) {
            // When sorted by amount, present under a single unified group so order by amount is strictly preserved
            listOf(
                DateGroup(
                    dateString = if (sortOption == HistorySortOption.AMOUNT_DESC) "Xếp theo số tiền: Cao → Thấp" else "Xếp theo số tiền: Thấp → Cao",
                    totalAmount = list.sumOf { it.expense.amount },
                    items = list
                )
            )
        } else {
            // Grouped by Day (09/08/2026, 08/08/2026, etc.)
            list.groupBy { DateUtils.formatDate(it.expense.date) }
                .map { (dateStr, items) ->
                    DateGroup(
                        dateString = dateStr,
                        totalAmount = items.sumOf { it.expense.amount },
                        items = items
                    )
                }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultCategories()
        }
    }

    fun setFilter(filter: HistoryFilter) {
        _selectedFilter.value = filter
    }

    fun setCustomDate(timestamp: Long) {
        _customSelectedDate.value = timestamp
        _selectedFilter.value = HistoryFilter.CUSTOM_DATE
    }

    fun setCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(sortOption: HistorySortOption) {
        _selectedSort.value = sortOption
    }

    fun resetFilters() {
        _selectedFilter.value = HistoryFilter.ALL
        _selectedCategoryId.value = null
        _searchQuery.value = ""
        _selectedSort.value = HistorySortOption.NEWEST_FIRST
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
