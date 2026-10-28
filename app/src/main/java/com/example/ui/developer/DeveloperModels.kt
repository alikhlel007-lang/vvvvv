package com.example.ui.developer

data class CafeteriaAdminSummary(
    val id: String,
    val numericId: String,
    val cafeName: String,
    val ownerName: String,
    val phone: String,
    val email: String,
    val address: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val totalSales: Double = 0.0,
    val paidOrdersCount: Int = 0,
    val serviceFeeTotal: Double = 0.0,
    val collectedFees: Double = 0.0,
    val remainingFee: Double = 0.0,
    val currency: String = "د.ع"
)

data class AdminInvoiceItem(
    val id: String,
    val invoiceCode: String,
    val tableNumber: Int,
    val customerName: String,
    val itemsSummary: String,
    val itemsCount: Int,
    val totalPrice: Double,
    val subtotal: Double,
    val serviceFee: Double,
    val currency: String,
    val status: String,
    val timestamp: Long,
    val notes: String = ""
)

data class AdminFeeCollectionRecord(
    val id: String,
    val amount: Double,
    val note: String,
    val timestamp: Long,
    val currency: String = "د.ع"
)

enum class DeveloperScreen {
    LOGIN,
    DASHBOARD,
    INVOICES
}
