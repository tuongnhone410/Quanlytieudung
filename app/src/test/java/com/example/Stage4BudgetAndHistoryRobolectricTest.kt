package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.MonthlyBudgetDao
import com.example.data.model.Category
import com.example.data.model.Expense
import com.example.data.model.MonthlyBudget
import com.example.data.repository.ExpenseRepository
import com.example.ui.screens.overview.BudgetStatus
import com.example.util.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
class Stage4BudgetAndHistoryRobolectricTest {

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

    private fun createTimestamp(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    @Test
    fun testMandatoryBudgetLifecycleWorkflow() {
        runBlocking {
            // 1. Tạo ngân sách: 10.000.000đ cho tháng 8/2026
            repository.setBudgetForMonth(2026, 8, 10_000_000L)
            var budget = repository.getBudgetForMonth(2026, 8).first()
            assertNotNull(budget)
            assertEquals(10_000_000L, budget!!.amount)

            val startAug = DateUtils.getStartOfMonth(2026, 8)
            val endAug = DateUtils.getEndOfMonth(2026, 8)

            // 2. Tạo giao dịch: 1.000.000đ
            repository.insertExpense(
                Expense(amount = 1_000_000L, categoryId = 1, note = "Ăn uống đầu tháng", date = createTimestamp(2026, 8, 1))
            )

            // Kiểm tra: Đã chi 1.000.000đ, Còn lại: 9.000.000đ, 10%
            var spent = repository.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertEquals(1_000_000L, spent)
            var remaining = (budget.amount - spent).coerceAtLeast(0L)
            assertEquals(9_000_000L, remaining)
            var percentage = (spent.toDouble() / budget.amount.toDouble() * 100.0).toFloat()
            assertEquals(10f, percentage, 0.01f)
            var status = when {
                percentage >= 100f -> BudgetStatus.OVER
                percentage >= 80f -> BudgetStatus.WARNING
                else -> BudgetStatus.NORMAL
            }
            assertEquals(BudgetStatus.NORMAL, status)

            // 3. Thêm giao dịch: 7.500.000đ -> Tổng 8.500.000đ
            repository.insertExpense(
                Expense(amount = 7_500_000L, categoryId = 5, note = "Mua sắm đồ dùng", date = createTimestamp(2026, 8, 5))
            )

            spent = repository.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertEquals(8_500_000L, spent)
            remaining = (budget.amount - spent).coerceAtLeast(0L)
            assertEquals(1_500_000L, remaining)
            percentage = (spent.toDouble() / budget.amount.toDouble() * 100.0).toFloat()
            assertEquals(85f, percentage, 0.01f)
            status = when {
                percentage >= 100f -> BudgetStatus.OVER
                percentage >= 80f -> BudgetStatus.WARNING
                else -> BudgetStatus.NORMAL
            }
            assertEquals(BudgetStatus.WARNING, status) // Gần đạt ngân sách

            // 4. Thêm: 2.000.000đ -> Tổng 10.500.000đ
            repository.insertExpense(
                Expense(amount = 2_000_000L, categoryId = 3, note = "Sửa xe", date = createTimestamp(2026, 8, 8))
            )

            spent = repository.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertEquals(10_500_000L, spent)
            remaining = (budget.amount - spent).coerceAtLeast(0L)
            assertEquals(0L, remaining)
            val overAmount = if (spent > budget.amount) spent - budget.amount else 0L
            assertEquals(500_000L, overAmount) // Đã vượt: 500.000đ
            percentage = (spent.toDouble() / budget.amount.toDouble() * 100.0).toFloat()
            assertEquals(105f, percentage, 0.01f)
            status = when {
                percentage >= 100f -> BudgetStatus.OVER
                percentage >= 80f -> BudgetStatus.WARNING
                else -> BudgetStatus.NORMAL
            }
            assertEquals(BudgetStatus.OVER, status) // Đã vượt ngân sách

            // 5. Xóa ngân sách -> Giao dịch vẫn còn, Màn hình chuyển thành "Chưa đặt ngân sách"
            repository.deleteBudgetForMonth(2026, 8)
            budget = repository.getBudgetForMonth(2026, 8).first()
            assertNull(budget)

            val finalSpent = repository.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertEquals(10_500_000L, finalSpent) // Giao dịch vẫn còn nguyên vẹn!
            val count = repository.getExpensesByDateRange(startAug, endAug).first().size
            assertEquals(3, count)
        }
    }

    @Test
    fun testMonthSwitchingIndependence() {
        runBlocking {
            // Section 27 Test Chuyển tháng
            // Tháng 7: Ngân sách 8.000.000đ, Chi 5.000.000đ
            repository.setBudgetForMonth(2026, 7, 8_000_000L)
            repository.insertExpense(
                Expense(amount = 5_000_000L, categoryId = 1, note = "Chi tháng 7", date = createTimestamp(2026, 7, 15))
            )

            // Tháng 8: Ngân sách 10.000.000đ, Chi 6.000.000đ
            repository.setBudgetForMonth(2026, 8, 10_000_000L)
            repository.insertExpense(
                Expense(amount = 6_000_000L, categoryId = 2, note = "Chi tháng 8", date = createTimestamp(2026, 8, 15))
            )

            // Tháng 9: Chưa đặt ngân sách
            val budgetSep = repository.getBudgetForMonth(2026, 9).first()
            assertNull(budgetSep)

            // Kiểm tra Tháng 7
            val startJul = DateUtils.getStartOfMonth(2026, 7)
            val endJul = DateUtils.getEndOfMonth(2026, 7)
            val budgetJul = repository.getBudgetForMonth(2026, 7).first()
            val spentJul = repository.getTotalExpenseAmountBetween(startJul, endJul).first() ?: 0L
            assertNotNull(budgetJul)
            assertEquals(8_000_000L, budgetJul!!.amount)
            assertEquals(5_000_000L, spentJul)

            // Kiểm tra Tháng 8
            val startAug = DateUtils.getStartOfMonth(2026, 8)
            val endAug = DateUtils.getEndOfMonth(2026, 8)
            val budgetAug = repository.getBudgetForMonth(2026, 8).first()
            val spentAug = repository.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertNotNull(budgetAug)
            assertEquals(10_000_000L, budgetAug!!.amount)
            assertEquals(6_000_000L, spentAug)
        }
    }

    @Test
    fun testAdvancedHistoryFilteringAndSearch() {
        runBlocking {
            // Insert test dataset
            val t1 = createTimestamp(2026, 8, 1) // Ăn sáng (Ăn uống - 1) 50k
            val t2 = createTimestamp(2026, 8, 2) // Cà phê sáng (Cà phê - 2) 35k
            val t3 = createTimestamp(2026, 8, 2) // Đổ xăng xe máy (Xăng xe - 3) 70k
            val t4 = createTimestamp(2026, 8, 9) // Ăn trưa phở bò (Ăn uống - 1) 60k

            repository.insertExpense(Expense(amount = 50_000L, categoryId = 1, note = "Ăn sáng", date = t1))
            repository.insertExpense(Expense(amount = 35_000L, categoryId = 2, note = "Cà phê sáng", date = t2))
            repository.insertExpense(Expense(amount = 70_000L, categoryId = 3, note = "Đổ xăng xe máy", date = t3))
            repository.insertExpense(Expense(amount = 60_000L, categoryId = 1, note = "Ăn trưa phở bò", date = t4))

            val allItems = repository.allExpenses.first()
            assertEquals(4, allItems.size)

            // 1. Filter by Category: Ăn uống (id = 1)
            val foodItems = allItems.filter { it.expense.categoryId == 1L }
            assertEquals(2, foodItems.size)
            assertEquals(110_000L, foodItems.sumOf { it.expense.amount })

            // 2. Search Query: "phở" (case-insensitive)
            val searchPho = allItems.filter {
                it.expense.note.lowercase().contains("phở") ||
                (it.category?.name?.lowercase()?.contains("phở") == true)
            }
            assertEquals(1, searchPho.size)
            assertEquals(60_000L, searchPho[0].expense.amount)

            // 3. Search Query by Category name: "xăng xe"
            val searchGas = allItems.filter {
                it.expense.note.lowercase().contains("xăng xe") ||
                (it.category?.name?.lowercase()?.contains("xăng xe") == true)
            }
            assertEquals(1, searchGas.size)
            assertEquals(70_000L, searchGas[0].expense.amount)

            // 4. Combined Filter: Date range (Aug 2) + Category (Ăn uống) -> should be 0
            val aug2Start = DateUtils.getStartOfDay(t2)
            val aug2End = DateUtils.getEndOfDay(t2)
            val aug2Expenses = repository.getExpensesByDateRange(aug2Start, aug2End).first()
            val aug2Food = aug2Expenses.filter { it.expense.categoryId == 1L }
            assertEquals(0, aug2Food.size)

            // 5. Combined Filter: Date range (Aug 2) + Category (Cà phê) -> 35k
            val aug2Coffee = aug2Expenses.filter { it.expense.categoryId == 2L }
            assertEquals(1, aug2Coffee.size)
            assertEquals(35_000L, aug2Coffee[0].expense.amount)

            // 6. Sorting tests
            val sortedByAmountDesc = allItems.sortedByDescending { it.expense.amount }
            assertEquals(70_000L, sortedByAmountDesc.first().expense.amount) // Xăng xe 70k
            assertEquals(35_000L, sortedByAmountDesc.last().expense.amount)  // Cà phê 35k

            val sortedByAmountAsc = allItems.sortedBy { it.expense.amount }
            assertEquals(35_000L, sortedByAmountAsc.first().expense.amount)  // Cà phê 35k
            assertEquals(70_000L, sortedByAmountAsc.last().expense.amount)   // Xăng xe 70k
        }
    }
}
