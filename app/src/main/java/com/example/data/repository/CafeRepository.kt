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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class CafeRepository(private val dao: CafeDao) {

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (_: Exception) {
        null
    }

    // Default Web Menu URL for GitHub Pages
    var baseWebMenuUrl: String = "https://alikhlel007-lang.github.io/vvvvv/?table="

    fun startFirestoreOrdersListener(scope: CoroutineScope) {
        try {
            firestore?.collection("orders")?.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    for (doc in snapshot.documents) {
                        val id = doc.id
                        val tableNumber = doc.getLong("tableNumber")?.toInt() ?: 1
                        val customerName = doc.getString("customerName") ?: "طاولة $tableNumber"
                        val itemsSummary = doc.getString("itemsSummary") ?: ""
                        val itemsCount = doc.getLong("itemsCount")?.toInt() ?: 1
                        val totalPrice = doc.getDouble("totalPrice") ?: 0.0
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
                            status = statusStr,
                            timestamp = timestamp,
                            notes = notes
                        )
                        dao.insertOrder(entity)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                "nameAr" to nameAr,
                "nameEn" to nameEn,
                "iconType" to iconType,
                "sortOrder" to 99,
                "customImageUri" to (customImageUri ?: "")
            )
            firestore?.collection("categories")?.document(catId)?.set(map)
        } catch (_: Exception) {}
    }

    suspend fun updateCategory(category: MenuCategory) {
        dao.updateCategory(category.toEntity())
        try {
            firestore?.collection("categories")?.document(category.id)?.set(
                hashMapOf(
                    "id" to category.id,
                    "nameAr" to category.nameAr,
                    "nameEn" to category.nameEn,
                    "iconType" to category.iconType,
                    "sortOrder" to category.sortOrder,
                    "customImageUri" to (category.customImageUri ?: "")
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun deleteCategory(category: MenuCategory) {
        dao.deleteCategory(category.toEntity())
        try {
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
            firestore?.collection("menu_items")?.document(itemId)?.set(itemMap)
        } catch (_: Exception) {}
    }

    suspend fun updateMenuItem(item: MenuItem) {
        dao.updateMenuItem(item.toEntity())
        try {
            val itemMap = hashMapOf(
                "id" to item.id,
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
            firestore?.collection("menu_items")?.document(item.id)?.set(itemMap)
        } catch (_: Exception) {}
    }

    suspend fun deleteMenuItem(item: MenuItem) {
        dao.deleteMenuItem(item.toEntity())
        try {
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
        val sep = if (baseUrl.contains("?")) "&" else "?"
        val payload = "$baseUrl${number}${sep}sec=$secToken"
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
        val sep = if (baseUrl.contains("?")) "&" else "?"
        val newPayload = "$baseUrl${table.tableNumber}${sep}sec=$newSec"
        val updated = table.copy(qrPayload = newPayload, securityToken = newSec)
        dao.updateTable(updated.toEntity())
    }

    suspend fun regenerateAllTablesQr(tables: List<CafeTable>, customBaseUrl: String? = null) {
        val baseUrl = customBaseUrl ?: baseWebMenuUrl
        tables.forEach { table ->
            val newSec = "SEC" + UUID.randomUUID().toString().take(6).uppercase()
            val sep = if (baseUrl.contains("?")) "&" else "?"
            val newPayload = "$baseUrl${table.tableNumber}${sep}sec=$newSec"
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

    // User Auth
    suspend fun login(email: String, password: String): UserAccount? {
        val user = dao.getUserByEmail(email.trim()) ?: return null
        if (user.passwordHash == password.trim() || user.passwordHash.isEmpty()) {
            return user.toDomain()
        }
        return null
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        cafeName: String
    ): UserAccount {
        // عند إنشاء حساب جديد، تبدأ الطاولات والباركودات فارغة تماماً
        dao.deleteAllTables()

        val entity = UserEntity(
            id = "u_" + UUID.randomUUID().toString().take(8),
            fullName = fullName,
            email = email.trim(),
            passwordHash = password.trim(),
            cafeName = cafeName.ifEmpty { "CAFE" },
            role = "owner"
        )
        dao.insertUser(entity)
        return entity.toDomain()
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
