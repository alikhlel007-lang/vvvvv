package com.example.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.model.OrderRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptPrinter {

    /**
     * طباعة فاتورة الحساب رسمياً عبر حوار الطباعة لنظام أندرويد
     * يدعم كافة الطابعات الحرارية وطابعات البلوتوث والواي فاي وحفظ PDF
     */
    fun printInvoice(
        context: Context,
        tableNumber: Int,
        orders: List<OrderRecord>,
        cafeName: String
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return

        val webView = WebView(context)
        val htmlContent = buildInvoiceHtml(tableNumber, orders, cafeName)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printAdapter = webView.createPrintDocumentAdapter("فاتورة_طاولة_$tableNumber")
                printManager.print(
                    "فاتورة طاولة $tableNumber",
                    printAdapter,
                    PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A6)
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                )
            }
        }

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    private fun buildInvoiceHtml(
        tableNumber: Int,
        orders: List<OrderRecord>,
        cafeName: String
    ): String {
        val currency = orders.firstOrNull()?.currency ?: "د.ع"
        val subtotal = orders.sumOf { it.subtotal }
        val rawFee = OrderFeeCalculator.calculateServiceFee(subtotal, currency)
        val settlementFee = OrderFeeCalculator.calculateSettlementFee(subtotal, currency)
        val settlementTotal = OrderFeeCalculator.calculateSettlementTotal(subtotal, currency)

        val parsedItems = OrderSummaryHelper.parseOrderItems(orders.map { it.itemsSummary })
        val customerNames = orders.map { it.customerName }.distinct().joinToString("، ")

        val timeStr = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault()).format(Date())

        val itemsHtml = StringBuilder()
        parsedItems.forEach { item ->
            val priceStr = if (item.totalPrice > 0.0) OrderFeeCalculator.formatPrice(item.totalPrice, item.currency) else "-"
            itemsHtml.append("""
                <tr>
                    <td style="padding: 6px 4px; text-align: right; font-weight: bold; border-bottom: 1px dashed #ddd;">${item.name}</td>
                    <td style="padding: 6px 4px; text-align: center; border-bottom: 1px dashed #ddd;">${item.quantity}</td>
                    <td style="padding: 6px 4px; text-align: left; font-weight: bold; border-bottom: 1px dashed #ddd;">$priceStr</td>
                </tr>
            """.trimIndent())
        }

        return """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="utf-8">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        padding: 12px;
                        color: #111;
                        line-height: 1.4;
                        margin: 0;
                        background: #fff;
                    }
                    .header {
                        text-align: center;
                        border-bottom: 2px solid #222;
                        padding-bottom: 10px;
                        margin-bottom: 10px;
                    }
                    .cafe-name {
                        font-size: 20px;
                        font-weight: 900;
                        margin-bottom: 4px;
                    }
                    .meta {
                        font-size: 12px;
                        color: #555;
                    }
                    .table-badge {
                        display: inline-block;
                        background: #222;
                        color: #fff;
                        padding: 4px 12px;
                        border-radius: 8px;
                        font-size: 14px;
                        font-weight: bold;
                        margin-top: 6px;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin: 12px 0;
                        font-size: 13px;
                    }
                    th {
                        background: #f0f0f0;
                        padding: 6px 4px;
                        border-bottom: 1.5px solid #222;
                        font-size: 12px;
                    }
                    .totals {
                        margin-top: 10px;
                        border-top: 1.5px solid #222;
                        padding-top: 8px;
                        font-size: 13px;
                    }
                    .total-row {
                        display: flex;
                        justify-content: space-between;
                        margin-bottom: 4px;
                    }
                    .grand-total {
                        font-size: 16px;
                        font-weight: 900;
                        border-top: 1.5px dashed #222;
                        padding-top: 6px;
                        margin-top: 6px;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 16px;
                        font-size: 11px;
                        color: #666;
                        border-top: 1px dashed #bbb;
                        padding-top: 8px;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="cafe-name">$cafeName</div>
                    <div class="meta">فاتورة حساب رسمية</div>
                    <div class="meta">$timeStr</div>
                    <div class="table-badge">طاولة رقم $tableNumber</div>
                </div>

                <div style="font-size: 12px; margin-bottom: 6px; color: #444;">
                    <strong>الزبون:</strong> $customerNames
                </div>

                <table>
                    <thead>
                        <tr>
                            <th style="text-align: right;">الصنف</th>
                            <th style="text-align: center; width: 45px;">العدد</th>
                            <th style="text-align: left; width: 85px;">السعر</th>
                        </tr>
                    </thead>
                    <tbody>
                        $itemsHtml
                    </tbody>
                </table>

                <div class="totals">
                    <div class="total-row">
                        <span>مجموع الطلبات:</span>
                        <span>${OrderFeeCalculator.formatPrice(subtotal, currency)}</span>
                    </div>
                    <div class="total-row">
                        <span>رسوم الخدمة 1%:</span>
                        <span>${OrderFeeCalculator.formatPrice(rawFee, currency)}</span>
                    </div>
                    <div class="total-row">
                        <span>رسوم الخدمة عند التسديد:</span>
                        <span>${OrderFeeCalculator.formatPrice(settlementFee, currency)}</span>
                    </div>
                    <div class="total-row grand-total">
                        <span>الإجمالي المسدد:</span>
                        <span>${OrderFeeCalculator.formatPrice(settlementTotal, currency)}</span>
                    </div>
                </div>

                <div class="footer">
                    <div>شكراً لزيارتكم الكريمة! نتشرف بحضوركم دائماً ✨</div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
