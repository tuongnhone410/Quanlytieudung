package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.MonthlyBudgetDao
import com.example.data.model.Expense
import com.example.data.repository.ExpenseRepository
import com.example.ui.screens.stats.StatsPeriod
import com.example.ui.screens.stats.StatsViewModel
import com.example.util.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Stage5StatisticsRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var expenseDao: ExpenseDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var monthlyBudgetDao: MonthlyBudgetDao
    private lateinit var repository: ExpenseRepository

    @Before
    fun setUp() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
            expenseDao = db.expenseDao()
            categoryDao = db.categoryDao()
            monthlyBudgetDao = db.monthlyBudgetDao()
            categoryDao.insertCategories(AppDatabase.DEFAULT_CATEGORIES)
            repository = ExpenseRepository(expenseDao, categoryDao, monthlyBudgetDao)
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun createTimestamp(year: Int, month: Int, day: Int, hour: Int = 12): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Test Requirement 25: Test Dữ Liệu Tháng 8
     * 01/08: Ăn uống 50.000đ (cat 1)
     * 02/08: Ăn uống 30.000đ (cat 1)
     * 02/08: Xăng xe 70.000đ (cat 3)
     * 03/08: Mua sắm 100.000đ (cat 5)
     * 09/08: Ăn uống 120.000đ (cat 1)
     * 09/08: Mua sắm 200.000đ (cat 5)
     */
    @Test
    fun testStage5AugustDatasetCalculations() {
        runBlocking {
            repository.insertExpense(Expense(amount = 50_000L, categoryId = 1, note = "Ăn sáng", date = createTimestamp(2026, 8, 1)))
            repository.insertExpense(Expense(amount = 30_000L, categoryId = 1, note = "Ăn nhẹ", date = createTimestamp(2026, 8, 2)))
            repository.insertExpense(Expense(amount = 70_000L, categoryId = 3, note = "Đổ xăng", date = createTimestamp(2026, 8, 2)))
            repository.insertExpense(Expense(amount = 100_000L, categoryId = 5, note = "Mua đồ", date = createTimestamp(2026, 8, 3)))
            repository.insertExpense(Expense(amount = 120_000L, categoryId = 1, note = "Ăn trưa", date = createTimestamp(2026, 8, 9)))
            repository.insertExpense(Expense(amount = 200_000L, categoryId = 5, note = "Mua sắm quần áo", date = createTimestamp(2026, 8, 9)))

            val startAug = DateUtils.getStartOfMonth(2026, 8)
            val endAug = DateUtils.getEndOfMonth(2026, 8)
            val expenses = repository.getExpensesByDateRange(startAug, endAug).first()

            // 1. Tổng tháng: 570.000đ
            val totalMonth = expenses.sumOf { it.expense.amount }
            assertEquals(570_000L, totalMonth)

            // 2. Ngày 09/08: 320.000đ
            val day9Start = DateUtils.getStartOfDay(createTimestamp(2026, 8, 9))
            val day9End = DateUtils.getEndOfDay(createTimestamp(2026, 8, 9))
            val day9Expenses = repository.getExpensesByDateRange(day9Start, day9End).first()
            val day9Total = day9Expenses.sumOf { it.expense.amount }
            assertEquals(320_000L, day9Total)

            // 3. Category totals:
            // Ăn uống (cat 1): 50k + 30k + 120k = 200.000đ
            val foodTotal = expenses.filter { it.expense.categoryId == 1L }.sumOf { it.expense.amount }
            assertEquals(200_000L, foodTotal)

            // Xăng xe (cat 3): 70.000đ
            val transportTotal = expenses.filter { it.expense.categoryId == 3L }.sumOf { it.expense.amount }
            assertEquals(70_000L, transportTotal)

            // Mua sắm (cat 5): 100k + 200k = 300.000đ
            val shoppingTotal = expenses.filter { it.expense.categoryId == 5L }.sumOf { it.expense.amount }
            assertEquals(300_000L, shoppingTotal)

            // 4. Danh mục cao nhất: Mua sắm (300.000đ)
            val categorySums = expenses.groupBy { it.expense.categoryId }
                .mapValues { entry -> entry.value.sumOf { it.expense.amount } }
            val topCategory = categorySums.maxByOrNull { it.value }
            assertEquals(5L, topCategory?.key)
            assertEquals(300_000L, topCategory?.value)

            // 5. Ngày chi nhiều nhất: 09/08 (320.000đ)
            val dailyMap = expenses.groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.expense.date }
                cal.get(Calendar.DAY_OF_MONTH)
            }.mapValues { entry -> entry.value.sumOf { it.expense.amount } }

            val maxDay = dailyMap.maxByOrNull { it.value }
            assertEquals(9, maxDay?.key)
            assertEquals(320_000L, maxDay?.value)

            // 6. Ngày chi ít nhất (> 0đ): 01/08 (50.000đ)
            val minDay = dailyMap.filter { it.value > 0L }.minByOrNull { it.value }
            assertEquals(1, minDay?.key)
            assertEquals(50_000L, minDay?.value)

            // 7. Số ngày có giao dịch: 4 ngày (01, 02, 03, 09)
            val activeDaysCount = dailyMap.filter { it.value > 0L }.size
            assertEquals(4, activeDaysCount)

            // 8. Trung bình chi tiêu: 570.000đ / 4 = 142.500đ/ngày
            val averageExpense = totalMonth / activeDaysCount
            assertEquals(142_500L, averageExpense)
        }
    }

    /**
     * Test Requirement 26: Test Tháng Trước
     * Tháng 7: 500.000đ
     * Tháng 8: 1.000.000đ
     * Kết quả: Tháng 8 = 1.000.000đ, Tháng 7 = 500.000đ, Chênh lệch = +500.000đ, Tăng: +100%
     */
    @Test
    fun testMonthOverMonthComparison() {
        runBlocking {
            // Tháng 7
            repository.insertExpense(Expense(amount = 500_000L, categoryId = 1, note = "Chi tháng 7", date = createTimestamp(2026, 7, 15)))
            // Tháng 8
            repository.insertExpense(Expense(amount = 1_000_000L, categoryId = 1, note = "Chi tháng 8", date = createTimestamp(2026, 8, 15)))

            val startJul = DateUtils.getStartOfMonth(2026, 7)
            val endJul = DateUtils.getEndOfMonth(2026, 7)
            val spentJul = repository.getTotalExpenseAmountBetween(startJul, endJul).first() ?: 0L
            assertEquals(500_000L, spentJul)

            val startAug = DateUtils.getStartOfMonth(2026, 8)
            val endAug = DateUtils.getEndOfMonth(2026, 8)
            val spentAug = repository.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertEquals(1_000_000L, spentAug)

            val difference = spentAug - spentJul
            assertEquals(500_000L, difference)

            val pct = ((difference.toDouble() / spentJul.toDouble()) * 100.0).toFloat()
            assertEquals(100.0f, pct, 0.01f)
            assertTrue(difference > 0L)
        }
    }

    /**
     * Test Requirement 27: Test Không Có Dữ Liệu
     * Tháng chưa có giao dịch: Tổng = 0đ, không crash, không chia cho 0.
     */
    @Test
    fun testEmptyDataHandling() {
        runBlocking {
            val startSep = DateUtils.getStartOfMonth(2026, 9)
            val endSep = DateUtils.getEndOfMonth(2026, 9)
            val expenses = repository.getExpensesByDateRange(startSep, endSep).first()
            val total = expenses.sumOf { it.expense.amount }
            assertEquals(0L, total)

            val activeDaysCount = expenses.groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.expense.date }
                cal.get(Calendar.DAY_OF_MONTH)
            }.filter { it.value.sumOf { e -> e.expense.amount } > 0L }.size

            val average = if (activeDaysCount > 0) total / activeDaysCount else 0L
            assertEquals(0L, average)

            val prevTotal = 0L
            val pctChange = if (prevTotal > 0L) ((total - prevTotal).toDouble() / prevTotal.toDouble() * 100.0).toFloat() else null
            assertNull(pctChange)
        }
    }

    /**
     * Test Requirement 28: Test Ngân Sách trong Statistics
     * Ngân sách: 10.000.000đ, Chi: 6.000.000đ -> 60%, Còn lại: 4.000.000đ
     */
    @Test
    fun testBudgetStatisticsIntegration() {
        runBlocking {
            repository.setBudgetForMonth(2026, 8, 10_000_000L)
            val budget = repository.getBudgetForMonth(2026, 8).first()
            assertNotNull(budget)
            assertEquals(10_000_000L, budget!!.amount)

            repository.insertExpense(Expense(amount = 6_000_000L, categoryId = 1, note = "Chi tiêu", date = createTimestamp(2026, 8, 5)))

            val startAug = DateUtils.getStartOfMonth(2026, 8)
            val endAug = DateUtils.getEndOfMonth(2026, 8)
            val total = repository.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertEquals(6_000_000L, total)

            val remaining = (budget.amount - total).coerceAtLeast(0L)
            assertEquals(4_000_000L, remaining)

            val usagePercentage = (total.toDouble() / budget.amount.toDouble() * 100.0).toFloat()
            assertEquals(60.0f, usagePercentage, 0.01f)
        }
    }

    /**
     * Test Requirement 29: Test Real-Time Reactivity
     * Thêm -> +100k
     * Sửa -> 100k thành 200k (+100k)
     * Xóa -> -200k
     */
    @Test
    fun testRealtimeStatisticsUpdates() {
        runBlocking {
            val startAug = DateUtils.getStartOfMonth(2026, 8)
            val endAug = DateUtils.getEndOfMonth(2026, 8)

            // 1. Thêm giao dịch: 100.000đ
            val expenseId = repository.insertExpense(
                Expense(amount = 100_000L, categoryId = 1, note = "Ăn trưa", date = createTimestamp(2026, 8, 10))
            )
            var total = repository.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertEquals(100_000L, total)

            // 2. Sửa giao dịch: 100.000đ -> 200.000đ
            val current = repository.getExpenseByIdDirect(expenseId)
            assertNotNull(current)
            repository.updateExpense(current!!.expense.copy(amount = 200_000L))
            total = repository.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertEquals(200_000L, total)

            // 3. Xóa giao dịch: 200.000đ -> 0đ
            repository.deleteExpenseById(expenseId)
            total = repository.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertEquals(0L, total)
        }
    }
}
