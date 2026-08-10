package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.model.Category
import com.example.data.model.Expense
import com.example.util.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExpenseCalculationRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var expenseDao: ExpenseDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun createDb() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
            expenseDao = db.expenseDao()
            categoryDao = db.categoryDao()
            categoryDao.insertCategories(AppDatabase.DEFAULT_CATEGORIES)
        }
    }

    @After
    fun closeDb() {
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
    fun testMandatoryCalculationAndEditDeleteWorkflow() {
        runBlocking {
            // Section 22 Mandatory Test
            // 01/08/2026: Ăn uống (id 1) 50.000đ
            val id1 = expenseDao.insertExpense(
                Expense(amount = 50_000L, categoryId = 1, note = "Ăn sáng", date = createTimestamp(2026, 8, 1))
            )
            // 02/08/2026: Ăn uống (id 1) 30.000đ
            val id2 = expenseDao.insertExpense(
                Expense(amount = 30_000L, categoryId = 1, note = "Ăn vặt", date = createTimestamp(2026, 8, 2))
            )
            // 02/08/2026: Xăng xe (id 3) 70.000đ
            val id3 = expenseDao.insertExpense(
                Expense(amount = 70_000L, categoryId = 3, note = "Đổ xăng", date = createTimestamp(2026, 8, 2))
            )
            // 09/08/2026: Mua sắm (id 5) 100.000đ
            val id4 = expenseDao.insertExpense(
                Expense(amount = 100_000L, categoryId = 5, note = "Mua áo", date = createTimestamp(2026, 8, 9))
            )

            // 1. Kiểm tra Tổng tháng 8: 250.000đ
            val startAug = DateUtils.getStartOfMonth(2026, 8)
            val endAug = DateUtils.getEndOfMonth(2026, 8)
            val monthTotal = expenseDao.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertEquals(250_000L, monthTotal)

            // 2. Kiểm tra Tổng ngày 02/08: 100.000đ
            val dateAug2 = createTimestamp(2026, 8, 2)
            val startAug2 = DateUtils.getStartOfDay(dateAug2)
            val endAug2 = DateUtils.getEndOfDay(dateAug2)
            val aug2Total = expenseDao.getTotalExpenseAmountBetween(startAug2, endAug2).first() ?: 0L
            assertEquals(100_000L, aug2Total)

            // 3. Kiểm tra Tổng theo danh mục trong tháng 8
            val augExpenses = expenseDao.getExpensesByDateRange(startAug, endAug).first()
            val catTotals = augExpenses.groupBy { it.expense.categoryId }
                .mapValues { entry -> entry.value.sumOf { it.expense.amount } }

            assertEquals(80_000L, catTotals[1L]) // Ăn uống: 50k + 30k = 80k
            assertEquals(70_000L, catTotals[3L]) // Xăng xe: 70k
            assertEquals(100_000L, catTotals[5L]) // Mua sắm: 100k

            // 4. Sửa: Mua sắm 100.000đ -> Ăn uống 100.000đ
            val expenseToUpdate = Expense(
                id = id4,
                amount = 100_000L,
                categoryId = 1, // Changed to Ăn uống
                note = "Ăn tiệc",
                date = createTimestamp(2026, 8, 9)
            )
            expenseDao.updateExpense(expenseToUpdate)

            val updatedAugExpenses = expenseDao.getExpensesByDateRange(startAug, endAug).first()
            val updatedMonthTotal = expenseDao.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertEquals(250_000L, updatedMonthTotal) // Tổng tháng vẫn 250.000đ

            val updatedCatTotals = updatedAugExpenses.groupBy { it.expense.categoryId }
                .mapValues { entry -> entry.value.sumOf { it.expense.amount } }
            assertEquals(180_000L, updatedCatTotals[1L]) // Ăn uống tăng lên 180k
            assertEquals(null, updatedCatTotals[5L]) // Mua sắm không còn giao dịch

            // 5. Xóa giao dịch 50.000đ (id1)
            expenseDao.deleteExpenseById(id1)
            val finalMonthTotal = expenseDao.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertEquals(200_000L, finalMonthTotal) // Tổng tháng trở về 200.000đ
        }
    }

    @Test
    fun testMonthSwitchingCalculation() {
        runBlocking {
            // Section 23 Month Switching Test
            // 31/07/2026: 100.000đ
            expenseDao.insertExpense(
                Expense(amount = 100_000L, categoryId = 1, note = "Cuối tháng 7", date = createTimestamp(2026, 7, 31))
            )
            // 01/08/2026: 200.000đ
            expenseDao.insertExpense(
                Expense(amount = 200_000L, categoryId = 1, note = "Đầu tháng 8", date = createTimestamp(2026, 8, 1))
            )

            // Tháng 7/2026
            val startJul = DateUtils.getStartOfMonth(2026, 7)
            val endJul = DateUtils.getEndOfMonth(2026, 7)
            val julTotal = expenseDao.getTotalExpenseAmountBetween(startJul, endJul).first() ?: 0L
            assertEquals(100_000L, julTotal)

            // Tháng 8/2026
            val startAug = DateUtils.getStartOfMonth(2026, 8)
            val endAug = DateUtils.getEndOfMonth(2026, 8)
            val augTotal = expenseDao.getTotalExpenseAmountBetween(startAug, endAug).first() ?: 0L
            assertEquals(200_000L, augTotal)
        }
    }
}
