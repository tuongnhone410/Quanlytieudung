package com.example.ui.screens.stats

import androidx.compose.ui.graphics.Color
import com.example.data.model.Category

enum class StatsPeriod(val label: String) {
    DAY("Ngày"),
    WEEK("Tuần"),
    MONTH("Tháng")
}

data class CategoryStatItem(
    val category: Category,
    val amount: Long,
    val percentage: Float,
    val color: Color,
    val transactionCount: Int
)

data class DailyStatItem(
    val dateTimestamp: Long,
    val dayOfMonth: Int,
    val dayOfWeekShort: String,
    val dayOfWeekFull: String,
    val dateFormatted: String,
    val amount: Long,
    val percentageOfMax: Float,
    val transactionCount: Int
)

data class PeriodComparison(
    val currentAmount: Long = 0L,
    val previousAmount: Long = 0L,
    val difference: Long = 0L,
    val percentageChange: Float? = null,
    val hasPreviousData: Boolean = false,
    val isIncrease: Boolean = false,
    val isDecrease: Boolean = false
)

data class DayStatHighlight(
    val dateTimestamp: Long,
    val dateFormatted: String,
    val dayOfWeek: String,
    val amount: Long
)

data class CategoryStatHighlight(
    val category: Category,
    val amount: Long,
    val percentage: Float,
    val color: Color
)

data class MonthlyBudgetStat(
    val hasBudget: Boolean = false,
    val budgetAmount: Long = 0L,
    val spentAmount: Long = 0L,
    val remainingAmount: Long = 0L,
    val overAmount: Long = 0L,
    val usagePercentage: Float = 0f
)

object CategoryChartColors {
    private val defaultPalette = listOf(
        Color(0xFF10B981), // 1. Ăn uống - Emerald
        Color(0xFFF59E0B), // 2. Cà phê / Đồ uống - Amber
        Color(0xFF3B82F6), // 3. Xăng xe / Đi lại - Blue
        Color(0xFF8B5CF6), // 4. Nhà cửa - Purple
        Color(0xFFEC4899), // 5. Mua sắm - Pink
        Color(0xFFEF4444), // 6. Trả nợ - Coral Red
        Color(0xFF06B6D4), // 7. Sức khỏe - Cyan
        Color(0xFFA855F7), // 8. Giải trí - Violet
        Color(0xFF14B8A6), // 9. Điện thoại / Internet - Teal
        Color(0xFF6366F1), // 10. Công việc - Indigo
        Color(0xFFF97316), // 11. Gia đình - Orange
        Color(0xFF64748B)  // 12. Khác - Slate
    )

    fun getColorForCategory(categoryId: Long): Color {
        val index = ((categoryId - 1).coerceAtLeast(0) % defaultPalette.size).toInt()
        return defaultPalette[index]
    }

    fun getColorByIndex(index: Int): Color {
        return defaultPalette[index % defaultPalette.size]
    }
}
