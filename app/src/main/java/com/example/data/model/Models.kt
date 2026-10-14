package com.example.data.model

data class UserAccount(
    val id: String,
    val fullName: String,
    val email: String,
    val cafeName: String,
    val role: String = "owner" // owner, cashier
)

data class MenuCategory(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val iconType: String, // burger, fries, drink, meals, sweets, pizza
    val sortOrder: Int = 0,
    val customImageUri: String? = null
)

data class MenuItem(
    val id: String,
    val categoryId: String,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val price: Double,
    val imageType: String = "burger", // burger, drink, fries, sweets, default
    val isAvailable: Boolean = true,
    val imageUri: String? = null,
    val bgImageUri: String? = null,
    val isOffer: Boolean = false,
    val offerDiscountPercent: Int = 0,
    val offerTag: String = "عرض خاص"
)

data class CafeTable(
    val id: String,
    val tableNumber: Int,
    val label: String,
    val capacity: Int = 4,
    val qrPayload: String
)

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int = 1,
    val notes: String = ""
)

enum class OrderStatus(val titleAr: String, val titleEn: String) {
    NEW("طلب جديد", "New"),
    PREPARING("قيد التحضير", "Preparing"),
    READY("جاهز للتقديم", "Ready"),
    PAID("تم الدفع", "Paid"),
    CANCELLED("ملغي", "Cancelled")
}

data class OrderRecord(
    val id: String,
    val tableNumber: Int,
    val customerName: String,
    val itemsSummary: String,
    val itemsCount: Int,
    val totalPrice: Double,
    val status: OrderStatus,
    val timestamp: Long,
    val notes: String = ""
)
