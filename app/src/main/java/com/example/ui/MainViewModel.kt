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
import com.example.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    CASHIER_ORDERS,
    MENU_MANAGEMENT,
    TABLES_BARCODE,
    CUSTOMER_MENU,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CafeRepository

    init {
        val db = CafeDatabase.getDatabase(application, viewModelScope)
        repository = CafeRepository(db.cafeDao(), application)
        viewModelScope.launch {
            try {
                val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (authUser != null && authUser.isEmailVerified) {
                    val uid = authUser.uid
                    val localUser = db.cafeDao().getUserByEmail(authUser.email ?: "")
                    val baseAccount = if (localUser != null) {
                        UserAccount(
                            id = uid,
                            fullName = localUser.fullName,
                            email = localUser.email,
                            cafeName = localUser.cafeName,
                            role = localUser.role
                        )
                    } else {
                        UserAccount(
                            id = uid,
                            fullName = authUser.displayName ?: "مدير الكافيه",
                            email = authUser.email ?: "",
                            cafeName = "Q-Cafe",
                            role = "owner"
                        )
                    }

                    // 1. Load persistent profile from SharedPreferences
                    val cachedProfile = repository.loadCafeProfile(uid)
                    val effectiveCafeName = cachedProfile["cafeName"] ?: baseAccount.cafeName
                    val effectiveOwnerName = cachedProfile["ownerName"] ?: baseAccount.fullName
                    val effectiveEmail = cachedProfile["email"] ?: baseAccount.email

                    val userAccount = baseAccount.copy(
                        cafeName = effectiveCafeName,
                        fullName = effectiveOwnerName,
                        email = effectiveEmail
                    )

                    _currentUser.value = userAccount
                    _cafeName.value = effectiveCafeName
                    _cafeOwnerName.value = effectiveOwnerName
                    _cafeEmail.value = effectiveEmail
                    cachedProfile["phone"]?.let { _cafePhone.value = it }
                    cachedProfile["address"]?.let { _cafeAddress.value = it }
                    cachedProfile["welcomeNote"]?.let { _cafeWelcomeNote.value = it }
                    cachedProfile["logoType"]?.let { _cafeLogoType.value = it }
                    cachedProfile["customLogoUri"]?.let { _cafeCustomLogoUri.value = it.ifBlank { null } }
                    cachedProfile["menuUrl"]?.let {
                        if (it.isNotBlank()) {
                            _webMenuUrl.value = it
                            repository.baseWebMenuUrl = it
                        }
                    }

                    repository.currentCafeteriaId = uid
                    repository.updateExistingTablesCafeteriaId()
                    repository.startFirestoreOrdersListener(viewModelScope, uid)
                    repository.loadMenuForCafeteria(uid)

                    // 2. Refresh profile from Firestore in background
                    launch(Dispatchers.IO) {
                        repository.fetchAndSyncCafeProfileFromFirestore(uid)?.let { remote ->
                            remote["cafeName"]?.let { if (it.isNotBlank()) _cafeName.value = it }
                            remote["phone"]?.let { if (it.isNotBlank()) _cafePhone.value = it }
                            remote["ownerName"]?.let { if (it.isNotBlank()) _cafeOwnerName.value = it }
                            remote["email"]?.let { if (it.isNotBlank()) _cafeEmail.value = it }
                            remote["address"]?.let { if (it.isNotBlank()) _cafeAddress.value = it }
                            remote["welcomeNote"]?.let { if (it.isNotBlank()) _cafeWelcomeNote.value = it }
                            remote["logoType"]?.let { if (it.isNotBlank()) _cafeLogoType.value = it }
                            remote["customLogoUri"]?.let { _cafeCustomLogoUri.value = it.ifBlank { null } }
                            remote["menuUrl"]?.let {
                                if (it.isNotBlank()) {
                                    _webMenuUrl.value = it
                                    repository.baseWebMenuUrl = it
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {}

            delay(1200)
            if (_currentScreen.value == AppScreen.SPLASH) {
                _currentScreen.value = if (_currentUser.value != null) AppScreen.CASHIER_ORDERS else AppScreen.LOGIN
            }
        }
    }

    // Navigation & Screen State
    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
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

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _unverifiedEmail = MutableStateFlow<String?>(null)
    val unverifiedEmail: StateFlow<String?> = _unverifiedEmail.asStateFlow()

    private val _registrationSuccessMessage = MutableStateFlow<String?>(null)
    val registrationSuccessMessage: StateFlow<String?> = _registrationSuccessMessage.asStateFlow()

    // Cashier filter (default to NEW stage, "الكل" option removed)
    private val _orderStatusFilter = MutableStateFlow<OrderStatus>(OrderStatus.NEW)
    val orderStatusFilter: StateFlow<OrderStatus> = _orderStatusFilter.asStateFlow()

    // Cafe Profile & Branding Settings
    private val _cafeName = MutableStateFlow("Q-Cafe")
    val cafeName: StateFlow<String> = _cafeName.asStateFlow()

    private val _cafeOwnerName = MutableStateFlow("صاحب الكافتيريا")
    val cafeOwnerName: StateFlow<String> = _cafeOwnerName.asStateFlow()

    private val _cafePhone = MutableStateFlow("+964 780 000 0000")
    val cafePhone: StateFlow<String> = _cafePhone.asStateFlow()

    private val _cafeEmail = MutableStateFlow("cafe.owner@gmail.com")
    val cafeEmail: StateFlow<String> = _cafeEmail.asStateFlow()

    private val _cafeAddress = MutableStateFlow("الفرع الرئيسي - صالة العوائل والشباب")
    val cafeAddress: StateFlow<String> = _cafeAddress.asStateFlow()

    private val _cafeWelcomeNote = MutableStateFlow("أهلاً وسهلاً بكم في كافتيريتنا، نتمنى لكم أطيب الأوقات!")
    val cafeWelcomeNote: StateFlow<String> = _cafeWelcomeNote.asStateFlow()

    private val _cafeLogoType = MutableStateFlow("custom")
    val cafeLogoType: StateFlow<String> = _cafeLogoType.asStateFlow()

    private val _cafeCustomLogoUri = MutableStateFlow<String?>(null)
    val cafeCustomLogoUri: StateFlow<String?> = _cafeCustomLogoUri.asStateFlow()

    private val _webMenuUrl = MutableStateFlow(repository.baseWebMenuUrl)
    val webMenuUrl: StateFlow<String> = _webMenuUrl.asStateFlow()

    fun setCustomLogoUri(uri: String?) {
        _cafeCustomLogoUri.value = uri
    }

    fun updateCafeProfile(
        name: String,
        phone: String,
        address: String = _cafeAddress.value,
        welcomeNote: String = _cafeWelcomeNote.value,
        logoType: String = _cafeLogoType.value,
        menuUrl: String = _webMenuUrl.value,
        ownerName: String = _cafeOwnerName.value,
        email: String = _cafeEmail.value,
        customLogoUri: String? = _cafeCustomLogoUri.value
    ) {
        _cafeName.value = name.ifBlank { "Q-Cafe" }
        _cafePhone.value = phone
        _cafeOwnerName.value = ownerName.ifBlank { "صاحب الكافتيريا" }
        _cafeEmail.value = email.ifBlank { "cafe.owner@gmail.com" }
        _cafeCustomLogoUri.value = customLogoUri
        _cafeAddress.value = address
        _cafeWelcomeNote.value = welcomeNote
        _cafeLogoType.value = logoType
        _webMenuUrl.value = menuUrl.ifBlank { repository.baseWebMenuUrl }
        repository.baseWebMenuUrl = _webMenuUrl.value

        // Also update currentUser if available
        _currentUser.value?.let { current ->
            _currentUser.value = current.copy(
                cafeName = _cafeName.value,
                fullName = _cafeOwnerName.value,
                email = _cafeEmail.value
            )
        }

        // Persist profile settings permanently in SharedPreferences & Firestore
        val cafeId = _currentUser.value?.id ?: repository.currentCafeteriaId
        repository.saveCafeProfile(
            cafeId = cafeId,
            cafeName = _cafeName.value,
            phone = _cafePhone.value,
            ownerName = _cafeOwnerName.value,
            email = _cafeEmail.value,
            address = _cafeAddress.value,
            welcomeNote = _cafeWelcomeNote.value,
            logoType = _cafeLogoType.value,
            customLogoUri = _cafeCustomLogoUri.value,
            menuUrl = _webMenuUrl.value,
            themeColor = _themeAccentColorHex.value
        )
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        _authError.value = null
        _authSuccessMessage.value = null
        _unverifiedEmail.value = null
        _registrationSuccessMessage.value = null
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

    fun setOrderStatusFilter(status: OrderStatus) {
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

    // Submit order from table with Customer Name
    fun submitOrder(customerName: String, customerNotes: String, onSuccess: (String) -> Unit) {
        val cartItems = _cart.value
        if (cartItems.isEmpty()) return

        viewModelScope.launch {
            val finalCustomerName = customerName.trim().ifBlank { "زبون طاولة ${_selectedTableNumber.value}" }
            val orderId = repository.createOrder(
                tableNumber = _selectedTableNumber.value,
                customerName = finalCustomerName,
                cartItems = cartItems,
                notes = customerNotes
            )
            clearCart()
            onSuccess(orderId)
        }
    }

    // Design & Theme Customization State
    private val _themeAccentColorHex = MutableStateFlow("#00DFC0")
    val themeAccentColorHex: StateFlow<String> = _themeAccentColorHex.asStateFlow()

    private val _cardCornerRadiusDp = MutableStateFlow(20)
    val cardCornerRadiusDp: StateFlow<Int> = _cardCornerRadiusDp.asStateFlow()

    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    fun updateThemeAccentColor(hex: String) {
        _themeAccentColorHex.value = hex
    }

    fun updateCardCornerRadius(dp: Int) {
        _cardCornerRadiusDp.value = dp
    }

    fun updateFontScale(scale: Float) {
        _fontScale.value = scale
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

    // Customer Menu Page Tab: 0 = Menu Orders, 1 = Offers & New Items, 2 = Games & Entertainment
    private val _customerMenuTab = MutableStateFlow(0)
    val customerMenuTab: StateFlow<Int> = _customerMenuTab.asStateFlow()

    fun setCustomerMenuTab(tab: Int) {
        _customerMenuTab.value = tab
    }

    // Categories & Items CRUD
    fun addCategory(nameAr: String, nameEn: String, iconType: String, customImageUri: String? = null) {
        viewModelScope.launch {
            repository.addCategory(nameAr, nameEn, iconType, customImageUri)
        }
    }

    fun updateCategory(category: MenuCategory) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: MenuCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            if (_selectedCategoryId.value == category.id) {
                _selectedCategoryId.value = null
            }
        }
    }

    fun addMenuItem(
        categoryId: String,
        nameAr: String,
        nameEn: String,
        descriptionAr: String,
        descriptionEn: String,
        price: Double,
        currency: String = "د.ع",
        imageType: String = "burger",
        imageUri: String? = null,
        bgImageUri: String? = null,
        galleryImages: List<String> = emptyList(),
        isOffer: Boolean = false,
        offerDiscountPercent: Int = 0,
        offerTag: String = "عرض خاص"
    ) {
        viewModelScope.launch {
            repository.addMenuItem(
                categoryId = categoryId,
                nameAr = nameAr,
                nameEn = nameEn,
                descriptionAr = descriptionAr,
                descriptionEn = descriptionEn,
                price = price,
                currency = currency,
                imageType = imageType,
                imageUri = imageUri,
                bgImageUri = bgImageUri,
                galleryImages = galleryImages,
                isOffer = isOffer,
                offerDiscountPercent = offerDiscountPercent,
                offerTag = offerTag
            )
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

    // Tables CRUD & Anti-fraud Barcode Regeneration
    fun addTable(number: Int, label: String, capacity: Int) {
        val cafeName = _currentUser.value?.cafeName ?: "CAFE"
        viewModelScope.launch {
            repository.addTable(number, label, capacity, cafeName)
        }
    }

    fun regenerateTableQr(table: CafeTable) {
        viewModelScope.launch {
            repository.regenerateTableQr(table)
        }
    }

    fun regenerateAllTablesQr() {
        viewModelScope.launch {
            repository.regenerateAllTablesQr(tables.value)
        }
    }

    fun generateDefaultTables(count: Int = 8) {
        viewModelScope.launch {
            repository.generateInitialTables(count)
        }
    }

    fun deleteTable(table: CafeTable) {
        viewModelScope.launch {
            repository.deleteTable(table)
        }
    }

    // Cashier reject and group settlement
    fun rejectOrder(order: OrderRecord) {
        viewModelScope.launch {
            repository.updateOrderStatus(order.id, order, OrderStatus.REJECTED)
        }
    }

    fun payAllOrdersForTable(tableOrders: List<OrderRecord>) {
        viewModelScope.launch {
            repository.payOrders(tableOrders)
        }
    }

    fun advanceTableSession(orders: List<OrderRecord>, newStatus: OrderStatus) {
        viewModelScope.launch {
            val tableNumber = orders.firstOrNull()?.tableNumber ?: 0
            if (newStatus == OrderStatus.DELIVERED) {
                repository.autoMergeCustomerOrdersForTable(tableNumber, orders)
            } else {
                orders.forEach { order ->
                    if (order.status != OrderStatus.PAID) {
                        repository.updateOrderStatus(order.id, order, newStatus)
                    }
                }
            }
        }
    }

    fun mergeCustomerOrders(ordersToMerge: List<OrderRecord>) {
        viewModelScope.launch {
            repository.mergeOrdersIntoOne(ordersToMerge)
        }
    }

    fun mergeNewOrderWithPreparing(newOrder: OrderRecord, preparingOrders: List<OrderRecord>) {
        viewModelScope.launch {
            repository.mergeNewOrderIntoPreparing(newOrder, preparingOrders)
        }
    }

    fun rejectTableSession(orders: List<OrderRecord>) {
        viewModelScope.launch {
            orders.forEach { order ->
                if (order.status != OrderStatus.PAID) {
                    repository.updateOrderStatus(order.id, order, OrderStatus.REJECTED)
                }
            }
        }
    }

    fun payOrders(orders: List<OrderRecord>) {
        viewModelScope.launch {
            repository.payOrders(orders)
        }
    }

    fun deleteTableSession(orders: List<OrderRecord>) {
        viewModelScope.launch {
            orders.forEach { order ->
                if (order.status != OrderStatus.PAID) {
                    repository.deleteOrder(order)
                }
            }
        }
    }

    // Auth actions
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authError.value = "يرجى إدخال البريد الإلكتروني وكلمة المرور"
            return
        }
        _authLoading.value = true
        _authError.value = null
        _authSuccessMessage.value = null
        _unverifiedEmail.value = null

        viewModelScope.launch {
            try {
                when (val result = repository.login(email, pass)) {
                    is AuthResult.Success -> {
                        val user = result.user
                        repository.currentCafeteriaId = user.id

                        val cachedProfile = repository.loadCafeProfile(user.id)
                        val effectiveCafeName = cachedProfile["cafeName"] ?: user.cafeName
                        val effectiveOwnerName = cachedProfile["ownerName"] ?: user.fullName
                        val effectiveEmail = cachedProfile["email"] ?: user.email

                        _currentUser.value = user.copy(
                            cafeName = effectiveCafeName,
                            fullName = effectiveOwnerName,
                            email = effectiveEmail
                        )
                        _cafeName.value = effectiveCafeName
                        _cafeOwnerName.value = effectiveOwnerName
                        _cafeEmail.value = effectiveEmail
                        cachedProfile["phone"]?.let { _cafePhone.value = it }
                        cachedProfile["address"]?.let { _cafeAddress.value = it }
                        cachedProfile["welcomeNote"]?.let { _cafeWelcomeNote.value = it }
                        cachedProfile["logoType"]?.let { _cafeLogoType.value = it }
                        cachedProfile["customLogoUri"]?.let { _cafeCustomLogoUri.value = it.ifBlank { null } }
                        cachedProfile["menuUrl"]?.let {
                            if (it.isNotBlank()) {
                                _webMenuUrl.value = it
                                repository.baseWebMenuUrl = it
                            }
                        }

                        repository.updateExistingTablesCafeteriaId()
                        repository.startFirestoreOrdersListener(viewModelScope, user.id)
                        repository.loadMenuForCafeteria(user.id)

                        viewModelScope.launch(Dispatchers.IO) {
                            repository.fetchAndSyncCafeProfileFromFirestore(user.id)?.let { remote ->
                                remote["cafeName"]?.let { if (it.isNotBlank()) _cafeName.value = it }
                                remote["phone"]?.let { if (it.isNotBlank()) _cafePhone.value = it }
                                remote["ownerName"]?.let { if (it.isNotBlank()) _cafeOwnerName.value = it }
                                remote["email"]?.let { if (it.isNotBlank()) _cafeEmail.value = it }
                                remote["address"]?.let { if (it.isNotBlank()) _cafeAddress.value = it }
                                remote["welcomeNote"]?.let { if (it.isNotBlank()) _cafeWelcomeNote.value = it }
                                remote["logoType"]?.let { if (it.isNotBlank()) _cafeLogoType.value = it }
                                remote["customLogoUri"]?.let { _cafeCustomLogoUri.value = it.ifBlank { null } }
                                remote["menuUrl"]?.let {
                                    if (it.isNotBlank()) {
                                        _webMenuUrl.value = it
                                        repository.baseWebMenuUrl = it
                                    }
                                }
                            }
                        }

                        _authError.value = null
                        _currentScreen.value = AppScreen.CASHIER_ORDERS
                    }
                    is AuthResult.EmailNotVerified -> {
                        _unverifiedEmail.value = result.email
                        _authError.value = result.message
                    }
                    is AuthResult.Error -> {
                        _authError.value = result.message
                    }
                    else -> {}
                }
            } finally {
                _authLoading.value = false
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

        _authLoading.value = true
        _authError.value = null
        _authSuccessMessage.value = null
        _registrationSuccessMessage.value = null

        viewModelScope.launch {
            try {
                when (val result = repository.register(fullName, email, pass, cafeName)) {
                    is AuthResult.VerificationEmailSent -> {
                        _registrationSuccessMessage.value = result.message
                        _unverifiedEmail.value = result.email
                    }
                    is AuthResult.Success -> {
                        _registrationSuccessMessage.value = "تم إنشاء الحساب بنجاح! يرجى تسجيل الدخول بعد التحقق من بريدك الإلكتروني."
                        _unverifiedEmail.value = result.user.email
                    }
                    is AuthResult.Error -> {
                        _authError.value = result.message
                    }
                    else -> {}
                }
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun resendVerificationEmail(email: String, password: String? = null) {
        if (email.isBlank()) {
            _authError.value = "يرجى إدخال البريد الإلكتروني"
            return
        }
        _authLoading.value = true
        _authError.value = null
        _authSuccessMessage.value = null

        viewModelScope.launch {
            try {
                when (val result = repository.resendEmailVerification(email, password)) {
                    is AuthResult.VerificationEmailSent -> {
                        _authSuccessMessage.value = result.message
                    }
                    is AuthResult.Error -> {
                        _authError.value = result.message
                    }
                    else -> {}
                }
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _authError.value = "يرجى إدخال بريد إلكتروني صحيح"
            return
        }
        _authLoading.value = true
        _authError.value = null
        _authSuccessMessage.value = null

        viewModelScope.launch {
            try {
                when (val result = repository.sendPasswordReset(email)) {
                    is AuthResult.VerificationEmailSent -> {
                        _authSuccessMessage.value = result.message
                    }
                    is AuthResult.Error -> {
                        _authError.value = result.message
                    }
                    else -> {}
                }
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun dismissRegistrationSuccess() {
        _registrationSuccessMessage.value = null
        _currentScreen.value = AppScreen.LOGIN
    }

    fun logout() {
        repository.logout()
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearLocalMenuAndOrders()
        }
        _currentUser.value = null
        _cart.value = emptyList()
        _currentScreen.value = AppScreen.LOGIN
    }
}
