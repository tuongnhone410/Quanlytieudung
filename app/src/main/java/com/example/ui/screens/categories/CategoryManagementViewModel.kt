package com.example.ui.screens.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryItemUiState(
    val category: Category,
    val expenseCount: Int,
    val isDefaultFallback: Boolean
)

data class AddEditCategoryDialogState(
    val isOpen: Boolean = false,
    val categoryToEdit: Category? = null,
    val name: String = "",
    val icon: String = "📦",
    val errorMessage: String? = null
)

data class DeleteCategoryDialogState(
    val isOpen: Boolean = false,
    val categoryToDelete: Category? = null,
    val expenseCount: Int = 0
)

class CategoryManagementViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ExpenseRepository.getInstance(AppDatabase.getInstance(application))

    private val _addEditDialogState = MutableStateFlow(AddEditCategoryDialogState())
    val addEditDialogState: StateFlow<AddEditCategoryDialogState> = _addEditDialogState.asStateFlow()

    private val _deleteDialogState = MutableStateFlow(DeleteCategoryDialogState())
    val deleteDialogState: StateFlow<DeleteCategoryDialogState> = _deleteDialogState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private val _infoDialogMessage = MutableStateFlow<String?>(null)
    val infoDialogMessage: StateFlow<String?> = _infoDialogMessage.asStateFlow()

    val categoriesWithCount: StateFlow<List<CategoryItemUiState>> = combine(
        repository.allCategories,
        repository.allExpenses
    ) { categories, expenses ->
        val expenseCounts = expenses.groupBy { it.expense.categoryId }
            .mapValues { it.value.size }

        categories.map { category ->
            val count = expenseCounts[category.id] ?: 0
            val isFallback = category.name.equals("Khác", ignoreCase = true) || category.id == 12L
            CategoryItemUiState(
                category = category,
                expenseCount = count,
                isDefaultFallback = isFallback
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun openAddCategoryDialog() {
        _addEditDialogState.value = AddEditCategoryDialogState(
            isOpen = true,
            categoryToEdit = null,
            name = "",
            icon = "🏷️",
            errorMessage = null
        )
    }

    fun openEditCategoryDialog(category: Category) {
        _addEditDialogState.value = AddEditCategoryDialogState(
            isOpen = true,
            categoryToEdit = category,
            name = category.name,
            icon = category.icon,
            errorMessage = null
        )
    }

    fun updateDialogName(name: String) {
        _addEditDialogState.value = _addEditDialogState.value.copy(
            name = name,
            errorMessage = null
        )
    }

    fun updateDialogIcon(icon: String) {
        _addEditDialogState.value = _addEditDialogState.value.copy(
            icon = icon
        )
    }

    fun dismissAddEditDialog() {
        _addEditDialogState.value = AddEditCategoryDialogState(isOpen = false)
    }

    fun saveCategory() {
        val state = _addEditDialogState.value
        val name = state.name.trim()
        val icon = state.icon.trim().ifEmpty { "📦" }

        if (name.isBlank()) {
            _addEditDialogState.value = state.copy(errorMessage = "Vui lòng nhập tên danh mục")
            return
        }
        if (name.length > 30) {
            _addEditDialogState.value = state.copy(errorMessage = "Tên danh mục không được vượt quá 30 ký tự")
            return
        }

        viewModelScope.launch {
            if (state.categoryToEdit != null) {
                // Update
                val result = repository.updateCategory(
                    id = state.categoryToEdit.id,
                    name = name,
                    icon = icon
                )
                if (result.isSuccess) {
                    dismissAddEditDialog()
                    _snackbarMessage.emit("Đã cập nhật danh mục \"$name\"")
                } else {
                    _addEditDialogState.value = state.copy(
                        errorMessage = result.exceptionOrNull()?.message ?: "Cập nhật danh mục thất bại"
                    )
                }
            } else {
                // Add
                val result = repository.addCategory(name = name, icon = icon)
                if (result.isSuccess) {
                    dismissAddEditDialog()
                    _snackbarMessage.emit("Đã thêm danh mục \"$name\"")
                } else {
                    _addEditDialogState.value = state.copy(
                        errorMessage = result.exceptionOrNull()?.message ?: "Thêm danh mục thất bại"
                    )
                }
            }
        }
    }

    fun requestDeleteCategory(item: CategoryItemUiState) {
        if (item.isDefaultFallback) {
            _infoDialogMessage.value = "Danh mục \"${item.category.name}\" là danh mục mặc định của hệ thống, không thể xóa."
            return
        }

        _deleteDialogState.value = DeleteCategoryDialogState(
            isOpen = true,
            categoryToDelete = item.category,
            expenseCount = item.expenseCount
        )
    }

    fun dismissDeleteDialog() {
        _deleteDialogState.value = DeleteCategoryDialogState(isOpen = false)
    }

    fun confirmDeleteCategory() {
        val state = _deleteDialogState.value
        val category = state.categoryToDelete ?: return

        viewModelScope.launch {
            val result = if (state.expenseCount > 0) {
                repository.deleteCategoryWithReassign(category.id)
            } else {
                repository.deleteCategoryDirect(category.id)
            }

            dismissDeleteDialog()
            if (result.isSuccess) {
                if (state.expenseCount > 0) {
                    _snackbarMessage.emit("Đã chuyển ${state.expenseCount} giao dịch sang \"Khác\" và xóa danh mục \"${category.name}\"")
                } else {
                    _snackbarMessage.emit("Đã xóa danh mục \"${category.name}\"")
                }
            } else {
                _snackbarMessage.emit("Lỗi: ${result.exceptionOrNull()?.message ?: "Không thể xóa danh mục"}")
            }
        }
    }

    fun dismissInfoDialog() {
        _infoDialogMessage.value = null
    }
}
