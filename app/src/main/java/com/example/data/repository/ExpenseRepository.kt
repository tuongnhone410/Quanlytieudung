package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.MonthlyBudgetDao
import com.example.data.model.BackupData
import com.example.data.model.Category
import com.example.data.model.Expense
import com.example.data.model.ExpenseWithCategory
import com.example.data.model.MonthlyBudget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val database: AppDatabase,
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val monthlyBudgetDao: MonthlyBudgetDao
) {
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allExpenses: Flow<List<ExpenseWithCategory>> = expenseDao.getAllExpensesWithCategory()
    val recentExpenses: Flow<List<ExpenseWithCategory>> = expenseDao.getRecentExpensesWithCategory(15)
    val totalExpenseAmount: Flow<Long?> = expenseDao.getTotalExpenseAmount()
    val expenseCount: Flow<Int> = expenseDao.getExpenseCount()

    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseWithCategory>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate)
    }

    fun getTotalExpenseAmountBetween(startDate: Long, endDate: Long): Flow<Long?> {
        return expenseDao.getTotalExpenseAmountBetween(startDate, endDate)
    }

    fun getExpenseById(id: Long): Flow<ExpenseWithCategory?> {
        return expenseDao.getExpenseById(id)
    }

    suspend fun getExpenseByIdDirect(id: Long): ExpenseWithCategory? = withContext(Dispatchers.IO) {
        expenseDao.getExpenseByIdDirect(id)
    }

    suspend fun ensureDefaultCategories() = withContext(Dispatchers.IO) {
        val count = categoryDao.getCategoryCount()
        if (count == 0) {
            categoryDao.insertCategories(AppDatabase.DEFAULT_CATEGORIES)
        }
    }

    suspend fun insertExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) = withContext(Dispatchers.IO) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpenseById(id)
    }

    fun getBudgetForMonth(year: Int, month: Int): Flow<MonthlyBudget?> {
        return monthlyBudgetDao.getBudgetForMonth(year, month)
    }

    suspend fun setBudgetForMonth(year: Int, month: Int, amount: Long) = withContext(Dispatchers.IO) {
        val existing = monthlyBudgetDao.getBudgetForMonthDirect(year, month)
        if (existing != null) {
            val updated = existing.copy(
                amount = amount,
                updatedAt = System.currentTimeMillis()
            )
            monthlyBudgetDao.insertOrUpdateBudget(updated)
        } else {
            val newBudget = MonthlyBudget(
                year = year,
                month = month,
                amount = amount,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            monthlyBudgetDao.insertOrUpdateBudget(newBudget)
        }
    }

    suspend fun deleteBudgetForMonth(year: Int, month: Int) = withContext(Dispatchers.IO) {
        monthlyBudgetDao.deleteBudgetByMonth(year, month)
    }

    // Category Management
    fun getExpenseCountForCategory(categoryId: Long): Flow<Int> {
        return expenseDao.getExpenseCountForCategory(categoryId)
    }

    suspend fun getExpenseCountForCategoryDirect(categoryId: Long): Int = withContext(Dispatchers.IO) {
        expenseDao.getExpenseCountForCategoryDirect(categoryId)
    }

    suspend fun addCategory(name: String, icon: String): Result<Long> = withContext(Dispatchers.IO) {
        val trimmedName = name.trim()
        val trimmedIcon = icon.trim().ifEmpty { "📦" }

        if (trimmedName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Tên danh mục không được để trống."))
        }
        if (trimmedName.length > 30) {
            return@withContext Result.failure(IllegalArgumentException("Tên danh mục không được vượt quá 30 ký tự."))
        }

        val existing = categoryDao.getCategoryByNameDirect(trimmedName)
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("Danh mục \"$trimmedName\" đã tồn tại."))
        }

        val now = System.currentTimeMillis()
        val newCategory = Category(
            name = trimmedName,
            icon = trimmedIcon,
            createdAt = now,
            updatedAt = now
        )
        val id = categoryDao.insertCategory(newCategory)
        Result.success(id)
    }

    suspend fun updateCategory(id: Long, name: String, icon: String): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmedName = name.trim()
        val trimmedIcon = icon.trim().ifEmpty { "📦" }

        if (trimmedName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Tên danh mục không được để trống."))
        }
        if (trimmedName.length > 30) {
            return@withContext Result.failure(IllegalArgumentException("Tên danh mục không được vượt quá 30 ký tự."))
        }

        val existingByName = categoryDao.getCategoryByNameDirect(trimmedName)
        if (existingByName != null && existingByName.id != id) {
            return@withContext Result.failure(IllegalArgumentException("Danh mục có tên \"$trimmedName\" đã tồn tại."))
        }

        val current = categoryDao.getCategoryByIdDirect(id)
            ?: return@withContext Result.failure(IllegalArgumentException("Không tìm thấy danh mục cần cập nhật."))

        val updated = current.copy(
            name = trimmedName,
            icon = trimmedIcon,
            updatedAt = System.currentTimeMillis()
        )
        categoryDao.updateCategory(updated)
        Result.success(Unit)
    }

    suspend fun getFallbackCategoryDirect(): Category = withContext(Dispatchers.IO) {
        categoryDao.getCategoryByNameDirect("Khác")
            ?: categoryDao.getCategoryByIdDirect(12L)
            ?: run {
                val fallback = Category(id = 12L, name = "Khác", icon = "📦")
                categoryDao.insertCategoryReplace(fallback)
                fallback
            }
    }

    suspend fun deleteCategoryWithReassign(categoryId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val fallback = getFallbackCategoryDirect()
            if (categoryId == fallback.id) {
                return@withContext Result.failure(IllegalStateException("Danh mục \"Khác\" là danh mục mặc định, không thể xóa."))
            }

            database.withTransaction {
                // Reassign all expenses belonging to categoryId to fallback
                expenseDao.reassignCategory(oldCategoryId = categoryId, newCategoryId = fallback.id)
                // Delete category
                categoryDao.deleteCategoryById(categoryId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategoryDirect(categoryId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val fallback = getFallbackCategoryDirect()
            if (categoryId == fallback.id) {
                return@withContext Result.failure(IllegalStateException("Danh mục \"Khác\" là danh mục mặc định, không thể xóa."))
            }
            val count = expenseDao.getExpenseCountForCategoryDirect(categoryId)
            if (count > 0) {
                return@withContext Result.failure(IllegalStateException("Danh mục đang có $count giao dịch, cần chuyển giao dịch trước khi xóa."))
            }
            categoryDao.deleteCategoryById(categoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Backup and Restore
    suspend fun generateBackupJson(): String = withContext(Dispatchers.IO) {
        val categories = categoryDao.getAllCategoriesDirect()
        val budgets = monthlyBudgetDao.getAllBudgetsDirect()
        val expenses = expenseDao.getAllExpensesDirect()

        val backupData = BackupData(
            version = BackupData.CURRENT_VERSION,
            appName = BackupData.APP_NAME,
            createdAt = System.currentTimeMillis(),
            categories = categories,
            budgets = budgets,
            expenses = expenses
        )
        backupData.toJsonString()
    }

    suspend fun restoreBackup(backupData: BackupData): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                // Clear existing data
                expenseDao.deleteAllExpenses()
                monthlyBudgetDao.deleteAllBudgets()
                categoryDao.deleteAllCategories()

                // Insert restored categories
                categoryDao.insertCategoriesReplace(backupData.categories)

                // Ensure fallback "Khác" category exists
                val hasKhac = backupData.categories.any { it.name == "Khác" }
                if (!hasKhac) {
                    categoryDao.insertCategoryReplace(Category(id = 12L, name = "Khác", icon = "📦"))
                }

                // Insert restored budgets
                if (backupData.budgets.isNotEmpty()) {
                    monthlyBudgetDao.insertBudgets(backupData.budgets)
                }

                // Insert restored expenses
                if (backupData.expenses.isNotEmpty()) {
                    expenseDao.insertExpenses(backupData.expenses)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        database.withTransaction {
            expenseDao.deleteAllExpenses()
            monthlyBudgetDao.deleteAllBudgets()
            categoryDao.deleteAllCategories()
            categoryDao.insertCategories(AppDatabase.DEFAULT_CATEGORIES)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ExpenseRepository? = null

        fun getInstance(database: AppDatabase): ExpenseRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ExpenseRepository(
                    database = database,
                    expenseDao = database.expenseDao(),
                    categoryDao = database.categoryDao(),
                    monthlyBudgetDao = database.monthlyBudgetDao()
                )
                INSTANCE = instance
                instance
            }
        }
    }
}

