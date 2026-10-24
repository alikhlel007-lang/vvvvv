package com.example.util

object OrderSummaryHelper {

    /**
     * تجميع الأصناف وضرب الكميات المتكررة لنفس الزبون تلقائياً
     * مثال: "1x برجر كلاسيك" + "1x برجر كلاسيك" => "2x برجر كلاسيك"
     */
    fun combineAndMultiplySummaries(summaries: List<String>): String {
        if (summaries.isEmpty()) return ""
        val itemMap = linkedMapOf<String, Int>()

        summaries.forEach { summary ->
            if (summary.isBlank()) return@forEach
            // تقسيم حسب الفاصلة العربية أو الإنجليزية أو علامة الزائد
            val tokens = summary.split(Regex("[،,+\n]"))
            for (rawToken in tokens) {
                val token = rawToken.trim()
                if (token.isBlank()) continue

                // البحث عن نمط "2x اسم الصنف" أو "2X اسم الصنف" أو "2× اسم الصنف" أو "2* اسم الصنف"
                val match = Regex("""^(\d+)\s*[xX×*]\s*(.+)$""").find(token)
                if (match != null) {
                    val count = match.groupValues[1].toIntOrNull() ?: 1
                    val name = match.groupValues[2].trim()
                    if (name.isNotBlank()) {
                        itemMap[name] = (itemMap[name] ?: 0) + count
                    }
                } else {
                    itemMap[token] = (itemMap[token] ?: 0) + 1
                }
            }
        }

        if (itemMap.isEmpty()) return ""
        return itemMap.entries.joinToString("، ") { (name, count) ->
            "${count}x $name"
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
