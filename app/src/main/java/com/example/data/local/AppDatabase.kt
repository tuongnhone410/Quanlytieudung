package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.MonthlyBudgetDao
import com.example.data.model.Category
import com.example.data.model.Expense
import com.example.data.model.MonthlyBudget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Expense::class,
        Category::class,
        MonthlyBudget::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun monthlyBudgetDao(): MonthlyBudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val DEFAULT_CATEGORIES = listOf(
            Category(id = 1, name = "Ăn uống", icon = "🍚"),
            Category(id = 2, name = "Cà phê / Đồ uống", icon = "🥤"),
            Category(id = 3, name = "Xăng xe / Đi lại", icon = "🛵"),
            Category(id = 4, name = "Nhà cửa", icon = "🏠"),
            Category(id = 5, name = "Mua sắm", icon = "🛒"),
            Category(id = 6, name = "Trả nợ", icon = "💳"),
            Category(id = 7, name = "Sức khỏe", icon = "💊"),
            Category(id = 8, name = "Giải trí", icon = "🎮"),
            Category(id = 9, name = "Điện thoại / Internet", icon = "📱"),
            Category(id = 10, name = "Công việc", icon = "💼"),
            Category(id = 11, name = "Gia đình", icon = "👨‍👩‍👧"),
            Category(id = 12, name = "Khác", icon = "📦")
        )

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Prepopulate default categories in background coroutine
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).categoryDao().insertCategories(DEFAULT_CATEGORIES)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
