package com.example.ui.screens.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Expense
import com.example.data.repository.ExpenseRepository
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository = ExpenseRepository.getInstance(
        AppDatabase.getInstance(application)
    )

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var editingExpenseId: Long? = null
    private var editingExpenseCreatedAt: Long = 0L

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _amount = MutableStateFlow<Long>(0L)
    val amount: StateFlow<Long> = _amount.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultCategories()
        }
    }

    fun initExpense(expenseId: Long?) {
        if (expenseId == null || expenseId <= 0L) {
            _isEditMode.value = false
            editingExpenseId = null
            return
        }

        editingExpenseId = expenseId
        _isEditMode.value = true

        viewModelScope.launch {
            val item = repository.getExpenseByIdDirect(expenseId)
            if (item != null) {
                _amount.value = item.expense.amount
                _selectedCategoryId.value = item.expense.categoryId
                _note.value = item.expense.note
                _selectedDate.value = item.expense.date
                editingExpenseCreatedAt = item.expense.createdAt
            }
        }
    }

    fun onAmountInputChanged(rawDigits: String) {
        val parsed = CurrencyFormatter.parseAmount(rawDigits)
        _amount.value = parsed
        _errorMessage.value = null
    }

    fun addQuickAmount(delta: Long) {
        _amount.value = (_amount.value + delta).coerceAtLeast(0L)
        _errorMessage.value = null
    }

    fun clearAmount() {
        _amount.value = 0L
    }

    fun onCategorySelected(categoryId: Long) {
        _selectedCategoryId.value = categoryId
        _errorMessage.value = null
    }

    fun onNoteChanged(newNote: String) {
        _note.value = newNote
    }

    fun onDateSelected(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun saveExpense() {
        if (_isSaving.value) return

        val currentAmount = _amount.value
        val categoryId = _selectedCategoryId.value ?: categories.value.firstOrNull()?.id

        if (currentAmount <= 0L) {
            _errorMessage.value = "Vui lòng nhập số tiền lớn hơn 0"
            return
        }

        if (categoryId == null) {
            _errorMessage.value = "Vui lòng chọn danh mục"
            return
        }

        _isSaving.value = true

        viewModelScope.launch {
            try {
                val currentEditingId = editingExpenseId
                if (_isEditMode.value && currentEditingId != null) {
                    val updatedExpense = Expense(
                        id = currentEditingId,
                        amount = currentAmount,
                        categoryId = categoryId,
                        note = _note.value.trim(),
                        date = _selectedDate.value,
                        createdAt = if (editingExpenseCreatedAt > 0L) editingExpenseCreatedAt else System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateExpense(updatedExpense)
                } else {
                    val newExpense = Expense(
                        amount = currentAmount,
                        categoryId = categoryId,
                        note = _note.value.trim(),
                        date = _selectedDate.value,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.insertExpense(newExpense)
                }
                _saveSuccessEvent.emit(Unit)
            } finally {
                _isSaving.value = false
            }
        }
    }
}
