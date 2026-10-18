package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val cafeName: String,
    val role: String
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val nameAr: String,
    val nameEn: String,
    val iconType: String,
    val sortOrder: Int,
    val customImageUri: String? = null
)

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val price: Double,
    val currency: String = "د.ع",
    val imageType: String,
    val isAvailable: Boolean,
    val imageUri: String? = null,
    val bgImageUri: String? = null,
    val galleryImagesStr: String = "",
    val isOffer: Boolean = false,
    val offerDiscountPercent: Int = 0,
    val offerTag: String = "عرض خاص"
)

@Entity(tableName = "cafe_tables")
data class TableEntity(
    @PrimaryKey val id: String,
    val tableNumber: Int,
    val label: String,
    val capacity: Int,
    val qrPayload: String,
    val securityToken: String = ""
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val tableNumber: Int,
    val customerName: String,
    val itemsSummary: String,
    val itemsCount: Int,
    val totalPrice: Double,
    val subtotal: Double = 0.0,
    val serviceFee: Double = 0.0,
    val currency: String = "د.ع",
    val status: String,
    val timestamp: Long,
    val notes: String
)
