package com.example.ui.developer

import com.example.util.InvoiceSecurity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DeveloperRepository {

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (_: Exception) {
            null
        }

    private val auth: FirebaseAuth
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

    suspend fun fetchAllCafeterias(): List<CafeteriaAdminSummary> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext emptyList()
        val list = mutableListOf<CafeteriaAdminSummary>()

        try {
            val seenIds = mutableSetOf<String>()
            val cafeSnap = db.collection("cafeterias").get().awaitTask()

            for (doc in cafeSnap.documents) {
                val id = doc.id
                if (id.isBlank() || id == "u_default" || seenIds.contains(id)) continue
                seenIds.add(id)

                val storedNumId = doc.getString("numericId")
                val numericId = if (!storedNumId.isNullOrBlank()) {
                    storedNumId
                } else {
                    val generated = InvoiceSecurity.generateCafeNumericId(id)
                    try {
                        db.collection("cafeterias").document(id)
                            .set(mapOf("numericId" to generated), SetOptions.merge())
                    } catch (_: Exception) {}
                    generated
                }

                val cafeName = doc.getString("cafeName") ?: doc.getString("name") ?: "كافتيريا $numericId"
                val ownerName = doc.getString("ownerName") ?: doc.getString("fullName") ?: "غير محدد"
                val phone = doc.getString("phone") ?: ""
                val email = doc.getString("email") ?: ""
                val address = doc.getString("address") ?: ""
                val isActive = doc.getBoolean("isActive") ?: true
                val createdAt = doc.getLong("createdAt") ?: 0L

                // Fetch orders for this cafeteria to calculate totals
                var totalSales = 0.0
                var paidCount = 0
                var currency = "د.ع"
                try {
                    val ordersSnap = db.collection("cafeterias").document(id).collection("orders").get().awaitTask()
                    for (orderDoc in ordersSnap.documents) {
                        val status = orderDoc.getString("status") ?: ""
                        if (status.equals("PAID", ignoreCase = true) || status == "الفواتير المسددة") {
                            paidCount++
                            val total = orderDoc.getDouble("totalPrice")
                                ?: orderDoc.getDouble("subtotal")
                                ?: 0.0
                            totalSales += total
                            orderDoc.getString("currency")?.let { if (it.isNotBlank()) currency = it }
                        }
                    }
                } catch (_: Exception) {}

                // Fetch fee collections
                var collectedFees = 0.0
                try {
                    val collectionsSnap = db.collection("cafeterias").document(id).collection("fee_collections").get().awaitTask()
                    for (colDoc in collectionsSnap.documents) {
                        val amt = colDoc.getDouble("amount") ?: 0.0
                        collectedFees += amt
                    }
                } catch (_: Exception) {}

                val serviceFeeTotal = totalSales * 0.01
                val remainingFee = (serviceFeeTotal - collectedFees).coerceAtLeast(0.0)

                list.add(
                    CafeteriaAdminSummary(
                        id = id,
                        numericId = numericId,
                        cafeName = cafeName,
                        ownerName = ownerName,
                        phone = phone,
                        email = email,
                        address = address,
                        isActive = isActive,
                        createdAt = createdAt,
                        totalSales = totalSales,
                        paidOrdersCount = paidCount,
                        serviceFeeTotal = serviceFeeTotal,
                        collectedFees = collectedFees,
                        remainingFee = remainingFee,
                        currency = currency
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        list.sortedByDescending { it.totalSales }
    }

    suspend fun fetchCafeInvoices(cafeId: String): List<AdminInvoiceItem> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext emptyList()
        val list = mutableListOf<AdminInvoiceItem>()

        try {
            val snap = db.collection("cafeterias").document(cafeId).collection("orders").get().awaitTask()
            for (doc in snap.documents) {
                val id = doc.id
                val tableNumber = doc.getLong("tableNumber")?.toInt() ?: 1
                val customerName = doc.getString("customerName") ?: "طاولة $tableNumber"
                val itemsSummary = doc.getString("itemsSummary") ?: ""
                val itemsCount = doc.getLong("itemsCount")?.toInt() ?: 1
                val totalPrice = doc.getDouble("totalPrice") ?: 0.0
                val subtotal = doc.getDouble("subtotal") ?: totalPrice
                val serviceFee = doc.getDouble("serviceFee") ?: 0.0
                val currency = doc.getString("currency") ?: "د.ع"
                val status = doc.getString("status") ?: "PAID"
                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                val notes = doc.getString("notes") ?: ""
                val code = InvoiceSecurity.generateInvoiceCode(id)

                list.add(
                    AdminInvoiceItem(
                        id = id,
                        invoiceCode = code,
                        tableNumber = tableNumber,
                        customerName = customerName,
                        itemsSummary = itemsSummary,
                        itemsCount = itemsCount,
                        totalPrice = totalPrice,
                        subtotal = subtotal,
                        serviceFee = serviceFee,
                        currency = currency,
                        status = status,
                        timestamp = timestamp,
                        notes = notes
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        list.sortedByDescending { it.timestamp }
    }

    suspend fun deleteInvoice(cafeId: String, orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("قاعدة البيانات غير متصلة"))
        try {
            db.collection("cafeterias").document(cafeId).collection("orders").document(orderId).delete().awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllInvoicesForCafe(cafeId: String): Result<Int> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("قاعدة البيانات غير متصلة"))
        try {
            val snap = db.collection("cafeterias").document(cafeId).collection("orders").get().awaitTask()
            var count = 0
            for (doc in snap.documents) {
                doc.reference.delete().awaitTask()
                count++
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleCafeStatus(cafeId: String, currentActive: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("قاعدة البيانات غير متصلة"))
        val newStatus = !currentActive
        try {
            db.collection("cafeterias").document(cafeId)
                .set(mapOf("isActive" to newStatus), SetOptions.merge())
                .awaitTask()
            Result.success(newStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordFeeCollection(cafeId: String, amount: Double, note: String): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("قاعدة البيانات غير متصلة"))
        try {
            val record = hashMapOf(
                "amount" to amount,
                "note" to note.trim(),
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("cafeterias").document(cafeId).collection("fee_collections").add(record).awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchFeeCollections(cafeId: String): List<AdminFeeCollectionRecord> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext emptyList()
        val list = mutableListOf<AdminFeeCollectionRecord>()
        try {
            val snap = db.collection("cafeterias").document(cafeId).collection("fee_collections").get().awaitTask()
            for (doc in snap.documents) {
                val amount = doc.getDouble("amount") ?: 0.0
                val note = doc.getString("note") ?: ""
                val timestamp = doc.getLong("timestamp") ?: 0L
                list.add(
                    AdminFeeCollectionRecord(
                        id = doc.id,
                        amount = amount,
                        note = note,
                        timestamp = timestamp
                    )
                )
            }
        } catch (_: Exception) {}
        list.sortedByDescending { it.timestamp }
    }
}
