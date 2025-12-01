package com.example.projectpp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpp.data.AppDatabase
import com.example.projectpp.data.FoodItem
import com.example.projectpp.data.UserPreferencesRepository
import com.example.projectpp.util.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlinx.coroutines.delay

enum class ExpirationStatus(val title: String) {
    EXPIRED("Kadaluwarsa"),
    EXPIRING_SOON("Akan Kadaluwarsa"),
    SAFE("Aman")
}
data class FoodItemUi(
    val item: FoodItem,
    val daysLeft: Int,
    val status: ExpirationStatus
)
sealed interface FoodUiState {
    object Loading : FoodUiState
    data class Success(val groupedFoods: Map<ExpirationStatus, List<FoodItemUi>>) : FoodUiState
    object Empty : FoodUiState
}

class FoodViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getDatabase(app).foodDao()
    private val profileRepository = UserPreferencesRepository(app)

    val userName: StateFlow<String> = profileRepository.userName
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Memuat...")
    val userDescription: StateFlow<String> = profileRepository.userDescription
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Memuat...")

    // BARU: Baca mode test
    val isTestMode: StateFlow<Boolean> = profileRepository.isTestMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun saveProfile(name: String, description: String) = viewModelScope.launch {
        profileRepository.saveProfile(name, description)
    }

    fun setTestMode(isTest: Boolean) = viewModelScope.launch {
        profileRepository.saveTestMode(isTest)
    }

    private val allItemsFlow = dao.getAllFoodFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val ticker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000L)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, System.currentTimeMillis())

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    val foods: StateFlow<FoodUiState> = combine(
        allItemsFlow,
        ticker,
        _selectedCategory
    ) { list, time, categoryFilter ->
        if (list == null) return@combine FoodUiState.Loading

        val visibleItems = list.filter { it.quantity > 0 }
        val filteredItems = if (categoryFilter == null) {
            visibleItems
        } else {
            visibleItems.filter { it.category == categoryFilter }
        }

        if (filteredItems.isEmpty()) {
            FoodUiState.Empty
        } else {
            val uiItems = filteredItems.map { item ->
                val daysLeft = max(
                    ((item.expirationDate - time) / (1000 * 60 * 60 * 24)).toInt(),
                    -999
                )
                val status = when {
                    daysLeft <= 0 -> ExpirationStatus.EXPIRED
                    daysLeft <= 3 -> ExpirationStatus.EXPIRING_SOON
                    else -> ExpirationStatus.SAFE
                }
                FoodItemUi(item, daysLeft, status)
            }
            val groupedData = uiItems.groupBy { it.status }
            FoodUiState.Success(groupedData)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = FoodUiState.Loading
    )

    val categories: StateFlow<Map<String, Int>> = allItemsFlow
        .map { list ->
            if (list == null) return@map emptyMap()
            val counts = list.filter { it.quantity > 0 }.groupBy { it.category }.mapValues { it.value.size }
            val allCategoryNames = list.map { it.category }.distinct()
            val categoryMap = mutableMapOf<String, Int>()
            for (name in allCategoryNames) {
                categoryMap[name] = counts[name] ?: 0
            }
            categoryMap.toSortedMap()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            emptyMap()
        )

    // PERBAIKAN: Logika Insert dengan Test Mode
    fun insertAndSchedule(food: FoodItem) = viewModelScope.launch {
        val newId = dao.insert(food)
        if (food.quantity > 0) {
            val finalFood = food.copy(id = newId)
            val isTest = isTestMode.value // Ambil nilai saat ini

            if (isTest) {
                // Mode Test: Jadwalkan 5 detik dari sekarang
                val triggerTime = System.currentTimeMillis() + 5_000L
                NotificationHelper.scheduleNotificationTest(getApplication(), finalFood, triggerTime)
            } else {
                // Mode Normal
                NotificationHelper.scheduleNotificationUnique(getApplication(), finalFood)
            }
        }
    }

    fun deleteAndCancel(food: FoodItem) = viewModelScope.launch {
        NotificationHelper.cancelNotification(getApplication(), food.id)
        dao.delete(food)
    }

    // Logika Update dengan Test Mode
    fun updateAndReschedule(food: FoodItem) = viewModelScope.launch {
        NotificationHelper.cancelNotification(getApplication(), food.id)
        dao.update(food)

        val isTest = isTestMode.value
        if (isTest) {
            val triggerTime = System.currentTimeMillis() + 5_000L
            NotificationHelper.scheduleNotificationTest(getApplication(), food, triggerTime)
        } else {
            NotificationHelper.scheduleNotificationUnique(getApplication(), food)
        }
    }

    fun renameCategory(oldName: String, newName: String) = viewModelScope.launch {
        if (oldName.equals(newName, ignoreCase = true) || newName.isBlank()) return@launch
        dao.renameCategory(oldName, newName)
    }

    fun deleteCategory(categoryName: String) = viewModelScope.launch {
        val itemsToDelete = dao.getAllNow().filter { it.category == categoryName }
        itemsToDelete.forEach {
            if(it.quantity > 0) {
                NotificationHelper.cancelNotification(getApplication(), it.id)
            }
        }
        dao.deleteByCategory(categoryName)
    }

    fun clearAll() = viewModelScope.launch {
        val itemsToDelete = dao.getAllRealProductsNow()
        itemsToDelete.forEach {
            NotificationHelper.cancelNotification(getApplication(), it.id)
        }
        dao.clearAllProducts()
    }
}