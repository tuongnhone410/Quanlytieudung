package com.example.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.AppTheme
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldPrimary

private val AVAILABLE_ICONS = listOf(
    "🍚", "🥤", "🛵", "🏠", "🛒", "💳",
    "💊", "🎮", "📱", "💼", "👨‍👩‍👧", "📦",
    "🍜", "☕", "🚗", "✈️", "🎬", "📚",
    "🎁", "⚽", "💻", "👕", "💡", "🐾",
    "🎓", "🏥", "🏖️", "🍕", "🍔", "🎂",
    "💰", "🛠️", "⛽", "👶", "🎵", "🏷️"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryManagementViewModel = viewModel()
) {
    val categories by viewModel.categoriesWithCount.collectAsStateWithLifecycle()
    val addEditState by viewModel.addEditDialogState.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteDialogState.collectAsStateWithLifecycle()
    val infoMessage by viewModel.infoDialogMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quản lý danh mục",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_category_mgmt")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = AppTheme.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddCategoryDialog() },
                containerColor = EmeraldPrimary,
                contentColor = Color(0xFF0F1015),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_category")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm danh mục",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thêm danh mục",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppTheme.colors.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.surfaceElevated
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.emeraldGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AppTheme.colors.emeraldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${categories.size} danh mục hoạt động",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tùy chỉnh biểu tượng và tên danh mục theo nhu cầu quản lý của bạn.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = AppTheme.colors.textSecondary
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Category Items
            items(
                items = categories,
                key = { it.category.id }
            ) { item ->
                CategoryListItemCard(
                    item = item,
                    onEdit = { viewModel.openEditCategoryDialog(item.category) },
                    onDelete = { viewModel.requestDeleteCategory(item) }
                )
            }
        }
    }

    // Add / Edit Dialog
    if (addEditState.isOpen) {
        AddEditCategoryDialog(
            state = addEditState,
            onNameChange = { viewModel.updateDialogName(it) },
            onIconChange = { viewModel.updateDialogIcon(it) },
            onDismiss = { viewModel.dismissAddEditDialog() },
            onSave = { viewModel.saveCategory() }
        )
    }

    // Delete Confirmation Dialog
    if (deleteState.isOpen && deleteState.categoryToDelete != null) {
        DeleteCategoryConfirmationDialog(
            state = deleteState,
            onDismiss = { viewModel.dismissDeleteDialog() },
            onConfirm = { viewModel.confirmDeleteCategory() }
        )
    }

    // Info Alert Dialog (for fallback category)
    if (infoMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissInfoDialog() },
            title = {
                Text(
                    text = "Thông báo",
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )
            },
            text = {
                Text(
                    text = infoMessage ?: "",
                    color = AppTheme.colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissInfoDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(text = "Đã hiểu", color = Color(0xFF0F1015), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = AppTheme.colors.surfaceElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun CategoryListItemCard(
    item: CategoryItemUiState,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_card_${item.category.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceElevated
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon bubble
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceHighlight)
                    .border(1.dp, AppTheme.colors.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.category.icon,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Name & Count
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.category.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.textPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isDefaultFallback) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AppTheme.colors.surfaceHighlight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AppTheme.colors.textTertiary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Mặc định",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AppTheme.colors.textTertiary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (item.expenseCount > 0) "${item.expenseCount} giao dịch" else "Chưa có giao dịch",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (item.expenseCount > 0) AppTheme.colors.emeraldLight else AppTheme.colors.textTertiary
                    )
                )
            }

            // Edit button
            IconButton(
                onClick = onEdit,
                modifier = Modifier.testTag("btn_edit_category_${item.category.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Chỉnh sửa",
                    tint = AppTheme.colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("btn_delete_category_${item.category.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Xóa",
                    tint = if (item.isDefaultFallback) AppTheme.colors.textTertiary.copy(alpha = 0.4f) else CoralExpense,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AddEditCategoryDialog(
    state: AddEditCategoryDialogState,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val isEdit = state.categoryToEdit != null
    val title = if (isEdit) "Chỉnh sửa danh mục" else "Thêm danh mục mới"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Name TextField
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Tên danh mục") },
                    placeholder = { Text("Ví dụ: Học tập, Thú cưng...") },
                    singleLine = true,
                    isError = state.errorMessage != null,
                    supportingText = {
                        if (state.errorMessage != null) {
                            Text(
                                text = state.errorMessage,
                                color = CoralExpense
                            )
                        } else {
                            Text(
                                text = "${state.name.length}/30 ký tự",
                                color = AppTheme.colors.textTertiary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = AppTheme.colors.border,
                        focusedTextColor = AppTheme.colors.textPrimary,
                        unfocusedTextColor = AppTheme.colors.textPrimary,
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = AppTheme.colors.textSecondary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_category_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Chọn biểu tượng",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Emoji Picker Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppTheme.colors.surfaceHighlight)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(AVAILABLE_ICONS) { icon ->
                        val isSelected = state.icon == icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) EmeraldPrimary.copy(alpha = 0.25f) else Color.Transparent
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) EmeraldPrimary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onIconChange(icon) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = icon,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color(0xFF0F1015)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_save_category")
            ) {
                Text(
                    text = if (isEdit) "Lưu thay đổi" else "Thêm danh mục",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppTheme.colors.textSecondary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
            ) {
                Text(text = "Hủy")
            }
        },
        containerColor = AppTheme.colors.surfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun DeleteCategoryConfirmationDialog(
    state: DeleteCategoryDialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val category = state.categoryToDelete ?: return
    val hasExpenses = state.expenseCount > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = CoralExpense,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = if (hasExpenses) "Chuyển giao dịch và xóa?" else "Xác nhận xóa danh mục?",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                ),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (hasExpenses) {
                    Text(
                        text = "Danh mục \"${category.icon} ${category.name}\" đang có ${state.expenseCount} giao dịch.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AppTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Để đảm bảo an toàn và không mất dữ liệu, toàn bộ ${state.expenseCount} giao dịch này sẽ được chuyển sang danh mục \"📦 Khác\" trước khi danh mục bị xóa.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.textSecondary
                        ),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Bạn có chắc chắn muốn xóa danh mục \"${category.icon} ${category.name}\"?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AppTheme.colors.textPrimary
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralExpense,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_delete_category")
            ) {
                Text(
                    text = if (hasExpenses) "Chuyển & Xóa" else "Xóa",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppTheme.colors.textSecondary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
            ) {
                Text(text = "Hủy")
            }
        },
        containerColor = AppTheme.colors.surfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}
