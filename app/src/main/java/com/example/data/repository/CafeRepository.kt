package com.example.data.repository

import com.example.data.local.CafeDao
import com.example.data.local.CategoryEntity
import com.example.data.local.MenuItemEntity
import com.example.data.local.OrderEntity
import com.example.data.local.TableEntity
import com.example.data.local.UserEntity
import com.example.data.model.CafeTable
import com.example.data.model.CartItem
import com.example.data.model.MenuCategory
import com.example.data.model.MenuItem
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStatus
import com.example.data.model.UserAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class AuthResult {
    data class Success(val user: UserAccount) : AuthResult()
    data class EmailNotVerified(val email: String, val message: String) : AuthResult()
    data class VerificationEmailSent(val email: String, val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class CafeRepository(private val dao: CafeDao) {

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (_: Exception) {
        null
    }

    private val firebaseAuth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (_: Exception) {
        null
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T =
        suspendCancellableCoroutine { continuation ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(task.result)
                } else {
                    continuation.resumeWithException(task.exception ?: RuntimeException("Task failed"))
                }
            }
        }

    // Default Web Menu URL for GitHub Pages
    var baseWebMenuUrl: String = "https://alikhlel007-lang.github.io/vvvvv/?table="

    // Multi-tenant cafeteria identifier (defaults to u_default until user logs in or registers)
    var currentCafeteriaId: String = "u_default"

    fun buildTableQrUrl(baseUrl: String, tableNumber: Int, secToken: String): String {
        val clean = baseUrl.trim()
        val sep = if (clean.contains("?")) "&" else "?"
        return if (clean.contains("table=")) {
            if (clean.endsWith("table=")) {
                "${clean}${tableNumber}&cafeteriaId=${currentCafeteriaId}&sec=${secToken}"
            } else {
                "${clean}&cafeteriaId=${currentCafeteriaId}&table=${tableNumber}&sec=${secToken}"
            }
        } else {
            "${clean}${sep}cafeteriaId=${currentCafeteriaId}&table=${tableNumber}&sec=${secToken}"
        }
    }

    fun startFirestoreOrdersListener(scope: CoroutineScope) {
        val parseOrderDoc: (com.google.firebase.firestore.DocumentSnapshot) -> Unit = { doc ->
            scope.launch(Dispatchers.IO) {
                try {
                    val id = doc.id
                    val tableNumber = doc.getLong("tableNumber")?.toInt() ?: 1
                    val customerName = doc.getString("customerName") ?: "طاولة $tableNumber"
                    val itemsSummary = doc.getString("itemsSummary") ?: ""
                    val itemsCount = doc.getLong("itemsCount")?.toInt() ?: 1
                    val totalPrice = doc.getDouble("totalPrice") ?: 0.0
                    val subtotal = doc.getDouble("subtotal") ?: totalPrice
                    val serviceFee = doc.getDouble("serviceFee") ?: 0.0
                    val currency = doc.getString("currency") ?: "د.ع"
                    val statusStr = doc.getString("status") ?: "NEW"
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    val notes = doc.getString("notes") ?: ""

                    val entity = OrderEntity(
                        id = id,
                        tableNumber = tableNumber,
                        customerName = customerName,
                        itemsSummary = itemsSummary,
                        itemsCount = itemsCount,
                        totalPrice = totalPrice,
                        subtotal = subtotal,
                        serviceFee = serviceFee,
                        currency = currency,
                        status = statusStr,
                        timestamp = timestamp,
                        notes = notes
                    )
                    dao.insertOrder(entity)
                } catch (_: Exception) {}
            }
        }

        try {
            // Listen to root collection orders
            firestore?.collection("orders")?.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                for (doc in snapshot.documents) {
                    parseOrderDoc(doc)
                }
            }

            // Listen to multi-tenant orders subcollection for current cafeteria
            if (currentCafeteriaId.isNotBlank()) {
                firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("orders")
                    ?.addSnapshotListener { snapshot, error ->
                        if (error != null || snapshot == null) return@addSnapshotListener
                        for (doc in snapshot.documents) {
                            parseOrderDoc(doc)
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncCurrentMenuToFirestore(cafeId: String = currentCafeteriaId, cafeName: String = "Q-Cafe", accentColor: String = "#00DFC0") {
        currentCafeteriaId = cafeId
        try {
            // 1. Save Cafe Profile
            val cafeProfile = hashMapOf(
                "cafeteriaId" to cafeId,
                "cafeName" to cafeName,
                "themeColor" to accentColor,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore?.collection("cafeterias")?.document(cafeId)?.set(cafeProfile)

            // 2. Sync All Categories
            val cats = dao.getAllCategoriesSync()
            for (c in cats) {
                val catMap = hashMapOf(
                    "id" to c.id,
                    "cafeteriaId" to cafeId,
                    "nameAr" to c.nameAr,
                    "nameEn" to c.nameEn,
                    "iconType" to c.iconType,
                    "sortOrder" to c.sortOrder,
                    "customImageUri" to (c.customImageUri ?: "")
                )
                firestore?.collection("cafeterias")?.document(cafeId)?.collection("categories")?.document(c.id)?.set(catMap)
                firestore?.collection("categories")?.document(c.id)?.set(catMap)
            }

            // 3. Sync All Menu Items
            val items = dao.getAllMenuItemsSync()
            for (i in items) {
                val itemMap = hashMapOf(
                    "id" to i.id,
                    "cafeteriaId" to cafeId,
                    "categoryId" to i.categoryId,
                    "nameAr" to i.nameAr,
                    "nameEn" to i.nameEn,
                    "descriptionAr" to i.descriptionAr,
                    "descriptionEn" to i.descriptionEn,
                    "price" to i.price,
                    "currency" to i.currency,
                    "imageType" to i.imageType,
                    "isAvailable" to i.isAvailable,
                    "imageUri" to (i.imageUri ?: ""),
                    "bgImageUri" to (i.bgImageUri ?: ""),
                    "galleryImages" to (if (i.galleryImagesStr.isBlank()) emptyList<String>() else i.galleryImagesStr.split("|||")),
                    "isOffer" to i.isOffer,
                    "offerDiscountPercent" to i.offerDiscountPercent,
                    "offerTag" to i.offerTag
                )
                firestore?.collection("cafeterias")?.document(cafeId)?.collection("menu_items")?.document(i.id)?.set(itemMap)
                firestore?.collection("menu_items")?.document(i.id)?.set(itemMap)
            }
        } catch (_: Exception) {}
    }

    // Categories
    val categories: Flow<List<MenuCategory>> = dao.getAllCategories().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun addCategory(nameAr: String, nameEn: String, iconType: String, customImageUri: String? = null) {
        val catId = "cat_" + UUID.randomUUID().toString().take(8)
        val entity = CategoryEntity(
            id = catId,
            nameAr = nameAr,
            nameEn = nameEn,
            iconType = iconType,
            sortOrder = 99,
            customImageUri = customImageUri
        )
        dao.insertCategory(entity)
        try {
            val map = hashMapOf(
                "id" to catId,
                "cafeteriaId" to currentCafeteriaId,
                "nameAr" to nameAr,
                "nameEn" to nameEn,
                "iconType" to iconType,
                "sortOrder" to 99,
                "customImageUri" to (customImageUri ?: "")
            )
            firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("categories")?.document(catId)?.set(map)
            firestore?.collection("categories")?.document(catId)?.set(map)
        } catch (_: Exception) {}
    }

    suspend fun updateCategory(category: MenuCategory) {
        dao.updateCategory(category.toEntity())
        try {
            val map = hashMapOf(
                "id" to category.id,
                "cafeteriaId" to currentCafeteriaId,
                "nameAr" to category.nameAr,
                "nameEn" to category.nameEn,
                "iconType" to category.iconType,
                "sortOrder" to category.sortOrder,
                "customImageUri" to (category.customImageUri ?: "")
            )
            firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("categories")?.document(category.id)?.set(map)
            firestore?.collection("categories")?.document(category.id)?.set(map)
        } catch (_: Exception) {}
    }

    suspend fun deleteCategory(category: MenuCategory) {
        dao.deleteCategory(category.toEntity())
        try {
            firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("categories")?.document(category.id)?.delete()
            firestore?.collection("categories")?.document(category.id)?.delete()
        } catch (_: Exception) {}
    }

    // Menu Items
    val menuItems: Flow<List<MenuItem>> = dao.getAllMenuItems().map { list ->
        list.map { it.toDomain() }
    }

    fun getMenuItemsByCategory(categoryId: String): Flow<List<MenuItem>> =
        dao.getMenuItemsByCategory(categoryId).map { list -> list.map { it.toDomain() } }

    suspend fun addMenuItem(
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
        val itemId = "item_" + UUID.randomUUID().toString().take(8)
        val entity = MenuItemEntity(
            id = itemId,
            categoryId = categoryId,
            nameAr = nameAr,
            nameEn = nameEn,
            descriptionAr = descriptionAr,
            descriptionEn = descriptionEn,
            price = price,
            currency = currency,
            imageType = imageType,
            isAvailable = true,
            imageUri = imageUri,
            bgImageUri = bgImageUri,
            galleryImagesStr = galleryImages.joinToString("|||"),
            isOffer = isOffer,
            offerDiscountPercent = offerDiscountPercent,
            offerTag = offerTag
        )
        dao.insertMenuItem(entity)
        try {
            val itemMap = hashMapOf(
                "id" to itemId,
                "cafeteriaId" to currentCafeteriaId,
                "categoryId" to categoryId,
                "nameAr" to nameAr,
                "nameEn" to nameEn,
                "descriptionAr" to descriptionAr,
                "descriptionEn" to descriptionEn,
                "price" to price,
                "currency" to currency,
                "imageType" to imageType,
                "isAvailable" to true,
                "imageUri" to (imageUri ?: ""),
                "bgImageUri" to (bgImageUri ?: ""),
                "galleryImages" to galleryImages,
                "isOffer" to isOffer,
                "offerDiscountPercent" to offerDiscountPercent,
                "offerTag" to offerTag
            )
            firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("menu_items")?.document(itemId)?.set(itemMap)
            firestore?.collection("menu_items")?.document(itemId)?.set(itemMap)
        } catch (_: Exception) {}
    }

    suspend fun updateMenuItem(item: MenuItem) {
        dao.updateMenuItem(item.toEntity())
        try {
            val itemMap = hashMapOf(
                "id" to item.id,
                "cafeteriaId" to currentCafeteriaId,
                "categoryId" to item.categoryId,
                "nameAr" to item.nameAr,
                "nameEn" to item.nameEn,
                "descriptionAr" to item.descriptionAr,
                "descriptionEn" to item.descriptionEn,
                "price" to item.price,
                "currency" to item.currency,
                "imageType" to item.imageType,
                "isAvailable" to item.isAvailable,
                "imageUri" to (item.imageUri ?: ""),
                "bgImageUri" to (item.bgImageUri ?: ""),
                "galleryImages" to item.galleryImages,
                "isOffer" to item.isOffer,
                "offerDiscountPercent" to item.offerDiscountPercent,
                "offerTag" to item.offerTag
            )
            firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("menu_items")?.document(item.id)?.set(itemMap)
            firestore?.collection("menu_items")?.document(item.id)?.set(itemMap)
        } catch (_: Exception) {}
    }

    suspend fun deleteMenuItem(item: MenuItem) {
        dao.deleteMenuItem(item.toEntity())
        try {
            firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("menu_items")?.document(item.id)?.delete()
            firestore?.collection("menu_items")?.document(item.id)?.delete()
        } catch (_: Exception) {}
    }

    // Tables
    val tables: Flow<List<CafeTable>> = dao.getAllTables().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun addTable(number: Int, label: String, capacity: Int, cafeName: String, customBaseUrl: String? = null) {
        val baseUrl = customBaseUrl ?: baseWebMenuUrl
        val secToken = "SEC" + UUID.randomUUID().toString().take(6).uppercase()
        val payload = buildTableQrUrl(baseUrl, number, secToken)
        val entity = TableEntity(
            id = "table_$number",
            tableNumber = number,
            label = label.ifEmpty { "طاولة $number" },
            capacity = capacity,
            qrPayload = payload,
            securityToken = secToken
        )
        dao.insertTable(entity)
    }

    suspend fun regenerateTableQr(table: CafeTable, customBaseUrl: String? = null) {
        val baseUrl = customBaseUrl ?: baseWebMenuUrl
        val newSec = "SEC" + UUID.randomUUID().toString().take(6).uppercase()
        val newPayload = buildTableQrUrl(baseUrl, table.tableNumber, newSec)
        val updated = table.copy(qrPayload = newPayload, securityToken = newSec)
        dao.updateTable(updated.toEntity())
    }

    suspend fun regenerateAllTablesQr(tables: List<CafeTable>, customBaseUrl: String? = null) {
        val baseUrl = customBaseUrl ?: baseWebMenuUrl
        tables.forEach { table ->
            val newSec = "SEC" + UUID.randomUUID().toString().take(6).uppercase()
            val newPayload = buildTableQrUrl(baseUrl, table.tableNumber, newSec)
            dao.updateTable(table.copy(qrPayload = newPayload, securityToken = newSec).toEntity())
        }
    }

    suspend fun updateAllTablesQrUrl(newBaseUrl: String) {
        baseWebMenuUrl = newBaseUrl
    }

    suspend fun deleteTable(table: CafeTable) {
        dao.deleteTable(table.toEntity())
    }

    // Orders
    val orders: Flow<List<OrderRecord>> = dao.getAllOrders().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun createOrder(
        tableNumber: Int,
        customerName: String,
        cartItems: List<CartItem>,
        notes: String
    ): String {
        val summary = cartItems.joinToString("، ") { "${it.quantity}x ${it.menuItem.nameAr}" }
        val count = cartItems.sumOf { it.quantity }
        val subtotal = cartItems.sumOf { it.menuItem.price * it.quantity }
        val currency = cartItems.firstOrNull()?.menuItem?.currency ?: "د.ع"
        val serviceFee = com.example.util.OrderFeeCalculator.calculateServiceFee(subtotal, currency)
        val total = subtotal + serviceFee
        val orderId = "ord_" + System.currentTimeMillis().toString().takeLast(6)

        // التحقق من وجود جلسة نشطة لهذه الطاولة ووراثة مرحلتها الحالية
        val activeOrders = try {
            dao.getActiveOrdersForTable(tableNumber)
        } catch (_: Exception) {
            emptyList()
        }

        val initialStatusName = when {
            activeOrders.any { it.status == OrderStatus.DELIVERED.name || it.status == OrderStatus.READY.name } -> OrderStatus.DELIVERED.name
            activeOrders.any { it.status == OrderStatus.PREPARING.name } -> OrderStatus.PREPARING.name
            else -> OrderStatus.NEW.name
        }

        val cleanedCustomerName = customerName.ifEmpty { "طاولة $tableNumber" }

        // إذا كان لنفس الزبون طلب نشط بالفعل ضمن نفس الجلسة، يتم دمج الأصناف في طلبه القديم
        val existingSameCustomerOrder = activeOrders.firstOrNull {
            it.customerName.trim().equals(cleanedCustomerName.trim(), ignoreCase = true)
        }

        if (existingSameCustomerOrder != null) {
            val updatedSummary = if (existingSameCustomerOrder.itemsSummary.isBlank()) {
                summary
            } else {
                "${existingSameCustomerOrder.itemsSummary}، $summary"
            }
            val updatedCount = existingSameCustomerOrder.itemsCount + count
            val updatedSubtotal = existingSameCustomerOrder.subtotal + subtotal
            val updatedFee = com.example.util.OrderFeeCalculator.calculateServiceFee(updatedSubtotal, currency)
            val updatedTotal = updatedSubtotal + updatedFee
            val updatedNotes = when {
                existingSameCustomerOrder.notes.isBlank() -> notes
                notes.isBlank() -> existingSameCustomerOrder.notes
                else -> "${existingSameCustomerOrder.notes} | $notes"
            }

            val updatedEntity = existingSameCustomerOrder.copy(
                itemsSummary = updatedSummary,
                itemsCount = updatedCount,
                subtotal = updatedSubtotal,
                serviceFee = updatedFee,
                totalPrice = updatedTotal,
                timestamp = System.currentTimeMillis(),
                notes = updatedNotes
            )
            dao.updateOrder(updatedEntity)

            try {
                val updateMap = hashMapOf<String, Any>(
                    "itemsSummary" to updatedSummary,
                    "itemsCount" to updatedCount,
                    "subtotal" to updatedSubtotal,
                    "serviceFee" to updatedFee,
                    "totalPrice" to updatedTotal,
                    "timestamp" to System.currentTimeMillis(),
                    "notes" to updatedNotes
                )
                firestore?.collection("orders")?.document(existingSameCustomerOrder.id)?.update(updateMap)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return existingSameCustomerOrder.id
        }

        val entity = OrderEntity(
            id = orderId,
            tableNumber = tableNumber,
            customerName = cleanedCustomerName,
            itemsSummary = summary,
            itemsCount = count,
            totalPrice = total,
            subtotal = subtotal,
            serviceFee = serviceFee,
            currency = currency,
            status = initialStatusName,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        dao.insertOrder(entity)

        // Sync order to Firestore
        try {
            val orderMap = hashMapOf(
                "id" to orderId,
                "tableNumber" to tableNumber,
                "customerName" to cleanedCustomerName,
                "itemsSummary" to summary,
                "itemsCount" to count,
                "totalPrice" to total,
                "subtotal" to subtotal,
                "serviceFee" to serviceFee,
                "currency" to currency,
                "status" to initialStatusName,
                "timestamp" to System.currentTimeMillis(),
                "notes" to notes
            )
            firestore?.collection("orders")?.document(orderId)?.set(orderMap)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return orderId
    }

    suspend fun payOrders(ordersList: List<OrderRecord>) {
        ordersList.forEach { order ->
            val updated = order.copy(status = OrderStatus.PAID).toEntity()
            dao.updateOrder(updated)
            try {
                firestore?.collection("orders")?.document(order.id)?.update("status", OrderStatus.PAID.name)
            } catch (_: Exception) {}
        }
    }

    suspend fun updateOrderStatus(orderId: String, currentOrder: OrderRecord, newStatus: OrderStatus) {
        val updated = currentOrder.copy(status = newStatus).toEntity()
        dao.updateOrder(updated)
        try {
            firestore?.collection("orders")?.document(orderId)?.update("status", newStatus.name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteOrder(order: OrderRecord) {
        dao.deleteOrder(order.toEntity())
        try {
            firestore?.collection("orders")?.document(order.id)?.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                enforceSingleItemPerCategory()
            } catch (_: Exception) {}
        }
    }

    suspend fun enforceSingleItemPerCategory() {
        val items = dao.getAllMenuItemsSync()
        val grouped = items.groupBy { it.categoryId }
        grouped.forEach { (_, catItems) ->
            if (catItems.size > 1) {
                catItems.drop(1).forEach { extra ->
                    dao.deleteMenuItem(extra)
                }
            }
        }
    }

    suspend fun generateInitialTables(count: Int = 8) {
        val newTables = (1..count).map { number ->
            val secToken = "SEC" + (1000 + number)
            TableEntity(
                id = "table_$number",
                tableNumber = number,
                label = "طاولة $number",
                capacity = if (number % 2 == 0) 4 else 2,
                qrPayload = "$baseWebMenuUrl$number&sec=$secToken",
                securityToken = secToken
            )
        }
        dao.insertTables(newTables)
    }

    suspend fun clearAllTables() {
        dao.deleteAllTables()
    }

    // User Auth with Firebase Authentication & Email Verification
    suspend fun login(email: String, password: String): AuthResult {
        val cleanEmail = email.trim()
        val cleanPass = password.trim()

        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            return AuthResult.Error("يرجى إدخال البريد الإلكتروني وكلمة المرور")
        }

        if (firebaseAuth != null) {
            return try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(cleanEmail, cleanPass).awaitTask()
                val firebaseUser = authResult.user ?: return AuthResult.Error("تعذر التحقق من المستخدم")

                // Reload to refresh isEmailVerified from Firebase
                firebaseUser.reload().awaitTask()

                if (!firebaseUser.isEmailVerified) {
                    firebaseAuth.signOut()
                    AuthResult.EmailNotVerified(
                        email = cleanEmail,
                        message = "لم يتم تفعيل الحساب بعد! يرجى مراجعة بريدك الإلكتروني ($cleanEmail) والضغط على رابط التحقق قبل تسجيل الدخول."
                    )
                } else {
                    val uid = firebaseUser.uid
                    val localUser = dao.getUserByEmail(cleanEmail)
                    val account = if (localUser != null) {
                        localUser.toDomain()
                    } else {
                        val newAccount = UserAccount(
                            id = uid,
                            fullName = firebaseUser.displayName ?: "مدير الكافيه",
                            email = cleanEmail,
                            cafeName = "Q-Cafe",
                            role = "owner"
                        )
                        dao.insertUser(
                            UserEntity(
                                id = uid,
                                fullName = newAccount.fullName,
                                email = cleanEmail,
                                passwordHash = cleanPass,
                                cafeName = newAccount.cafeName,
                                role = newAccount.role
                            )
                        )
                        newAccount
                    }
                    currentCafeteriaId = uid
                    AuthResult.Success(account)
                }
            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is FirebaseAuthInvalidUserException -> "لا يوجد حساب مسجل بهذا البريد الإلكتروني"
                    is FirebaseAuthInvalidCredentialsException -> "البريد الإلكتروني أو كلمة المرور غير صحيحة"
                    else -> e.localizedMessage ?: "فشل تسجيل الدخول. تأكد من صحة البيانات واتصال الإنترنت"
                }
                AuthResult.Error(errorMsg)
            }
        } else {
            val user = dao.getUserByEmail(cleanEmail)
            return if (user != null && user.passwordHash == cleanPass) {
                currentCafeteriaId = user.id
                AuthResult.Success(user.toDomain())
            } else {
                AuthResult.Error("البريد الإلكتروني أو كلمة المرور غير صحيحة")
            }
        }
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        cafeName: String
    ): AuthResult {
        // الحساب الجديد يبدأ فارغاً تماماً من الطاولات والطلبات التجريبية
        dao.deleteAllTables()
        dao.deleteAllOrders()

        val cleanEmail = email.trim()
        val cleanPass = password.trim()
        val cleanCafeName = cafeName.trim().ifEmpty { "Q-Cafe" }
        val cleanFullName = fullName.trim().ifEmpty { "مدير الكافيه" }

        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            return AuthResult.Error("يرجى ملء جميع الحقول المطلوبة")
        }

        if (cleanPass.length < 6) {
            return AuthResult.Error("كلمة المرور يجب أن لا تقل عن 6 أحرف")
        }

        if (firebaseAuth != null) {
            return try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(cleanEmail, cleanPass).awaitTask()
                val firebaseUser = authResult.user ?: return AuthResult.Error("تعذر إنشاء الحساب في Firebase")

                // Send email verification link
                firebaseUser.sendEmailVerification().awaitTask()

                val uid = firebaseUser.uid
                val userProfile = hashMapOf(
                    "id" to uid,
                    "fullName" to cleanFullName,
                    "email" to cleanEmail,
                    "cafeName" to cleanCafeName,
                    "role" to "owner",
                    "createdAt" to System.currentTimeMillis()
                )
                firestore?.collection("cafeterias")?.document(uid)?.set(userProfile)
                firestore?.collection("users")?.document(uid)?.set(userProfile)

                val entity = UserEntity(
                    id = uid,
                    fullName = cleanFullName,
                    email = cleanEmail,
                    passwordHash = cleanPass,
                    cafeName = cleanCafeName,
                    role = "owner"
                )
                dao.insertUser(entity)

                // Sign out so user must verify their email before accessing the cashier
                firebaseAuth.signOut()

                AuthResult.VerificationEmailSent(
                    email = cleanEmail,
                    message = "تم إنشاء الحساب بنجاح! تم إرسال رابط تأكيد إلى بريدك الإلكتروني ($cleanEmail).\n\nيرجى فتح بريدك الإلكتروني والضغط على رابط التحقق قبل تسجيل الدخول."
                )
            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is FirebaseAuthUserCollisionException -> "البريد الإلكتروني مسجل بالفعل، يرجى تسجيل الدخول"
                    is FirebaseAuthWeakPasswordException -> "كلمة المرور ضعيفة جداً"
                    is FirebaseAuthInvalidCredentialsException -> "صيغة البريد الإلكتروني غير صحيحة"
                    else -> e.localizedMessage ?: "حدث خطأ أثناء إنشاء الحساب في Firebase"
                }
                AuthResult.Error(errorMsg)
            }
        } else {
            val entity = UserEntity(
                id = "u_" + UUID.randomUUID().toString().take(8),
                fullName = cleanFullName,
                email = cleanEmail,
                passwordHash = cleanPass,
                cafeName = cleanCafeName,
                role = "owner"
            )
            dao.insertUser(entity)
            currentCafeteriaId = entity.id
            return AuthResult.Success(entity.toDomain())
        }
    }

    suspend fun resendEmailVerification(email: String, password: String? = null): AuthResult {
        val cleanEmail = email.trim()
        if (firebaseAuth == null) {
            return AuthResult.Error("خدمة Firebase غير متوفرة")
        }
        return try {
            var currentUser = firebaseAuth.currentUser
            if ((currentUser == null || currentUser.email != cleanEmail) && !password.isNullOrBlank()) {
                val res = firebaseAuth.signInWithEmailAndPassword(cleanEmail, password.trim()).awaitTask()
                currentUser = res.user
            }
            if (currentUser != null) {
                currentUser.sendEmailVerification().awaitTask()
                firebaseAuth.signOut()
                AuthResult.VerificationEmailSent(
                    email = cleanEmail,
                    message = "تمت إعادة إرسال رابط التفعيل إلى $cleanEmail بنجاح! تفقد بريدك الوارد والبريد غير الهام (Spam)."
                )
            } else {
                AuthResult.Error("يرجى إدخال كلمة المرور مع البريد لإعادة إرسال الرابط")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "تعذر إعادة إرسال رابط التفعيل")
        }
    }

    suspend fun sendPasswordReset(email: String): AuthResult {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return AuthResult.Error("يرجى إدخال بريد إلكتروني صالح")
        }
        if (firebaseAuth == null) {
            return AuthResult.Error("خدمة Firebase غير متوفرة")
        }
        return try {
            firebaseAuth.sendPasswordResetEmail(cleanEmail).awaitTask()
            AuthResult.VerificationEmailSent(
                email = cleanEmail,
                message = "تم إرسال رابط إعادة تعيين كلمة المرور إلى $cleanEmail بنجاح! تفقد بريدك الإلكتروني واتبع الرابط لتغيير كلمة المرور."
            )
        } catch (e: Exception) {
            val errorMsg = when (e) {
                is FirebaseAuthInvalidUserException -> "لا يوجد حساب مسجل بهذا البريد الإلكتروني"
                is FirebaseAuthInvalidCredentialsException -> "صيغة البريد الإلكتروني غير صحيحة"
                else -> e.localizedMessage ?: "فشل إرسال رابط إعادة تعيين كلمة المرور"
            }
            AuthResult.Error(errorMsg)
        }
    }

    // Mapping extensions
    private fun CategoryEntity.toDomain() = MenuCategory(
        id = id,
        nameAr = nameAr,
        nameEn = nameEn,
        iconType = iconType,
        sortOrder = sortOrder,
        customImageUri = customImageUri
    )

    private fun MenuCategory.toEntity() = CategoryEntity(
        id = id,
        nameAr = nameAr,
        nameEn = nameEn,
        iconType = iconType,
        sortOrder = sortOrder,
        customImageUri = customImageUri
    )

    private fun MenuItemEntity.toDomain() = MenuItem(
        id = id,
        categoryId = categoryId,
        nameAr = nameAr,
        nameEn = nameEn,
        descriptionAr = descriptionAr,
        descriptionEn = descriptionEn,
        price = price,
        currency = currency,
        imageType = imageType,
        isAvailable = isAvailable,
        imageUri = imageUri,
        bgImageUri = bgImageUri,
        galleryImages = if (galleryImagesStr.isBlank()) emptyList() else galleryImagesStr.split("|||").filter { it.isNotBlank() },
        isOffer = isOffer,
        offerDiscountPercent = offerDiscountPercent,
        offerTag = offerTag
    )

    private fun MenuItem.toEntity() = MenuItemEntity(
        id = id,
        categoryId = categoryId,
        nameAr = nameAr,
        nameEn = nameEn,
        descriptionAr = descriptionAr,
        descriptionEn = descriptionEn,
        price = price,
        currency = currency,
        imageType = imageType,
        isAvailable = isAvailable,
        imageUri = imageUri,
        bgImageUri = bgImageUri,
        galleryImagesStr = galleryImages.joinToString("|||"),
        isOffer = isOffer,
        offerDiscountPercent = offerDiscountPercent,
        offerTag = offerTag
    )

    private fun TableEntity.toDomain() = CafeTable(
        id = id,
        tableNumber = tableNumber,
        label = label,
        capacity = capacity,
        qrPayload = qrPayload,
        securityToken = securityToken
    )

    private fun CafeTable.toEntity() = TableEntity(
        id = id,
        tableNumber = tableNumber,
        label = label,
        capacity = capacity,
        qrPayload = qrPayload,
        securityToken = securityToken
    )

    private fun OrderEntity.toDomain() = OrderRecord(
        id = id,
        tableNumber = tableNumber,
        customerName = customerName,
        itemsSummary = itemsSummary,
        itemsCount = itemsCount,
        totalPrice = totalPrice,
        subtotal = if (subtotal > 0.0) subtotal else totalPrice,
        serviceFee = serviceFee,
        currency = currency,
        status = when (status.uppercase()) {
            "READY" -> OrderStatus.DELIVERED
            else -> try {
                OrderStatus.valueOf(status)
            } catch (_: Exception) {
                OrderStatus.NEW
            }
        },
        timestamp = timestamp,
        notes = notes
    )

    private fun OrderRecord.toEntity() = OrderEntity(
        id = id,
        tableNumber = tableNumber,
        customerName = customerName,
        itemsSummary = itemsSummary,
        itemsCount = itemsCount,
        totalPrice = totalPrice,
        subtotal = subtotal,
        serviceFee = serviceFee,
        currency = currency,
        status = status.name,
        timestamp = timestamp,
        notes = notes
    )

    private fun UserEntity.toDomain() = UserAccount(
        id = id,
        fullName = fullName,
        email = email,
        cafeName = cafeName,
        role = role
    )
}
