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

class CafeRepository(private val dao: CafeDao, private val context: android.content.Context? = null) {

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (_: Exception) {
            null
        }

    private val firebaseAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

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

    private var ordersListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    fun buildTableQrUrl(baseUrl: String, tableNumber: Int, secToken: String): String {
        val clean = baseUrl.trim()
        val baseWithoutQuery = if (clean.contains("?")) clean.substringBefore("?") else clean
        val normalized = if (baseWithoutQuery.endsWith("/")) baseWithoutQuery else "$baseWithoutQuery/"
        return "${normalized}?cafeteriaId=${currentCafeteriaId}&table=${tableNumber}&sec=${secToken}"
    }

    fun startFirestoreOrdersListener(scope: CoroutineScope, cafeId: String = currentCafeteriaId) {
        // Cancel any existing listener registration
        ordersListenerRegistration?.remove()
        ordersListenerRegistration = null

        val targetCafeId = cafeId.ifBlank { currentCafeteriaId }
        if (targetCafeId.isBlank() || targetCafeId == "u_default") {
            return
        }

        val parseOrderDoc: (com.google.firebase.firestore.DocumentSnapshot) -> Unit = { doc ->
            scope.launch(Dispatchers.IO) {
                try {
                    val id = doc.id
                    val docCafeId = doc.getString("cafeteriaId") ?: targetCafeId
                    // Strict isolation: discard orders from other cafeterias
                    if (docCafeId != targetCafeId) return@launch

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
            // Strictly listen to THIS cafeteria's orders subcollection ONLY.
            // Do NOT listen to global collection("orders") to prevent cross-tenant leakage.
            ordersListenerRegistration = firestore?.collection("cafeterias")?.document(targetCafeId)?.collection("orders")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    for (doc in snapshot.documents) {
                        parseOrderDoc(doc)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveCafeProfile(
        cafeId: String,
        cafeName: String,
        phone: String,
        ownerName: String,
        email: String,
        address: String,
        welcomeNote: String,
        logoType: String,
        customLogoUri: String?,
        menuUrl: String,
        themeColor: String
    ) {
        val prefs = context?.getSharedPreferences("cafe_profile_prefs", android.content.Context.MODE_PRIVATE)
        prefs?.edit()?.apply {
            putString("cafe_name_$cafeId", cafeName)
            putString("cafe_phone_$cafeId", phone)
            putString("cafe_owner_$cafeId", ownerName)
            putString("cafe_email_$cafeId", email)
            putString("cafe_address_$cafeId", address)
            putString("cafe_welcome_$cafeId", welcomeNote)
            putString("cafe_logo_type_$cafeId", logoType)
            putString("cafe_logo_uri_$cafeId", customLogoUri ?: "")
            putString("cafe_menu_url_$cafeId", menuUrl)
            putString("cafe_theme_color_$cafeId", themeColor)
            apply()
        }

        try {
            val profileMap = hashMapOf<String, Any?>(
                "cafeteriaId" to cafeId,
                "cafeName" to cafeName,
                "phone" to phone,
                "ownerName" to ownerName,
                "email" to email,
                "address" to address,
                "welcomeNote" to welcomeNote,
                "logoType" to logoType,
                "customLogoUri" to (customLogoUri ?: ""),
                "menuUrl" to menuUrl,
                "themeColor" to themeColor,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore?.collection("cafeterias")?.document(cafeId)?.set(profileMap, com.google.firebase.firestore.SetOptions.merge())
        } catch (_: Exception) {}
    }

    fun loadCafeProfile(cafeId: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val prefs = context?.getSharedPreferences("cafe_profile_prefs", android.content.Context.MODE_PRIVATE)
        prefs?.let { p ->
            p.getString("cafe_name_$cafeId", null)?.let { map["cafeName"] = it }
            p.getString("cafe_phone_$cafeId", null)?.let { map["phone"] = it }
            p.getString("cafe_owner_$cafeId", null)?.let { map["ownerName"] = it }
            p.getString("cafe_email_$cafeId", null)?.let { map["email"] = it }
            p.getString("cafe_address_$cafeId", null)?.let { map["address"] = it }
            p.getString("cafe_welcome_$cafeId", null)?.let { map["welcomeNote"] = it }
            p.getString("cafe_logo_type_$cafeId", null)?.let { map["logoType"] = it }
            p.getString("cafe_logo_uri_$cafeId", null)?.let { map["customLogoUri"] = it }
            p.getString("cafe_menu_url_$cafeId", null)?.let { map["menuUrl"] = it }
            p.getString("cafe_theme_color_$cafeId", null)?.let { map["themeColor"] = it }
        }
        return map
    }

    suspend fun fetchAndSyncCafeProfileFromFirestore(cafeId: String): Map<String, String>? {
        return try {
            val snap = firestore?.collection("cafeterias")?.document(cafeId)?.get()?.awaitTask()
            if (snap != null && snap.exists()) {
                val name = snap.getString("cafeName") ?: snap.getString("name") ?: ""
                val phone = snap.getString("phone") ?: ""
                val owner = snap.getString("ownerName") ?: snap.getString("fullName") ?: ""
                val email = snap.getString("email") ?: ""
                val addr = snap.getString("address") ?: ""
                val welcome = snap.getString("welcomeNote") ?: ""
                val logoType = snap.getString("logoType") ?: "custom"
                val logoUri = snap.getString("customLogoUri") ?: ""
                val menuUrl = snap.getString("menuUrl") ?: ""
                val theme = snap.getString("themeColor") ?: ""

                if (name.isNotBlank()) {
                    saveCafeProfile(
                        cafeId = cafeId,
                        cafeName = name,
                        phone = phone,
                        ownerName = owner,
                        email = email,
                        address = addr,
                        welcomeNote = welcome,
                        logoType = logoType,
                        customLogoUri = logoUri.ifBlank { null },
                        menuUrl = menuUrl.ifBlank { baseWebMenuUrl },
                        themeColor = theme.ifBlank { "#00DFC0" }
                    )
                }
                loadCafeProfile(cafeId)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun clearLocalMenuAndOrders() {
        dao.deleteAllCategories()
        dao.deleteAllMenuItems()
        dao.deleteAllOrders()
    }

    suspend fun loadMenuForCafeteria(cafeId: String) {
        currentCafeteriaId = cafeId
        if (cafeId.isBlank() || cafeId == "u_default") return

        try {
            val catDocs = firestore?.collection("cafeterias")?.document(cafeId)?.collection("categories")?.get()?.awaitTask()
            val itemDocs = firestore?.collection("cafeterias")?.document(cafeId)?.collection("menu_items")?.get()?.awaitTask()

            val hasRemoteCategories = catDocs != null && !catDocs.isEmpty
            val hasRemoteItems = itemDocs != null && !itemDocs.isEmpty

            if (hasRemoteCategories) {
                dao.deleteAllCategories()
                val loadedCategories = catDocs.documents.mapNotNull { doc ->
                    val id = doc.id
                    val nameAr = doc.getString("nameAr") ?: return@mapNotNull null
                    val nameEn = doc.getString("nameEn") ?: ""
                    val iconType = doc.getString("iconType") ?: "burger"
                    val sortOrder = doc.getLong("sortOrder")?.toInt() ?: 0
                    val customImageUri = doc.getString("customImageUri")
                    CategoryEntity(id, nameAr, nameEn, iconType, sortOrder, customImageUri)
                }
                dao.insertCategories(loadedCategories)
            }

            if (hasRemoteItems) {
                dao.deleteAllMenuItems()
                val loadedItems = itemDocs.documents.mapNotNull { doc ->
                    val id = doc.id
                    val categoryId = doc.getString("categoryId") ?: return@mapNotNull null
                    val nameAr = doc.getString("nameAr") ?: return@mapNotNull null
                    val nameEn = doc.getString("nameEn") ?: ""
                    val descriptionAr = doc.getString("descriptionAr") ?: ""
                    val descriptionEn = doc.getString("descriptionEn") ?: ""
                    val price = doc.getDouble("price") ?: 0.0
                    val currency = doc.getString("currency") ?: "د.ع"
                    val imageType = doc.getString("imageType") ?: "burger"
                    val isAvailable = doc.getBoolean("isAvailable") ?: true
                    val imageUri = doc.getString("imageUri")
                    val bgImageUri = doc.getString("bgImageUri")
                    val isOffer = doc.getBoolean("isOffer") ?: false
                    val offerDiscountPercent = doc.getLong("offerDiscountPercent")?.toInt() ?: 0
                    val offerTag = doc.getString("offerTag") ?: "عرض خاص"

                    MenuItemEntity(
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
                        galleryImagesStr = "",
                        isOffer = isOffer,
                        offerDiscountPercent = offerDiscountPercent,
                        offerTag = offerTag
                    )
                }
                dao.insertMenuItems(loadedItems)
            } else if (!hasRemoteCategories && !hasRemoteItems) {
                // Newly created cafeteria with empty cloud menu:
                // Seed initial categories & items specifically for this cafeteria
                dao.deleteAllCategories()
                dao.deleteAllMenuItems()
                seedInitialMenuForCafeteria(cafeId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun seedInitialMenuForCafeteria(cafeId: String) {
        val initialCategories = listOf(
            CategoryEntity("cat_burgers", "البرجر", "Burgers", "burger", 1),
            CategoryEntity("cat_appetizers", "المقبلات", "Appetizers", "fries", 2),
            CategoryEntity("cat_drinks", "المشروبات", "Drinks", "drink", 3),
            CategoryEntity("cat_meals", "الوجبات", "Meals", "meals", 4),
            CategoryEntity("cat_sweets", "الحلويات", "Desserts", "sweets", 5)
        )
        dao.insertCategories(initialCategories)

        val initialItems = listOf(
            MenuItemEntity(
                id = "b1",
                categoryId = "cat_burgers",
                nameAr = "برجر كلاسيك",
                nameEn = "Classic Burger",
                descriptionAr = "لحم بقري مشوي مع الجبن الطازج والخس والطماطم مع البطاطس المقلية",
                descriptionEn = "Grilled beef patty with cheese, fresh lettuce and tomatoes",
                price = 7000.0,
                currency = "د.ع",
                imageType = "burger",
                isAvailable = true,
                isOffer = true,
                offerDiscountPercent = 15,
                offerTag = "عرض اليوم"
            ),
            MenuItemEntity(
                id = "a1",
                categoryId = "cat_appetizers",
                nameAr = "بطاطس مقلية مقرمشة",
                nameEn = "French Fries",
                descriptionAr = "بطاطس ذهبية مقرمشة مع بهارات الكافيه الخاصة وصلصة الكاتشب",
                descriptionEn = "Golden crispy french fries with special cafe seasonings",
                price = 3000.0,
                currency = "د.ع",
                imageType = "fries",
                isAvailable = true,
                isOffer = false,
                offerTag = "طازج"
            ),
            MenuItemEntity(
                id = "d1",
                categoryId = "cat_drinks",
                nameAr = "سبانش لاتيه بارد",
                nameEn = "Iced Spanish Latte",
                descriptionAr = "قهوة إسبريسو غنية مع الحليب المكثف المحلى ومكعبات الثلج المنعشة",
                descriptionEn = "Rich espresso with sweet condensed milk and ice cubes",
                price = 4500.0,
                currency = "د.ع",
                imageType = "drink",
                isAvailable = true,
                isOffer = true,
                offerDiscountPercent = 10,
                offerTag = "الأكثر طلباً"
            ),
            MenuItemEntity(
                id = "m1",
                categoryId = "cat_meals",
                nameAr = "وجبة شاورما عربي دجاج",
                nameEn = "Chicken Shawarma Meal",
                descriptionAr = "قطع شاورما دجاج متبلة في خبز الصاج مع الثومية والبطاطس والمخلل",
                descriptionEn = "Seasoned chicken shawarma in saj bread with garlic sauce and fries",
                price = 8000.0,
                currency = "د.ع",
                imageType = "meals",
                isAvailable = true,
                isOffer = false,
                offerTag = "وجبة مميزة"
            ),
            MenuItemEntity(
                id = "s1",
                categoryId = "cat_sweets",
                nameAr = "تشيز كيك فراولة",
                nameEn = "Strawberry Cheesecake",
                descriptionAr = "تشيز كيك كريمي بارد على قاعدة بسكويت مقرمش مع صوص الفراولة الطازجة",
                descriptionEn = "Creamy cheesecake on crunchy biscuit base with strawberry sauce",
                price = 5000.0,
                currency = "د.ع",
                imageType = "sweets",
                isAvailable = true,
                isOffer = true,
                offerDiscountPercent = 20,
                offerTag = "حلوى اليوم"
            )
        )
        dao.insertMenuItems(initialItems)

        // Upload to this cafeteria's Firestore
        syncCurrentMenuToFirestore(cafeId)
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
            firestore?.collection("cafeterias")?.document(cafeId)?.set(cafeProfile, com.google.firebase.firestore.SetOptions.merge())

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
        } catch (_: Exception) {}
    }

    suspend fun deleteCategory(category: MenuCategory) {
        dao.deleteCategory(category.toEntity())
        try {
            firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("categories")?.document(category.id)?.delete()
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
        } catch (_: Exception) {}
    }

    suspend fun deleteMenuItem(item: MenuItem) {
        dao.deleteMenuItem(item.toEntity())
        try {
            firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("menu_items")?.document(item.id)?.delete()
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
        val currency = cartItems.firstOrNull()?.menuItem?.currency ?: "د.ع"
        val summary = cartItems.joinToString("، ") {
            val lineTotal = it.menuItem.price * it.quantity
            com.example.util.OrderSummaryHelper.formatLineItem(it.menuItem.nameAr, it.quantity, lineTotal, currency)
        }
        val count = cartItems.sumOf { it.quantity }
        val subtotal = cartItems.sumOf { it.menuItem.price * it.quantity }
        val serviceFee = com.example.util.OrderFeeCalculator.calculateServiceFee(subtotal, currency)
        val total = subtotal + serviceFee
        val orderId = "ord_" + System.currentTimeMillis().toString().takeLast(6)

        // التحقق من وجود جلسة نشطة لهذه الطاولة
        val activeOrders = try {
            dao.getActiveOrdersForTable(tableNumber)
        } catch (_: Exception) {
            emptyList()
        }

        val cleanedCustomerName = customerName.ifEmpty { "طاولة $tableNumber" }

        // إذا كان هناك طلب سابق لنفس الطاولة/الزبون ما زال في قائمة "طلبات جديدة" (NEW) يتم الدمج تلقائياً
        val existingNewOrder = activeOrders.firstOrNull {
            it.status == OrderStatus.NEW.name &&
            (it.customerName.trim().equals(cleanedCustomerName.trim(), ignoreCase = true) || cleanedCustomerName == "طاولة $tableNumber")
        }

        if (existingNewOrder != null) {
            val updatedSummary = com.example.util.OrderSummaryHelper.combineAndMultiplySummaries(
                listOf(existingNewOrder.itemsSummary, summary)
            )
            val updatedCount = existingNewOrder.itemsCount + count
            val updatedSubtotal = existingNewOrder.subtotal + subtotal
            val updatedFee = com.example.util.OrderFeeCalculator.calculateServiceFee(updatedSubtotal, currency)
            val updatedTotal = updatedSubtotal + updatedFee
            val updatedNotes = com.example.util.OrderSummaryHelper.combineNotes(
                listOf(existingNewOrder.notes, notes)
            )

            val updatedEntity = existingNewOrder.copy(
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
                firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("orders")?.document(existingNewOrder.id)?.update(updateMap)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return existingNewOrder.id
        }

        // إذا كان الطلب السابق قد وصل لقسم التحضير أو تسديد الحساب، يعامل كطلب جديد اعتيادي (NEW)
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
            status = OrderStatus.NEW.name,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        dao.insertOrder(entity)

        // Sync order to Firestore
        try {
            val orderMap = hashMapOf(
                "id" to orderId,
                "cafeteriaId" to currentCafeteriaId,
                "tableNumber" to tableNumber,
                "customerName" to cleanedCustomerName,
                "itemsSummary" to summary,
                "itemsCount" to count,
                "totalPrice" to total,
                "subtotal" to subtotal,
                "serviceFee" to serviceFee,
                "currency" to currency,
                "status" to OrderStatus.NEW.name,
                "timestamp" to System.currentTimeMillis(),
                "notes" to notes
            )
            firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("orders")?.document(orderId)?.set(orderMap)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return orderId
    }

    /**
     * دمج طلبات نفس الزبون يدوياً أو تلقائياً في طلب واحد موحد
     */
    suspend fun mergeOrdersIntoOne(orders: List<OrderRecord>) {
        if (orders.size <= 1) return
        val targetOrder = orders.first()
        val otherOrders = orders.drop(1)

        val combinedSummary = com.example.util.OrderSummaryHelper.combineAndMultiplySummaries(orders.map { it.itemsSummary })
        val combinedCount = orders.sumOf { it.itemsCount }
        val combinedSubtotal = orders.sumOf { it.subtotal }
        val currency = targetOrder.currency
        val combinedFee = com.example.util.OrderFeeCalculator.calculateServiceFee(combinedSubtotal, currency)
        val combinedTotal = combinedSubtotal + combinedFee
        val combinedNotes = com.example.util.OrderSummaryHelper.combineNotes(orders.map { it.notes })

        val updatedTarget = targetOrder.copy(
            itemsSummary = combinedSummary,
            itemsCount = combinedCount,
            subtotal = combinedSubtotal,
            serviceFee = combinedFee,
            totalPrice = combinedTotal,
            notes = combinedNotes,
            timestamp = orders.maxOf { it.timestamp }
        )

        dao.updateOrder(updatedTarget.toEntity())
        try {
            val map = hashMapOf<String, Any>(
                "itemsSummary" to combinedSummary,
                "itemsCount" to combinedCount,
                "subtotal" to combinedSubtotal,
                "serviceFee" to combinedFee,
                "totalPrice" to combinedTotal,
                "notes" to combinedNotes,
                "timestamp" to updatedTarget.timestamp
            )
            firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("orders")?.document(targetOrder.id)?.update(map)
        } catch (_: Exception) {}

        // حذف الطلبات المكررة التي تم دمجها
        otherOrders.forEach { other ->
            dao.deleteOrder(other.toEntity())
            try {
                firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("orders")?.document(other.id)?.delete()
            } catch (_: Exception) {}
        }
    }

    /**
     * دمج طلب جديد مع طلب سابق قيد التحضير لنفس الطاولة
     */
    suspend fun mergeNewOrderIntoPreparing(newOrder: OrderRecord, preparingOrders: List<OrderRecord>) {
        if (preparingOrders.isEmpty()) return
        val targetPreparingOrder = preparingOrders.first()
        val allOrders = listOf(targetPreparingOrder, newOrder)
        mergeOrdersIntoOne(allOrders)
        updateOrderStatus(targetPreparingOrder.id, targetPreparingOrder, OrderStatus.PREPARING)
    }

    /**
     * الدمج التلقائي عند الانتقال للمرحلة الثالثة (تسديد حساب الطاولة)
     * في حال وصل إلى المرحلة الثالثة كلا الطلبين ولم يتم الدمج بينهما يدوياً، يتم الدمج تلقائياً
     */
    suspend fun autoMergeCustomerOrdersForTable(tableNumber: Int, ordersToAdvance: List<OrderRecord>) {
        ordersToAdvance.forEach { order ->
            if (order.status != OrderStatus.PAID) {
                updateOrderStatus(order.id, order, OrderStatus.DELIVERED)
            }
        }

        val allActiveForTable = try {
            dao.getActiveOrdersForTable(tableNumber)
        } catch (_: Exception) {
            emptyList()
        }

        val deliveredOrders = allActiveForTable
            .filter { it.status == OrderStatus.DELIVERED.name || it.status == OrderStatus.READY.name }
            .map { it.toDomain() }

        val byCustomer = deliveredOrders.groupBy { it.customerName }
        byCustomer.forEach { (_, custOrders) ->
            if (custOrders.size > 1) {
                mergeOrdersIntoOne(custOrders)
            }
        }
    }

    suspend fun payOrders(ordersList: List<OrderRecord>) {
        ordersList.forEach { order ->
            val settledTotal = com.example.util.OrderFeeCalculator.calculateSettlementTotal(order.subtotal, order.currency)
            val settledFee = com.example.util.OrderFeeCalculator.calculateSettlementFee(order.subtotal, order.currency)
            val finalTotal = if (settledTotal > 0.0) settledTotal else order.totalPrice
            val finalFee = if (settledFee > 0.0) settledFee else order.serviceFee
            val updated = order.copy(
                status = OrderStatus.PAID,
                totalPrice = finalTotal,
                serviceFee = finalFee
            ).toEntity()
            dao.updateOrder(updated)
            try {
                val updateMap = hashMapOf<String, Any>(
                    "status" to OrderStatus.PAID.name,
                    "totalPrice" to finalTotal,
                    "serviceFee" to finalFee
                )
                firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("orders")?.document(order.id)?.update(updateMap)
            } catch (_: Exception) {}
        }
    }

    suspend fun updateOrderStatus(orderId: String, currentOrder: OrderRecord, newStatus: OrderStatus) {
        val updated = currentOrder.copy(status = newStatus).toEntity()
        dao.updateOrder(updated)
        try {
            firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("orders")?.document(orderId)?.update("status", newStatus.name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteOrder(order: OrderRecord) {
        dao.deleteOrder(order.toEntity())
        try {
            firestore?.collection("cafeterias")?.document(currentCafeteriaId)?.collection("orders")?.document(order.id)?.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun generateInitialTables(count: Int = 8) {
        val newTables = (1..count).map { number ->
            val secToken = "SEC" + (1000 + number)
            val payload = buildTableQrUrl(baseWebMenuUrl, number, secToken)
            TableEntity(
                id = "table_$number",
                tableNumber = number,
                label = "طاولة $number",
                capacity = if (number % 2 == 0) 4 else 2,
                qrPayload = payload,
                securityToken = secToken
            )
        }
        dao.insertTables(newTables)
    }

    suspend fun updateExistingTablesCafeteriaId() {
        try {
            val currentTables = dao.getAllTablesSync()
            currentTables.forEach { table ->
                val newPayload = buildTableQrUrl(baseWebMenuUrl, table.tableNumber, table.securityToken)
                dao.updateTable(table.copy(qrPayload = newPayload))
            }
        } catch (_: Exception) {}
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

        return try {
            val auth = firebaseAuth
            val authResult = auth.signInWithEmailAndPassword(cleanEmail, cleanPass).awaitTask()
            val firebaseUser = authResult.user ?: return AuthResult.Error("تعذر التحقق من المستخدم في Firebase")

            // Reload to refresh isEmailVerified from Firebase
            firebaseUser.reload().awaitTask()

            if (!firebaseUser.isEmailVerified) {
                auth.signOut()
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
            val msg = e.message ?: ""
            val errorMsg = when {
                e is FirebaseAuthInvalidUserException -> "لا يوجد حساب مسجل بهذا البريد الإلكتروني في Firebase"
                e is FirebaseAuthInvalidCredentialsException -> "البريد الإلكتروني أو كلمة المرور غير صحيحة"
                msg.contains("operation-not-allowed", ignoreCase = true) || msg.contains("provider is not enabled", ignoreCase = true) ->
                    "يرجى تفعيل (Email/Password) في لوحة Firebase: Authentication -> Sign-in method"
                else -> e.localizedMessage ?: "فشل تسجيل الدخول في Firebase: تأكد من اتصال الإنترنت"
            }
            AuthResult.Error(errorMsg)
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

        return try {
            val auth = firebaseAuth
            val authResult = auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).awaitTask()
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
            auth.signOut()

            AuthResult.VerificationEmailSent(
                email = cleanEmail,
                message = "تم إنشاء الحساب في Firebase بنجاح!\n\nتم إرسال رابط تأكيد إلى بريدك الإلكتروني ($cleanEmail).\n\nيرجى فتح بريدك الإلكتروني والضغط على رابط التحقق قبل تسجيل الدخول."
            )
        } catch (e: Exception) {
            val msg = e.message ?: ""
            val errorMsg = when {
                e is FirebaseAuthUserCollisionException -> "البريد الإلكتروني مسجل بالفعل في Firebase، يرجى تسجيل الدخول"
                e is FirebaseAuthWeakPasswordException -> "كلمة المرور ضعيفة جداً في Firebase (يجب أن تكون 6 أحرف على الأقل)"
                e is FirebaseAuthInvalidCredentialsException -> "صيغة البريد الإلكتروني غير صحيحة"
                msg.contains("operation-not-allowed", ignoreCase = true) || msg.contains("provider is not enabled", ignoreCase = true) ->
                    "يرجى تفعيل (Email/Password) في لوحة Firebase: Authentication -> Sign-in method"
                else -> "خطأ في Firebase أثناء إنشاء الحساب: ${e.localizedMessage ?: e.javaClass.simpleName}"
            }
            AuthResult.Error(errorMsg)
        }
    }

    suspend fun resendEmailVerification(email: String, password: String? = null): AuthResult {
        val cleanEmail = email.trim()
        return try {
            val auth = firebaseAuth
            var currentUser = auth.currentUser
            if ((currentUser == null || currentUser.email != cleanEmail) && !password.isNullOrBlank()) {
                val res = auth.signInWithEmailAndPassword(cleanEmail, password.trim()).awaitTask()
                currentUser = res.user
            }
            if (currentUser != null) {
                currentUser.sendEmailVerification().awaitTask()
                auth.signOut()
                AuthResult.VerificationEmailSent(
                    email = cleanEmail,
                    message = "تمت إعادة إرسال رابط التفعيل إلى $cleanEmail بنجاح! تفقد بريدك الوارد والبريد غير الهام (Spam)."
                )
            } else {
                AuthResult.Error("يرجى إدخال كلمة المرور مع البريد لإعادة إرسال الرابط")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "تعذر إعادة إرسال رابط التفعيل في Firebase")
        }
    }

    suspend fun sendPasswordReset(email: String): AuthResult {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return AuthResult.Error("يرجى إدخال بريد إلكتروني صالح")
        }
        return try {
            firebaseAuth.sendPasswordResetEmail(cleanEmail).awaitTask()
            AuthResult.VerificationEmailSent(
                email = cleanEmail,
                message = "تم إرسال رابط إعادة تعيين كلمة المرور إلى $cleanEmail بنجاح! تفقد بريدك الإلكتروني واتبع الرابط لتغيير كلمة المرور."
            )
        } catch (e: Exception) {
            val errorMsg = when (e) {
                is FirebaseAuthInvalidUserException -> "لا يوجد حساب مسجل بهذا البريد الإلكتروني في Firebase"
                is FirebaseAuthInvalidCredentialsException -> "صيغة البريد الإلكتروني غير صحيحة"
                else -> e.localizedMessage ?: "فشل إرسال رابط إعادة تعيين كلمة المرور في Firebase"
            }
            AuthResult.Error(errorMsg)
        }
    }

    fun stopFirestoreOrdersListener() {
        ordersListenerRegistration?.remove()
        ordersListenerRegistration = null
    }

    fun logout() {
        stopFirestoreOrdersListener()
        try {
            firebaseAuth.signOut()
        } catch (_: Exception) {}
        currentCafeteriaId = "u_default"
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
