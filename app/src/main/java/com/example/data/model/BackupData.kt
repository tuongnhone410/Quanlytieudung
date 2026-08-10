package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupData(
    val version: Int = CURRENT_VERSION,
    val appName: String = APP_NAME,
    val createdAt: Long = System.currentTimeMillis(),
    val createdAtFormatted: String = formatDate(System.currentTimeMillis()),
    val categories: List<Category>,
    val budgets: List<MonthlyBudget>,
    val expenses: List<Expense>
) {
    fun toJsonString(): String {
        val root = JSONObject()
        root.put("version", version)
        root.put("appName", appName)
        root.put("createdAt", createdAt)
        root.put("createdAtFormatted", createdAtFormatted)

        // Categories array
        val categoriesArray = JSONArray()
        for (cat in categories) {
            val catObj = JSONObject()
            catObj.put("id", cat.id)
            catObj.put("name", cat.name)
            catObj.put("icon", cat.icon)
            catObj.put("createdAt", cat.createdAt)
            catObj.put("updatedAt", cat.updatedAt)
            categoriesArray.put(catObj)
        }
        root.put("categories", categoriesArray)

        // Budgets array
        val budgetsArray = JSONArray()
        for (budget in budgets) {
            val budgetObj = JSONObject()
            budgetObj.put("id", budget.id)
            budgetObj.put("year", budget.year)
            budgetObj.put("month", budget.month)
            budgetObj.put("amount", budget.amount)
            budgetObj.put("createdAt", budget.createdAt)
            budgetObj.put("updatedAt", budget.updatedAt)
            budgetsArray.put(budgetObj)
        }
        root.put("budgets", budgetsArray)

        // Expenses array
        val expensesArray = JSONArray()
        for (exp in expenses) {
            val expObj = JSONObject()
            expObj.put("id", exp.id)
            expObj.put("amount", exp.amount)
            expObj.put("categoryId", exp.categoryId)
            expObj.put("note", exp.note)
            expObj.put("date", exp.date)
            expObj.put("createdAt", exp.createdAt)
            expObj.put("updatedAt", exp.updatedAt)
            expensesArray.put(expObj)
        }
        root.put("expenses", expensesArray)

        return root.toString(2)
    }

    companion object {
        const val CURRENT_VERSION = 1
        const val APP_NAME = "Quản lý chi tiêu"

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        fun fromJsonString(jsonString: String): Result<BackupData> {
            return try {
                if (jsonString.isBlank()) {
                    return Result.failure(IllegalArgumentException("File dữ liệu trống."))
                }

                val root = JSONObject(jsonString)

                if (!root.has("version")) {
                    return Result.failure(IllegalArgumentException("File sao lưu thiếu thông tin phiên bản."))
                }
                val version = root.getInt("version")
                if (version < 1) {
                    return Result.failure(IllegalArgumentException("Phiên bản file sao lưu không hợp lệ (version: $version)."))
                }

                val appName = root.optString("appName", APP_NAME)
                val createdAt = root.optLong("createdAt", System.currentTimeMillis())
                val createdAtFormatted = root.optString("createdAtFormatted", formatDate(createdAt))

                // Parse categories
                val parsedCategories = mutableListOf<Category>()
                if (root.has("categories")) {
                    val catArray = root.getJSONArray("categories")
                    for (i in 0 until catArray.length()) {
                        val obj = catArray.getJSONObject(i)
                        val id = obj.getLong("id")
                        val name = obj.getString("name").trim()
                        val icon = obj.optString("icon", "📦").trim()
                        val catCreatedAt = obj.optLong("createdAt", createdAt)
                        val catUpdatedAt = obj.optLong("updatedAt", createdAt)

                        if (name.isBlank()) {
                            return Result.failure(IllegalArgumentException("Danh mục trong file có tên không hợp lệ."))
                        }

                        parsedCategories.add(
                            Category(
                                id = id,
                                name = name,
                                icon = if (icon.isNotBlank()) icon else "📦",
                                createdAt = catCreatedAt,
                                updatedAt = catUpdatedAt
                            )
                        )
                    }
                }

                // If no categories in backup, ensure fallback exists
                if (parsedCategories.isEmpty()) {
                    parsedCategories.add(Category(id = 12, name = "Khác", icon = "📦"))
                }

                // Parse budgets
                val parsedBudgets = mutableListOf<MonthlyBudget>()
                if (root.has("budgets")) {
                    val budgetArray = root.getJSONArray("budgets")
                    for (i in 0 until budgetArray.length()) {
                        val obj = budgetArray.getJSONObject(i)
                        val id = obj.optLong("id", 0L)
                        val year = obj.getInt("year")
                        val month = obj.getInt("month")
                        val amount = obj.getLong("amount")
                        val bCreatedAt = obj.optLong("createdAt", createdAt)
                        val bUpdatedAt = obj.optLong("updatedAt", createdAt)

                        if (year in 2000..2100 && month in 1..12 && amount >= 0L) {
                            parsedBudgets.add(
                                MonthlyBudget(
                                    id = id,
                                    year = year,
                                    month = month,
                                    amount = amount,
                                    createdAt = bCreatedAt,
                                    updatedAt = bUpdatedAt
                                )
                            )
                        }
                    }
                }

                // Parse expenses
                val categoryIdSet = parsedCategories.map { it.id }.toSet()
                val fallbackCategoryId = parsedCategories.firstOrNull { it.name == "Khác" }?.id
                    ?: parsedCategories.first().id

                val parsedExpenses = mutableListOf<Expense>()
                if (root.has("expenses")) {
                    val expArray = root.getJSONArray("expenses")
                    for (i in 0 until expArray.length()) {
                        val obj = expArray.getJSONObject(i)
                        val id = obj.optLong("id", 0L)
                        val amount = obj.getLong("amount")
                        var categoryId = obj.getLong("categoryId")
                        val note = obj.optString("note", "")
                        val date = obj.getLong("date")
                        val expCreatedAt = obj.optLong("createdAt", date)
                        val expUpdatedAt = obj.optLong("updatedAt", date)

                        if (amount <= 0L) {
                            return Result.failure(IllegalArgumentException("Số tiền chi tiêu không hợp lệ ở dòng $i."))
                        }

                        // If categoryId doesn't exist in restored categories, point to fallback
                        if (!categoryIdSet.contains(categoryId)) {
                            categoryId = fallbackCategoryId
                        }

                        parsedExpenses.add(
                            Expense(
                                id = id,
                                amount = amount,
                                categoryId = categoryId,
                                note = note,
                                date = if (date > 0L) date else System.currentTimeMillis(),
                                createdAt = expCreatedAt,
                                updatedAt = expUpdatedAt
                            )
                        )
                    }
                }

                Result.success(
                    BackupData(
                        version = version,
                        appName = appName,
                        createdAt = createdAt,
                        createdAtFormatted = createdAtFormatted,
                        categories = parsedCategories,
                        budgets = parsedBudgets,
                        expenses = parsedExpenses
                    )
                )
            } catch (e: Exception) {
                Result.failure(IllegalArgumentException("File sao lưu không hợp lệ: ${e.localizedMessage ?: "Cấu trúc JSON sai"}", e))
            }
        }
    }
}
