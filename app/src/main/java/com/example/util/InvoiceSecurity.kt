package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlin.math.abs

object InvoiceSecurity {
    const val DEVELOPER_PHONE = "07734524730"
    const val DEVELOPER_WHATSAPP_INTERNATIONAL = "9647734524730"
    const val MASTER_OVERRIDE_PIN = "132132"

    /**
     * حساب تجزئة 32 بت مطابق لخوارزمية جافا وسكربت
     */
    private fun stringHashCode(s: String): Int {
        var h = 0
        for (i in 0 until s.length) {
            h = 31 * h + s[i].code
        }
        return h
    }

    /**
     * توليد كود أمان فريد مكون من 6 أرقام لكل فاتورة بناءً على معرف الفاتورة وخوارزمية سرية
     */
    fun generateInvoiceCode(orderId: String): String {
        val hash = abs(stringHashCode(orderId + "_qcafe_security_132"))
        val codeNum = 100000 + (hash % 900000)
        return codeNum.toString()
    }

    /**
     * التحقق من صحة الكود المدخل (إما كود الفاتورة المعتمد أو كود المطور العام 132132)
     */
    fun verifyCode(orderId: String, enteredCode: String): Boolean {
        val clean = enteredCode.trim()
        if (clean == MASTER_OVERRIDE_PIN) return true
        return clean == generateInvoiceCode(orderId)
    }

    /**
     * فتح محادثة واتساب مع المطور مباشرة مع رسالة مجهزة ببيانات الفاتورة
     */
    fun openWhatsAppRequest(
        context: Context,
        cafeName: String,
        tableNumber: Int,
        orderId: String,
        amount: Double,
        currency: String
    ) {
        val msg = "مرحباً إدارة Q-Cafe 👋\n" +
                "أرغب بطلب كود موافقة لحذف فاتورة مسددة:\n" +
                "• الكافتيريا: $cafeName\n" +
                "• طاولة: $tableNumber\n" +
                "• رقم الفاتورة: $orderId\n" +
                "• المبلغ: ${OrderFeeCalculator.formatPrice(amount, currency)}\n" +
                "يرجى تزويدي بكود الموافقة للحذف وشكراً."

        try {
            val encoded = Uri.encode(msg)
            val uri = Uri.parse("https://wa.me/$DEVELOPER_WHATSAPP_INTERNATIONAL?text=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to normal dialer or browser
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$DEVELOPER_WHATSAPP_INTERNATIONAL"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    /**
     * فتح محادثة واتساب مع المطور لطلب كود الموافقة على مسح وتصفير كافة الفواتير المسددة
     */
    fun openWhatsAppBulkDeleteRequest(
        context: Context,
        cafeName: String,
        invoiceCount: Int,
        totalSalesText: String
    ) {
        val msg = "مرحباً إدارة Q-Cafe 👋\n" +
                "أرغب بطلب كود موافقة لتصفير وحذف كافة الفواتير المسددة في الأرشيف:\n" +
                "• الكافتيريا: $cafeName\n" +
                "• عدد الفواتير: $invoiceCount فاتورة\n" +
                "• إجمالي المبيعات: $totalSalesText\n" +
                "يرجى تزويدي بكود الموافقة الشامل (Master Code) وشكراً."

        try {
            val encoded = Uri.encode(msg)
            val uri = Uri.parse("https://wa.me/$DEVELOPER_WHATSAPP_INTERNATIONAL?text=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$DEVELOPER_WHATSAPP_INTERNATIONAL"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }
}
