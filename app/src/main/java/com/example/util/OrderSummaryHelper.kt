package com.example.util

data class ParsedOrderItem(
    val name: String,
    val quantity: Int,
    val totalPrice: Double,
    val currency: String
)

object OrderSummaryHelper {

    private data class ItemData(
        var quantity: Int,
        var totalPrice: Double,
        var currency: String
    )

    /**
     * Format a cart item or line item into summary text:
     * e.g. "2x برجر كلاسيك بسعر 14,000 د.ع"
     */
    fun formatLineItem(name: String, quantity: Int, totalPrice: Double, currency: String = "د.ع"): String {
        val formattedPrice = OrderFeeCalculator.formatPrice(totalPrice, currency)
        return "${quantity}x $name بسعر $formattedPrice"
    }

    /**
     * تفكيك وتجميع الأصناف في كائنات مهيكلة لعرضها كصفوف منسقة عند الكاشير
     * [اسم الصنف]   [عدد X]   [السعر X]
     */
    fun parseOrderItems(summaries: List<String>): List<ParsedOrderItem> {
        if (summaries.isEmpty()) return emptyList()
        val itemMap = linkedMapOf<String, ItemData>()

        summaries.forEach { summary ->
            if (summary.isBlank()) return@forEach
            // تقسيم حسب الفاصلة العربية أو الإنجليزية أو علامة الزائد أو السطر الجديد
            val tokens = summary.split(Regex("[،,+\n]"))
            for (rawToken in tokens) {
                val token = rawToken.trim()
                if (token.isBlank()) continue

                // البحث عن نمط يحتوي على السعر: "2x برجر كلاسيك بسعر 14,000 د.ع"
                val matchWithPrice = Regex("""^(\d+)\s*[xX×*]?\s*(.+?)\s+بسعر\s+([\d,.]+)\s*(د\.ع|\$|USD)?$""").find(token)
                if (matchWithPrice != null) {
                    val count = matchWithPrice.groupValues[1].toIntOrNull() ?: 1
                    val name = matchWithPrice.groupValues[2].trim()
                    val priceStr = matchWithPrice.groupValues[3].replace(",", "").trim()
                    val priceVal = priceStr.toDoubleOrNull() ?: 0.0
                    val curr = matchWithPrice.groupValues[4].ifBlank { "د.ع" }

                    val existing = itemMap[name]
                    if (existing != null) {
                        existing.quantity += count
                        existing.totalPrice += priceVal
                    } else {
                        itemMap[name] = ItemData(count, priceVal, curr)
                    }
                } else {
                    // نمط بديل بدون سعر: "2x برجر كلاسيك"
                    val matchNoPrice = Regex("""^(\d+)\s*[xX×*]\s*(.+)$""").find(token)
                    if (matchNoPrice != null) {
                        val count = matchNoPrice.groupValues[1].toIntOrNull() ?: 1
                        val name = matchNoPrice.groupValues[2].trim()
                        val existing = itemMap[name]
                        if (existing != null) {
                            existing.quantity += count
                        } else {
                            itemMap[name] = ItemData(count, 0.0, "د.ع")
                        }
                    } else {
                        val existing = itemMap[token]
                        if (existing != null) {
                            existing.quantity += 1
                        } else {
                            itemMap[token] = ItemData(1, 0.0, "د.ع")
                        }
                    }
                }
            }
        }

        return itemMap.map { (name, data) ->
            ParsedOrderItem(name, data.quantity, data.totalPrice, data.currency)
        }
    }

    /**
     * تجميع الأصناف وضرب الكميات والأسعار المتكررة لنفس الزبون تلقائياً
     * مثال: "1x برجر كلاسيك بسعر 7,000 د.ع" + "1x برجر كلاسيك بسعر 7,000 د.ع" => "2x برجر كلاسيك بسعر 14,000 د.ع"
     */
    fun combineAndMultiplySummaries(summaries: List<String>): String {
        val items = parseOrderItems(summaries)
        if (items.isEmpty()) return ""
        return items.joinToString("، ") { item ->
            if (item.totalPrice > 0.0) {
                "${item.quantity}x ${item.name} بسعر ${OrderFeeCalculator.formatPrice(item.totalPrice, item.currency)}"
            } else {
                "${item.quantity}x ${item.name}"
            }
        }
    }

    /**
     * دمج ملاحظات الطلبات المتعددة لنفس الزبون بدون تكرار
     */
    fun combineNotes(notes: List<String>): String {
        return notes
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" | ")
    }
}
