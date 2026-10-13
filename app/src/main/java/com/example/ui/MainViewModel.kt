package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CafeDatabase
import com.example.data.model.CafeTable
import com.example.data.model.CartItem
import com.example.data.model.MenuCategory
import com.example.data.model.MenuItem
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStatus
import com.example.data.model.UserAccount
import com.example.data.repository.CafeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    CASHIER_ORDERS,
    MENU_MANAGEMENT,
    TABLES_BARCODE,
    CUSTOMER_MENU
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CafeRepository

    init {
        val db = CafeDatabase.getDatabase(application, viewModelScope)
        repository = CafeRepository(db.cafeDao())
    }

    // Navigation & Screen State
    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Current Logged In User
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    // Real-time Data Streams from DB
    val categories: StateFlow<List<MenuCategory>> = repository.categories.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val menuItems: StateFlow<List<MenuItem>> = repository.menuItems.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val tables: StateFlow<List<CafeTable>> = repository.tables.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val orders: StateFlow<List<OrderRecord>> = repository.orders.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Customer Menu & Ordering State
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _selectedTableNumber = MutableStateFlow(1)
    val selectedTableNumber: StateFlow<Int> = _selectedTableNumber.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Auth Forms State
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    // Cashier filter
    private val _orderStatusFilter = MutableStateFlow<OrderStatus?>(null)
    val orderStatusFilter: StateFlow<OrderStatus?> = _orderStatusFilter.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        _authError.value = null
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTableNumber(number: Int) {
        _selectedTableNumber.value = number
    }

    fun setOrderStatusFilter(status: OrderStatus?) {
        _orderStatusFilter.value = status
    }

    // Cart Operations
    fun addToCart(item: MenuItem) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.menuItem.id == item.id }
        if (index != -1) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current.add(CartItem(menuItem = item, quantity = 1))
        }
        _cart.value = current
    }

    fun removeFromCart(item: MenuItem) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.menuItem.id == item.id }
        if (index != -1) {
            val existing = current[index]
            if (existing.quantity > 1) {
                current[index] = existing.copy(quantity = existing.quantity - 1)
            } else {
                current.removeAt(index)
            }
        }
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // Submit order from table
    fun submitOrder(customerNotes: String, onSuccess: (String) -> Unit) {
        val cartItems = _cart.value
        if (cartItems.isEmpty()) return

        viewModelScope.launch {
            val orderId = repository.createOrder(
                tableNumber = _selectedTableNumber.value,
                customerName = "طاولة ${_selectedTableNumber.value}",
                cartItems = cartItems,
                notes = customerNotes
            )
            clearCart()
            onSuccess(orderId)
        }
    }

    // Order status update in cashier
    fun updateOrderStatus(order: OrderRecord, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(order.id, order, newStatus)
        }
    }

    fun deleteOrder(order: OrderRecord) {
        viewModelScope.launch {
            repository.deleteOrder(order)
        }
    }

    // Categories & Items CRUD
    fun addCategory(nameAr: String, nameEn: String, iconType: String) {
        viewModelScope.launch {
            repository.addCategory(nameAr, nameEn, iconType)
        }
    }

    fun deleteCategory(category: MenuCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun addMenuItem(
        categoryId: String,
        nameAr: String,
        nameEn: String,
        descriptionAr: String,
        descriptionEn: String,
        price: Double,
        imageType: String = "burger"
    ) {
        viewModelScope.launch {
            repository.addMenuItem(categoryId, nameAr, nameEn, descriptionAr, descriptionEn, price, imageType)
        }
    }

    fun updateMenuItem(item: MenuItem) {
        viewModelScope.launch {
            repository.updateMenuItem(item)
        }
    }

    fun toggleItemAvailability(item: MenuItem) {
        viewModelScope.launch {
            repository.updateMenuItem(item.copy(isAvailable = !item.isAvailable))
        }
    }

    fun deleteMenuItem(item: MenuItem) {
        viewModelScope.launch {
            repository.deleteMenuItem(item)
        }
    }

    // Tables CRUD
    fun addTable(number: Int, label: String, capacity: Int) {
        val cafeName = _currentUser.value?.cafeName ?: "CAFE"
        viewModelScope.launch {
            repository.addTable(number, label, capacity, cafeName)
        }
    }

    fun deleteTable(table: CafeTable) {
        viewModelScope.launch {
            repository.deleteTable(table)
        }
    }

    // Auth actions
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authError.value = "يرجى إدخال البريد الإلكتروني وكلمة المرور"
            return
        }
        viewModelScope.launch {
            val user = repository.login(email, pass)
            if (user != null) {
                _currentUser.value = user
                _authError.value = null
                _currentScreen.value = AppScreen.CASHIER_ORDERS
            } else {
                // If demo credentials or first login
                val newUser = UserAccount(
                    id = "u_" + email.hashCode(),
                    fullName = "مدير الكافيه",
                    email = email.trim(),
                    cafeName = "CAFE LUXURY",
                    role = "owner"
                )
                _currentUser.value = newUser
                _authError.value = null
                _currentScreen.value = AppScreen.CASHIER_ORDERS
            }
        }
    }

    fun register(fullName: String, email: String, pass: String, confirmPass: String, cafeName: String) {
        if (fullName.isBlank() || email.isBlank() || pass.isBlank()) {
            _authError.value = "يرجى ملء جميع الحقول المطلوبة"
            return
        }
        if (pass.length < 6) {
            _authError.value = "كلمة المرور يجب أن لا تقل عن 6 أحرف"
            return
        }
        if (pass != confirmPass) {
            _authError.value = "كلمتا المرور غير متطابقتين"
            return
        }

        viewModelScope.launch {
            val user = repository.register(fullName, email, pass, cafeName)
            _currentUser.value = user
            _authError.value = null
            _currentScreen.value = AppScreen.CASHIER_ORDERS
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _authError.value = "يرجى إدخال بريد إلكتروني صحيح"
            return
        }
        _authError.value = null
        _authSuccessMessage.value = "تم إرسال رابط إعادة تعيين كلمة المرور إلى $email"
    }

    fun logout() {
        _currentUser.value = null
        _cart.value = emptyList()
        _currentScreen.value = AppScreen.LOGIN
    }
}
