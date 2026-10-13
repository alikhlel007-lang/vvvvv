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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class CafeRepository(private val dao: CafeDao) {

    // Categories
    val categories: Flow<List<MenuCategory>> = dao.getAllCategories().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun addCategory(nameAr: String, nameEn: String, iconType: String) {
        val entity = CategoryEntity(
            id = "cat_" + UUID.randomUUID().toString().take(8),
            nameAr = nameAr,
            nameEn = nameEn,
            iconType = iconType,
            sortOrder = 99
        )
        dao.insertCategory(entity)
    }

    suspend fun updateCategory(category: MenuCategory) {
        dao.updateCategory(category.toEntity())
    }

    suspend fun deleteCategory(category: MenuCategory) {
        dao.deleteCategory(category.toEntity())
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
        imageType: String = "burger"
    ) {
        val entity = MenuItemEntity(
            id = "item_" + UUID.randomUUID().toString().take(8),
            categoryId = categoryId,
            nameAr = nameAr,
            nameEn = nameEn,
            descriptionAr = descriptionAr,
            descriptionEn = descriptionEn,
            price = price,
            imageType = imageType,
            isAvailable = true
        )
        dao.insertMenuItem(entity)
    }

    suspend fun updateMenuItem(item: MenuItem) {
        dao.updateMenuItem(item.toEntity())
    }

    suspend fun deleteMenuItem(item: MenuItem) {
        dao.deleteMenuItem(item.toEntity())
    }

    // Tables
    val tables: Flow<List<CafeTable>> = dao.getAllTables().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun addTable(number: Int, label: String, capacity: Int, cafeName: String) {
        val entity = TableEntity(
            id = "table_$number",
            tableNumber = number,
            label = label.ifEmpty { "طاولة $number" },
            capacity = capacity,
            qrPayload = "https://cafe.menu/table?id=$number&cafe=${cafeName.replace(" ", "_")}"
        )
        dao.insertTable(entity)
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
        val total = cartItems.sumOf { it.menuItem.price * it.quantity }
        val orderId = "ord_" + System.currentTimeMillis().toString().takeLast(6)

        val entity = OrderEntity(
            id = orderId,
            tableNumber = tableNumber,
            customerName = customerName.ifEmpty { "طاولة $tableNumber" },
            itemsSummary = summary,
            itemsCount = count,
            totalPrice = total,
            status = OrderStatus.NEW.name,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        dao.insertOrder(entity)
        return orderId
    }

    suspend fun updateOrderStatus(orderId: String, currentOrder: OrderRecord, newStatus: OrderStatus) {
        val updated = currentOrder.copy(status = newStatus).toEntity()
        dao.updateOrder(updated)
    }

    suspend fun deleteOrder(order: OrderRecord) {
        dao.deleteOrder(order.toEntity())
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
        sortOrder = sortOrder
    )

    private fun MenuCategory.toEntity() = CategoryEntity(
        id = id,
        nameAr = nameAr,
        nameEn = nameEn,
        iconType = iconType,
        sortOrder = sortOrder
    )

    private fun MenuItemEntity.toDomain() = MenuItem(
        id = id,
        categoryId = categoryId,
        nameAr = nameAr,
        nameEn = nameEn,
        descriptionAr = descriptionAr,
        descriptionEn = descriptionEn,
        price = price,
        imageType = imageType,
        isAvailable = isAvailable
    )

    private fun MenuItem.toEntity() = MenuItemEntity(
        id = id,
        categoryId = categoryId,
        nameAr = nameAr,
        nameEn = nameEn,
        descriptionAr = descriptionAr,
        descriptionEn = descriptionEn,
        price = price,
        imageType = imageType,
        isAvailable = isAvailable
    )

    private fun TableEntity.toDomain() = CafeTable(
        id = id,
        tableNumber = tableNumber,
        label = label,
        capacity = capacity,
        qrPayload = qrPayload
    )

    private fun CafeTable.toEntity() = TableEntity(
        id = id,
        tableNumber = tableNumber,
        label = label,
        capacity = capacity,
        qrPayload = qrPayload
    )

    private fun OrderEntity.toDomain() = OrderRecord(
        id = id,
        tableNumber = tableNumber,
        customerName = customerName,
        itemsSummary = itemsSummary,
        itemsCount = itemsCount,
        totalPrice = totalPrice,
        status = try {
            OrderStatus.valueOf(status)
        } catch (_: Exception) {
            OrderStatus.NEW
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
