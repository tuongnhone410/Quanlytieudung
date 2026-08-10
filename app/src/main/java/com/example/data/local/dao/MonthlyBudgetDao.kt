package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MonthlyBudget
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyBudgetDao {
    @Query("SELECT * FROM monthly_budgets WHERE year = :year AND month = :month LIMIT 1")
    fun getBudgetForMonth(year: Int, month: Int): Flow<MonthlyBudget?>

    @Query("SELECT * FROM monthly_budgets WHERE year = :year AND month = :month LIMIT 1")
    suspend fun getBudgetForMonthDirect(year: Int, month: Int): MonthlyBudget?

    @Query("SELECT * FROM monthly_budgets ORDER BY year DESC, month DESC")
    fun getAllBudgets(): Flow<List<MonthlyBudget>>

    @Query("SELECT * FROM monthly_budgets ORDER BY year DESC, month DESC")
    suspend fun getAllBudgetsDirect(): List<MonthlyBudget>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: MonthlyBudget): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<MonthlyBudget>): List<Long>

    @Update
    suspend fun updateBudget(budget: MonthlyBudget)

    @Delete
    suspend fun deleteBudget(budget: MonthlyBudget)

    @Query("DELETE FROM monthly_budgets WHERE year = :year AND month = :month")
    suspend fun deleteBudgetByMonth(year: Int, month: Int)

    @Query("DELETE FROM monthly_budgets")
    suspend fun deleteAllBudgets()
}

