package com.example.ui.developer

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.InvoiceSecurity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeveloperViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DeveloperRepository()
    private val prefs = application.getSharedPreferences("developer_session_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("dev_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentScreen = MutableStateFlow(if (_isLoggedIn.value) DeveloperScreen.DASHBOARD else DeveloperScreen.LOGIN)
    val currentScreen: StateFlow<DeveloperScreen> = _currentScreen.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allCafeterias = MutableStateFlow<List<CafeteriaAdminSummary>>(emptyList())
    val allCafeterias: StateFlow<List<CafeteriaAdminSummary>> = _allCafeterias.asStateFlow()

    val filteredCafeterias: StateFlow<List<CafeteriaAdminSummary>> = combine(_allCafeterias, _searchQuery) { list, query ->
        val q = query.trim().lowercase()
        if (q.isBlank()) {
            list
        } else {
            val cleanQ = q.removePrefix("#")
            list.filter { cafe ->
                cafe.numericId.contains(cleanQ) ||
                        cafe.cafeName.lowercase().contains(q) ||
                        cafe.ownerName.lowercase().contains(q) ||
                        cafe.phone.contains(cleanQ) ||
                        cafe.email.lowercase().contains(q) ||
                        cafe.id.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Summary calculations across all cafeterias
    val totalSystemSales: StateFlow<Double> = _allCafeterias.combine(_allCafeterias) { list, _ ->
        list.sumOf { it.totalSales }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalServiceFees: StateFlow<Double> = _allCafeterias.combine(_allCafeterias) { list, _ ->
        list.sumOf { it.serviceFeeTotal }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalCollectedFees: StateFlow<Double> = _allCafeterias.combine(_allCafeterias) { list, _ ->
        list.sumOf { it.collectedFees }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalRemainingFees: StateFlow<Double> = _allCafeterias.combine(_allCafeterias) { list, _ ->
        list.sumOf { it.remainingFee }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Invoices Screen state
    private val _selectedCafe = MutableStateFlow<CafeteriaAdminSummary?>(null)
    val selectedCafe: StateFlow<CafeteriaAdminSummary?> = _selectedCafe.asStateFlow()

    private val _selectedCafeInvoices = MutableStateFlow<List<AdminInvoiceItem>>(emptyList())
    val selectedCafeInvoices: StateFlow<List<AdminInvoiceItem>> = _selectedCafeInvoices.asStateFlow()

    private val _invoicesLoading = MutableStateFlow(false)
    val invoicesLoading: StateFlow<Boolean> = _invoicesLoading.asStateFlow()

    // Dialogs state
    private val _cafeForCollection = MutableStateFlow<CafeteriaAdminSummary?>(null)
    val cafeForCollection: StateFlow<CafeteriaAdminSummary?> = _cafeForCollection.asStateFlow()

    private val _cafeForHistory = MutableStateFlow<CafeteriaAdminSummary?>(null)
    val cafeForHistory: StateFlow<CafeteriaAdminSummary?> = _cafeForHistory.asStateFlow()

    private val _historyRecords = MutableStateFlow<List<AdminFeeCollectionRecord>>(emptyList())
    val historyRecords: StateFlow<List<AdminFeeCollectionRecord>> = _historyRecords.asStateFlow()

    private val _historyLoading = MutableStateFlow(false)
    val historyLoading: StateFlow<Boolean> = _historyLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        if (_isLoggedIn.value) {
            refreshDashboard()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun login(emailOrPin: String, pass: String): Boolean {
        val cleanInput = emailOrPin.trim()
        val cleanPass = pass.trim()

        // Developer Master Access credentials
        val isValid = cleanInput == InvoiceSecurity.MASTER_OVERRIDE_PIN ||
                cleanPass == InvoiceSecurity.MASTER_OVERRIDE_PIN ||
                (cleanInput.equals("alikhlel132@gmail.com", ignoreCase = true) && cleanPass.length >= 6) ||
                (cleanInput.contains("@") && cleanPass == InvoiceSecurity.MASTER_OVERRIDE_PIN)

        if (isValid) {
            prefs.edit().putBoolean("dev_logged_in", true).apply()
            _isLoggedIn.value = true
            _currentScreen.value = DeveloperScreen.DASHBOARD
            refreshDashboard()
            return true
        } else {
            _errorMessage.value = "رمز الدخول أو بيانات المطور غير صحيحة"
            return false
        }
    }

    fun logout() {
        prefs.edit().putBoolean("dev_logged_in", false).apply()
        _isLoggedIn.value = false
        _currentScreen.value = DeveloperScreen.LOGIN
    }

    fun navigateTo(screen: DeveloperScreen) {
        _currentScreen.value = screen
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.fetchAllCafeterias()
                _allCafeterias.value = list
            } catch (e: Exception) {
                _errorMessage.value = "تعذر تحميل بيانات الكافتيريات: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun openInvoices(cafe: CafeteriaAdminSummary) {
        _selectedCafe.value = cafe
        _currentScreen.value = DeveloperScreen.INVOICES
        loadCafeInvoices(cafe.id)
    }

    fun loadCafeInvoices(cafeId: String) {
        viewModelScope.launch {
            _invoicesLoading.value = true
            try {
                val invoices = repository.fetchCafeInvoices(cafeId)
                _selectedCafeInvoices.value = invoices
            } catch (e: Exception) {
                _errorMessage.value = "تعذر تحميل فواتير الكافتيريا: ${e.localizedMessage}"
            } finally {
                _invoicesLoading.value = false
            }
        }
    }

    fun deleteSingleInvoice(cafeId: String, orderId: String) {
        viewModelScope.launch {
            val result = repository.deleteInvoice(cafeId, orderId)
            if (result.isSuccess) {
                _selectedCafeInvoices.value = _selectedCafeInvoices.value.filterNot { it.id == orderId }
                _statusMessage.value = "تم حذف الفاتورة بنجاح"
                refreshDashboard()
            } else {
                _errorMessage.value = "تعذر حذف الفاتورة: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun deleteAllInvoicesForSelectedCafe(onCompleted: () -> Unit) {
        val cafe = _selectedCafe.value ?: return
        viewModelScope.launch {
            _invoicesLoading.value = true
            val result = repository.deleteAllInvoicesForCafe(cafe.id)
            _invoicesLoading.value = false
            if (result.isSuccess) {
                val deleted = result.getOrDefault(0)
                _selectedCafeInvoices.value = emptyList()
                _statusMessage.value = "تم حذف كافة الفواتير بنجاح ($deleted فاتورة)"
                refreshDashboard()
                onCompleted()
            } else {
                _errorMessage.value = "تعذر تصفير الفواتير: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun toggleCafeStatus(cafe: CafeteriaAdminSummary) {
        viewModelScope.launch {
            val res = repository.toggleCafeStatus(cafe.id, cafe.isActive)
            if (res.isSuccess) {
                val newActive = res.getOrDefault(!cafe.isActive)
                _allCafeterias.value = _allCafeterias.value.map {
                    if (it.id == cafe.id) it.copy(isActive = newActive) else it
                }
                _statusMessage.value = if (newActive) "تم تفعيل الكافتيريا بنجاح" else "تم إيقاف الكافتيريا بنجاح"
            } else {
                _errorMessage.value = "تعذر تعديل حالة الكافتيريا: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun openCollectDialog(cafe: CafeteriaAdminSummary) {
        _cafeForCollection.value = cafe
    }

    fun closeCollectDialog() {
        _cafeForCollection.value = null
    }

    fun confirmFeeCollection(cafe: CafeteriaAdminSummary, amount: Double, note: String) {
        if (amount <= 0) {
            _errorMessage.value = "يرجى إدخال مبلغ صحيح"
            return
        }
        viewModelScope.launch {
            val res = repository.recordFeeCollection(cafe.id, amount, note)
            if (res.isSuccess) {
                _cafeForCollection.value = null
                _statusMessage.value = "تم تسجيل استلام $amount د.ع بنجاح"
                refreshDashboard()
            } else {
                _errorMessage.value = "تعذر تسجيل الاستلام: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun openHistoryDialog(cafe: CafeteriaAdminSummary) {
        _cafeForHistory.value = cafe
        _historyLoading.value = true
        viewModelScope.launch {
            val history = repository.fetchFeeCollections(cafe.id)
            _historyRecords.value = history
            _historyLoading.value = false
        }
    }

    fun closeHistoryDialog() {
        _cafeForHistory.value = null
        _historyRecords.value = emptyList()
    }

    fun clearMessages() {
        _statusMessage.value = null
        _errorMessage.value = null
    }
}
