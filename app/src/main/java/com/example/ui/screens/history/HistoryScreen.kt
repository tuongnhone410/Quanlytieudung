package com.example.ui.screens.history

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EmptyExpenseState
import com.example.ui.components.ExpenseCardItem
import com.example.ui.components.ExpenseDetailBottomSheet
import com.example.ui.theme.AppTheme
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToEditExpense: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val customSelectedDate by viewModel.customSelectedDate.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val filteredExpenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val filteredTotal by viewModel.filteredTotal.collectAsStateWithLifecycle()
    val dateGroups by viewModel.dateGroups.collectAsStateWithLifecycle()
    val selectedExpenseForDetail by viewModel.selectedExpenseForDetail.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showSortMenu by remember { mutableStateOf(false) }

    val selectedCategoryObj = allCategories.firstOrNull { it.id == selectedCategoryId }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen"),
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lịch sử chi tiêu",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                    )
                },
                actions = {
                    // Sort button with DropdownMenu
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("btn_sort_history")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sắp xếp",
                                tint = EmeraldLight
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier
                                .background(AppTheme.colors.surfaceElevated)
                                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(8.dp))
                        ) {
                            HistorySortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (selectedSort == option) EmeraldPrimary else AppTheme.colors.textPrimary,
                                                fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        showSortMenu = false
                                    },
                                    modifier = Modifier.testTag("sort_option_${option.name.lowercase()}")
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddExpense,
                modifier = Modifier.testTag("fab_add_expense_history"),
                shape = CircleShape,
                containerColor = EmeraldPrimary,
                contentColor = Color(0xFF0F1015)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm chi tiêu",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input"),
                    placeholder = {
                        Text(
                            text = "Tìm kiếm giao dịch, danh mục...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = AppTheme.colors.textTertiary)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = AppTheme.colors.textTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setSearchQuery("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Xóa tìm kiếm",
                                    tint = AppTheme.colors.textTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = AppTheme.colors.border,
                        focusedTextColor = AppTheme.colors.textPrimary,
                        unfocusedTextColor = AppTheme.colors.textPrimary,
                        focusedContainerColor = AppTheme.colors.surface,
                        unfocusedContainerColor = AppTheme.colors.surface
                    )
                )
            }

            // 2. Time Filter Chips Row (Hôm nay, Tuần này, Tháng này, Tháng trước, Tất cả, Chọn ngày)
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HistoryFilter.values().forEach { filter ->
                            val isSelected = selectedFilter == filter
                            val label = if (filter == HistoryFilter.CUSTOM_DATE && isSelected) {
                                DateUtils.formatDate(customSelectedDate)
                            } else {
                                filter.displayName
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else AppTheme.colors.surfaceElevated
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) EmeraldPrimary else AppTheme.colors.border,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (filter == HistoryFilter.CUSTOM_DATE) {
                                            val cal = Calendar.getInstance().apply { timeInMillis = customSelectedDate }
                                            DatePickerDialog(
                                                context,
                                                { _, year, month, dayOfMonth ->
                                                    val selCal = Calendar.getInstance().apply {
                                                        set(Calendar.YEAR, year)
                                                        set(Calendar.MONTH, month)
                                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                    }
                                                    viewModel.setCustomDate(selCal.timeInMillis)
                                                },
                                                cal.get(Calendar.YEAR),
                                                cal.get(Calendar.MONTH),
                                                cal.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        } else {
                                            viewModel.setFilter(filter)
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("filter_chip_${filter.name.lowercase()}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (filter == HistoryFilter.CUSTOM_DATE) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = if (isSelected) EmeraldLight else AppTheme.colors.textTertiary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) EmeraldLight else AppTheme.colors.textSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Category Filter Chips Row (Tất cả, Ăn uống, Cà phê, Xăng xe,...)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "Tất cả" chip
                    val isAllCategorySelected = selectedCategoryId == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isAllCategorySelected) EmeraldPrimary.copy(alpha = 0.2f) else AppTheme.colors.surface)
                            .border(
                                width = if (isAllCategorySelected) 1.5.dp else 1.dp,
                                color = if (isAllCategorySelected) EmeraldPrimary else AppTheme.colors.border,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setCategory(null) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("category_filter_all")
                    ) {
                        Text(
                            text = "Tất cả danh mục",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isAllCategorySelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAllCategorySelected) EmeraldLight else AppTheme.colors.textSecondary
                            )
                        )
                    }

                    // Individual Categories
                    allCategories.forEach { category ->
                        val isCatSelected = selectedCategoryId == category.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCatSelected) EmeraldPrimary.copy(alpha = 0.2f) else AppTheme.colors.surface)
                            .border(
                                width = if (isCatSelected) 1.5.dp else 1.dp,
                                color = if (isCatSelected) EmeraldPrimary else AppTheme.colors.border,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                if (isCatSelected) viewModel.setCategory(null) else viewModel.setCategory(category.id)
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("category_filter_${category.id}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = category.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCatSelected) EmeraldLight else AppTheme.colors.textSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Summary Card for Current Filtered Results
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_summary_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        AppTheme.colors.surfaceHighlight.copy(alpha = 0.5f),
                                        AppTheme.colors.surfaceElevated
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val headerLabel = buildString {
                                    if (selectedCategoryObj != null) {
                                        append("${selectedCategoryObj.icon} ${selectedCategoryObj.name}")
                                        append(" — ")
                                    }
                                    when (selectedFilter) {
                                        HistoryFilter.ALL -> append("Tất cả")
                                        HistoryFilter.TODAY -> append("Hôm nay")
                                        HistoryFilter.THIS_WEEK -> append("Tuần này")
                                        HistoryFilter.THIS_MONTH -> append("Tháng này")
                                        HistoryFilter.LAST_MONTH -> append("Tháng trước")
                                        HistoryFilter.CUSTOM_DATE -> append(DateUtils.formatDate(customSelectedDate))
                                    }
                                }

                                Text(
                                    text = headerLabel.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        color = AppTheme.colors.textSecondary
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppTheme.colors.surfaceHighlight)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${filteredExpenses.size} giao dịch",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppTheme.colors.textTertiary
                                        ),
                                        modifier = Modifier.testTag("history_transaction_count")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "Tổng tiền",
                                        style = MaterialTheme.typography.bodySmall.copy(color = AppTheme.colors.textTertiary)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = CurrencyFormatter.formatVnd(filteredTotal),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AppTheme.colors.textPrimary,
                                            fontSize = 26.sp
                                        ),
                                        modifier = Modifier.testTag("history_total_amount")
                                    )
                                }

                                // Active sort indicator
                                Text(
                                    text = selectedSort.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EmeraldLight,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 5. Transactions List or Empty Result State
            if (filteredExpenses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .testTag("empty_history_state"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = AppTheme.colors.surfaceElevated.copy(alpha = 0.6f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.surfaceHighlight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (searchQuery.isNotEmpty()) Icons.Default.Search else Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = AppTheme.colors.textSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = if (searchQuery.isNotEmpty()) "Không tìm thấy giao dịch" else "Không có giao dịch nào",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.textPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (searchQuery.isNotEmpty()) {
                                    "Không có khoản chi nào khớp với từ khóa \"$searchQuery\""
                                } else {
                                    "Chưa có giao dịch phù hợp với bộ lọc hiện tại."
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = AppTheme.colors.textSecondary
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            if (searchQuery.isNotEmpty() || selectedFilter != HistoryFilter.ALL || selectedCategoryId != null) {
                                Button(
                                    onClick = { viewModel.resetFilters() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppTheme.colors.surfaceHighlight,
                                        contentColor = AppTheme.colors.textPrimary
                                    )
                                ) {
                                    Text("Xóa bộ lọc", fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Button(
                                    onClick = onNavigateToAddExpense,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldPrimary,
                                        contentColor = Color(0xFF0F1015)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Thêm chi tiêu", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            } else {
                dateGroups.forEach { group ->
                    item(key = "header_${group.dateString}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.dateString,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textSecondary
                                )
                            )
                            Text(
                                text = "Tổng: ${CurrencyFormatter.formatVnd(group.totalAmount)}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.textTertiary
                                )
                            )
                        }
                    }

                    items(
                        items = group.items,
                        key = { it.expense.id }
                    ) { item ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            ExpenseCardItem(
                                item = item,
                                onClick = { viewModel.onExpenseClicked(item) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Transaction Detail Sheet
    if (selectedExpenseForDetail != null) {
        ExpenseDetailBottomSheet(
            item = selectedExpenseForDetail!!,
            onDismiss = { viewModel.dismissExpenseDetail() },
            onEdit = { expenseId ->
                onNavigateToEditExpense(expenseId)
            },
            onDelete = { expense ->
                viewModel.deleteExpense(expense)
            }
        )
    }
}
