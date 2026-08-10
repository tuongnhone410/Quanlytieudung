package com.example.ui.screens.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BackupData
import com.example.data.model.Category
import com.example.data.repository.ExpenseRepository
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ThemePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository = ExpenseRepository.getInstance(
        AppDatabase.getInstance(application)
    )
    private val themePreferences = ThemePreferences.getInstance(application)

    val currentThemeMode: StateFlow<AppThemeMode> = themePreferences.themeMode

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expenseCount: StateFlow<Int> = repository.expenseCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingMessage = MutableStateFlow("")
    val processingMessage: StateFlow<String> = _processingMessage.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    fun setThemeMode(mode: AppThemeMode) {
        themePreferences.setThemeMode(mode)
    }

    suspend fun getBackupJson(): String {
        return repository.generateBackupJson()
    }

    fun exportBackupToFile(uri: Uri) {
        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "Đang tạo bản sao lưu dữ liệu..."
            try {
                val json = repository.generateBackupJson()
                withContext(Dispatchers.IO) {
                    val context = getApplication<Application>()
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(json.toByteArray(Charsets.UTF_8))
                        outputStream.flush()
                    } ?: throw IllegalStateException("Không thể ghi file vào vị trí đã chọn.")
                }
                _snackbarMessage.emit("Sao lưu dữ liệu thành công!")
            } catch (e: Exception) {
                _snackbarMessage.emit("Lỗi sao lưu: ${e.localizedMessage ?: "Không thể lưu tệp"}")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun importBackupFromFile(uri: Uri) {
        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "Đang kiểm tra và khôi phục dữ liệu..."
            try {
                val jsonContent = withContext(Dispatchers.IO) {
                    val context = getApplication<Application>()
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).readText()
                    } ?: throw IllegalStateException("Không thể mở tệp đã chọn.")
                }

                val parseResult = BackupData.fromJsonString(jsonContent)
                if (parseResult.isFailure) {
                    val errorMsg = parseResult.exceptionOrNull()?.message ?: "File sao lưu không hợp lệ."
                    _snackbarMessage.emit("Khôi phục thất bại: $errorMsg")
                    return@launch
                }

                val backupData = parseResult.getOrThrow()
                val restoreResult = repository.restoreBackup(backupData)

                if (restoreResult.isSuccess) {
                    _snackbarMessage.emit(
                        "Khôi phục thành công ${backupData.expenses.size} giao dịch và ${backupData.categories.size} danh mục!"
                    )
                } else {
                    val errorMsg = restoreResult.exceptionOrNull()?.message ?: "Không thể ghi dữ liệu vào cơ sở dữ liệu."
                    _snackbarMessage.emit("Khôi phục thất bại: $errorMsg")
                }
            } catch (e: Exception) {
                _snackbarMessage.emit("Lỗi khi đọc file: ${e.localizedMessage ?: "File không đúng định dạng"}")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "Đang xóa toàn bộ dữ liệu..."
            try {
                repository.clearAllData()
                _snackbarMessage.emit("Đã xóa toàn bộ dữ liệu và đặt lại danh mục mặc định.")
            } catch (e: Exception) {
                _snackbarMessage.emit("Lỗi khi xóa dữ liệu: ${e.localizedMessage}")
            } finally {
                _isProcessing.value = false
            }
        }
    }
}
