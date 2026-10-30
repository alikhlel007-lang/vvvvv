package com.example.ui.cashier

import android.widget.Toast
import com.example.ui.components.InvoiceDeletionDialog
import com.example.ui.components.MenuCashierSwitcher
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStatus
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.CafeCardBorder
import com.example.ui.theme.CafeCardBorderGlow
import com.example.ui.theme.CafeCyanPrimary
import com.example.ui.theme.CafeDarkBg
import com.example.ui.theme.CafeDarkBgGradientEnd
import com.example.ui.theme.CafeGold
import com.example.ui.theme.CafeGoldLight
import com.example.ui.theme.CafeSurface
import com.example.ui.theme.CafeSurfaceVariant
import com.example.ui.theme.CafeTextPrimary
import com.example.ui.theme.CafeTextSecondary
import com.example.ui.theme.OrderStatusCancelled
import com.example.ui.theme.OrderStatusNew
import com.example.ui.theme.OrderStatusPaid
import com.example.ui.theme.OrderStatusPreparing
import com.example.ui.theme.OrderStatusReady
import com.example.ui.theme.OrderStatusDelivered
import com.example.ui.theme.OrderStatusRejected
import com.example.util.OrderFeeCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * نموذج جلسة وفاتورة الطاولة الموحدة (فاتورة واحدة لكل جلسة)
 */
data class TableSession(
    val tableNumber: Int,
    val orders: List<OrderRecord>,
    val stage: OrderStatus,
    val lastTimestamp: Long,
    val hasPreparingOrder: Boolean = false,
    val hasDeliveredOrder: Boolean = false,
    val preparingOrders: List<OrderRecord> = emptyList(),
    val otherTableOrders: List<OrderRecord> = emptyList(),
    val sessionId: String = ""
)

@Composable
fun CashierOrdersScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val orders by viewModel.orders.collectAsState()
    val activeFilter by viewModel.orderStatusFilter.collectAsState()

    // تجميع الطلبات في جلسات موحدة حسب المراحل المستقلة لكل طاولة
    // بعد التسديد تعود الطاولة نظيفة وجاهزة تماماً لاستقبال طلبات جديدة
    var showActiveTablesDialog by remember { mutableStateOf(false) }
    var showRevenueDetailsDialog by remember { mutableStateOf(false) }
    var orderForAuthorizedDeletion by remember { mutableStateOf<com.example.data.model.OrderRecord?>(null) }

    // تجميع الطلبات في جلسات ومحطات مستقلة لكل طاولة
    val tableSessions = remember(orders) {
        val byTable = orders.groupBy { it.tableNumber }
        val sessionsList = mutableListOf<TableSession>()

        byTable.forEach { (tableNumber, tableOrders) ->
            val activeOrders = tableOrders.filter {
                it.status != OrderStatus.PAID &&
                it.status != OrderStatus.CANCELLED &&
                it.status != OrderStatus.REJECTED
            }

            val newOrders = activeOrders.filter { it.status == OrderStatus.NEW }
            val prepOrders = activeOrders.filter { it.status == OrderStatus.PREPARING }
            val delivOrders = activeOrders.filter { it.status == OrderStatus.DELIVERED || it.status == OrderStatus.READY }

            // 1. طلبات جديدة (NEW) - كل طلب جديد يظهر كبطاقة مستقلة مع أزرار (رفض / دمج / أوافق)
            newOrders.forEach { newOrder ->
                val otherNew = newOrders.filter { it.id != newOrder.id }
                sessionsList.add(
                    TableSession(
                        tableNumber = tableNumber,
                        orders = listOf(newOrder),
                        stage = OrderStatus.NEW,
                        lastTimestamp = newOrder.timestamp,
                        hasPreparingOrder = prepOrders.isNotEmpty(),
                        hasDeliveredOrder = delivOrders.isNotEmpty(),
                        preparingOrders = prepOrders,
                        otherTableOrders = (otherNew + prepOrders),
                        sessionId = "table_${tableNumber}_new_${newOrder.id}"
                    )
                )
            }

            // 2. قيد التحضير (PREPARING) - يظهر كل طلب تحضير كبطاقة مستقلة لنفس الطاولة لتبدو كأنها لطاولات مختلفة
            prepOrders.forEach { prepOrder ->
                val otherPreps = prepOrders.filter { it.id != prepOrder.id }
                sessionsList.add(
                    TableSession(
                        tableNumber = tableNumber,
                        orders = listOf(prepOrder),
                        stage = OrderStatus.PREPARING,
                        lastTimestamp = prepOrder.timestamp,
                        hasPreparingOrder = otherPreps.isNotEmpty(),
                        hasDeliveredOrder = delivOrders.isNotEmpty(),
                        preparingOrders = otherPreps,
                        otherTableOrders = otherPreps,
                        sessionId = "table_${tableNumber}_prep_${prepOrder.id}"
                    )
                )
            }

            // 3. تسديد حساب الطاولة (DELIVERED) - تجميع كامل طلبات الطاولة المسلمة في فاتورة موحدة
            if (delivOrders.isNotEmpty()) {
                sessionsList.add(
                    TableSession(
                        tableNumber = tableNumber,
                        orders = delivOrders.sortedBy { it.timestamp },
                        stage = OrderStatus.DELIVERED,
                        lastTimestamp = delivOrders.maxOf { it.timestamp },
                        hasPreparingOrder = prepOrders.isNotEmpty(),
                        hasDeliveredOrder = true,
                        preparingOrders = prepOrders,
                        otherTableOrders = emptyList(),
                        sessionId = "table_${tableNumber}_deliv_${delivOrders.maxOf { it.timestamp }}"
                    )
                )
            }

            // 4. الفواتير المسددة (PAID) - تظهر بشكل مستقل في تبويب الفواتير المسددة
            val paidOrders = tableOrders.filter { it.status == OrderStatus.PAID }
            if (paidOrders.isNotEmpty()) {
                val paidGroups = paidOrders.groupBy { it.timestamp / 900000L }
                paidGroups.forEach { (_, batch) ->
                    sessionsList.add(
                        TableSession(
                            tableNumber = tableNumber,
                            orders = batch.sortedBy { it.timestamp },
                            stage = OrderStatus.PAID,
                            lastTimestamp = batch.maxOf { it.timestamp },
                            sessionId = "table_${tableNumber}_paid_${batch.maxOf { it.timestamp }}"
                        )
                    )
                }
            }

            // 5. الطلبات المرفوضة والملغية
            val rejectedOrders = tableOrders.filter { it.status == OrderStatus.REJECTED || it.status == OrderStatus.CANCELLED }
            if (rejectedOrders.isNotEmpty()) {
                val rejGroups = rejectedOrders.groupBy { it.timestamp / 900000L }
                rejGroups.forEach { (_, batch) ->
                    sessionsList.add(
                        TableSession(
                            tableNumber = tableNumber,
                            orders = batch.sortedBy { it.timestamp },
                            stage = OrderStatus.REJECTED,
                            lastTimestamp = batch.maxOf { it.timestamp },
                            sessionId = "table_${tableNumber}_rej_${batch.maxOf { it.timestamp }}"
                        )
                    )
                }
            }
        }

        sessionsList.sortedByDescending { it.lastTimestamp }
    }

    // تصفية الجلسات حسب المرحلة المحددة
    val filteredSessions = tableSessions.filter { it.stage == activeFilter }

    // العدادات والإحصائيات
    val newSessionsCount = tableSessions.count { it.stage == OrderStatus.NEW }
    val preparingSessionsCount = tableSessions.count { it.stage == OrderStatus.PREPARING }
    val deliveredSessionsCount = tableSessions.count { it.stage == OrderStatus.DELIVERED }
    val paidSessionsCount = tableSessions.count { it.stage == OrderStatus.PAID }
    val rejectedSessionsCount = tableSessions.count { it.stage == OrderStatus.REJECTED }

    // أرقام الطاولات النشطة فعلياً
    val activeTableNumbers = remember(orders) {
        orders.filter {
            it.status != OrderStatus.PAID &&
            it.status != OrderStatus.CANCELLED &&
            it.status != OrderStatus.REJECTED
        }.map { it.tableNumber }.distinct().sorted()
    }
    val activeTablesCount = activeTableNumbers.size

    val cafeName by viewModel.cafeName.collectAsState()

    // حساب إجمالي الإيرادات المسددة (الصافي بدون النسبة + النسبة المتراكمة + الكلي مع النسبة)
    val paidOrders = orders.filter { it.status == OrderStatus.PAID }
    val paidSubtotalIqd = paidOrders.filter { it.currency != "$" && it.currency != "USD" }.sumOf { it.subtotal }
    val paidSubtotalUsd = paidOrders.filter { it.currency == "$" || it.currency == "USD" }.sumOf { it.subtotal }
    val paidFeesIqd = paidOrders.filter { it.currency != "$" && it.currency != "USD" }.sumOf { it.serviceFee }
    val paidFeesUsd = paidOrders.filter { it.currency == "$" || it.currency == "USD" }.sumOf { it.serviceFee }
    val paidTotalIqd = paidOrders.filter { it.currency != "$" && it.currency != "USD" }.sumOf { it.totalPrice }
    val paidTotalUsd = paidOrders.filter { it.currency == "$" || it.currency == "USD" }.sumOf { it.totalPrice }

    // العرض الافتراضي للمبيعات المسددة: السعر بدون النسبة (الصافي)
    val netRevenueDisplay = when {
        paidSubtotalIqd > 0 && paidSubtotalUsd > 0 ->
            "${OrderFeeCalculator.formatPrice(paidSubtotalIqd, "د.ع")} + ${OrderFeeCalculator.formatPrice(paidSubtotalUsd, "$")}"
        paidSubtotalUsd > 0 -> OrderFeeCalculator.formatPrice(paidSubtotalUsd, "$")
        else -> OrderFeeCalculator.formatPrice(paidSubtotalIqd, "د.ع")
    }

    // النسبة المتراكمة للمبيعات
    val accumulatedFeesDisplay = when {
        paidFeesIqd > 0 && paidFeesUsd > 0 ->
            "${OrderFeeCalculator.formatPrice(paidFeesIqd, "د.ع")} + ${OrderFeeCalculator.formatPrice(paidFeesUsd, "$")}"
        paidFeesUsd > 0 -> OrderFeeCalculator.formatPrice(paidFeesUsd, "$")
        else -> OrderFeeCalculator.formatPrice(paidFeesIqd, "د.ع")
    }

    // الحساب الكلي شامل النسبة
    val totalRevenueDisplay = when {
        paidTotalIqd > 0 && paidTotalUsd > 0 ->
            "${OrderFeeCalculator.formatPrice(paidTotalIqd, "د.ع")} + ${OrderFeeCalculator.formatPrice(paidTotalUsd, "$")}"
        paidTotalUsd > 0 -> OrderFeeCalculator.formatPrice(paidTotalUsd, "$")
        else -> OrderFeeCalculator.formatPrice(paidTotalIqd, "د.ع")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        CafeDarkBg,
                        CafeDarkBgGradientEnd,
                        Color(0xFF021215)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TOP BAR: اليمين (التبديل بين الكاشير والمنيو)، الوسط (اسم الكافيه)، اليسار (الترس)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // RIGHT SIDE: التبديل السريع المطابق تماماً لشاشة المنيو
                MenuCashierSwitcher(
                    activeTab = "cashier",
                    onSelectMenu = { viewModel.navigateTo(AppScreen.CUSTOMER_MENU) },
                    onSelectCashier = { /* already on cashier */ }
                )

                // CENTER: اسم الكافيه
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = cafeName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CafeTextPrimary
                    )
                    Text(
                        text = if (newSessionsCount > 0) "$newSessionsCount طلبات بانتظار التحضير" else "لوحة إدارة الطلبات والفواتير",
                        fontSize = 11.sp,
                        color = if (newSessionsCount > 0) CafeCyanPrimary else CafeGold,
                        fontWeight = FontWeight.Medium
                    )
                }

                // LEFT SIDE: Settings Gear Icon
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CafeSurfaceVariant)
                        .border(1.2.dp, CafeCyanPrimary.copy(alpha = 0.5f), CircleShape)
                        .testTag("cashier_gear_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "الإعدادات",
                        tint = CafeCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // METRIC CARDS ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "طلبات جديدة",
                    value = "$newSessionsCount",
                    valueColor = if (newSessionsCount > 0) CafeCyanPrimary else CafeTextPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setOrderStatusFilter(OrderStatus.NEW) }
                )
                MetricCard(
                    title = "طاولات نشطة",
                    value = "$activeTablesCount",
                    valueColor = CafeGold,
                    hasArrow = true,
                    modifier = Modifier.weight(1f),
                    onClick = { showActiveTablesDialog = true }
                )
                MetricCard(
                    title = "المبيعات المسددة",
                    value = netRevenueDisplay,
                    valueColor = CafeCyanPrimary,
                    hasArrow = true,
                    modifier = Modifier.weight(1.3f),
                    onClick = { showRevenueDetailsDialog = true }
                )
            }

            // نافذة حوارية: الطاولات النشطة لاختيار رقم الطاولة وتسديد حسابها مع تفاصيل كل الطلبات والأسعار
            if (showActiveTablesDialog) {
                AlertDialog(
                    onDismissRequest = { showActiveTablesDialog = false },
                    containerColor = CafeSurface,
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = CafeGold, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("الطاولات النشطة (${activeTableNumbers.size})", color = CafeTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("اختر الطاولة لتسديد حسابها مع تفاصيل المواد والأسعار المطلوبة:", fontSize = 12.sp, color = CafeTextSecondary)

                            if (activeTableNumbers.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("لا توجد طاولات نشطة حالياً. جميع الطاولات نظيفة وجاهزة لاستقبال طلبات جديدة.", color = CafeTextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                                }
                            } else {
                                activeTableNumbers.forEach { tableNum ->
                                    val tableActiveOrders = orders.filter {
                                        it.tableNumber == tableNum &&
                                        it.status != OrderStatus.PAID &&
                                        it.status != OrderStatus.CANCELLED &&
                                        it.status != OrderStatus.REJECTED
                                    }
                                    val tableCurr = tableActiveOrders.firstOrNull()?.currency ?: "د.ع"
                                    val tableSubtotal = tableActiveOrders.sumOf { it.subtotal }
                                    val tableSettlementTotal = OrderFeeCalculator.calculateSettlementTotal(tableSubtotal, tableCurr)
                                    val tableSettlementFee = OrderFeeCalculator.calculateSettlementFee(tableSubtotal, tableCurr)
                                    val customers = tableActiveOrders.map { it.customerName }.distinct()
                                    val hasDelivered = tableActiveOrders.any { it.status == OrderStatus.DELIVERED || it.status == OrderStatus.READY }
                                    val hasPrep = tableActiveOrders.any { it.status == OrderStatus.PREPARING }

                                    val stageLabel = when {
                                        hasDelivered -> "جاهز للتسديد 🍽️"
                                        hasPrep -> "قيد التحضير 🍳"
                                        else -> "طلب جديد ✨"
                                    }
                                    val stageColor = when {
                                        hasDelivered -> OrderStatusDelivered
                                        hasPrep -> OrderStatusPreparing
                                        else -> OrderStatusNew
                                    }

                                    // تفكيك واستخراج جميع المواد والأسعار المطلوبة لهذه الطاولة
                                    val parsedItems = com.example.util.OrderSummaryHelper.parseOrderItems(tableActiveOrders.map { it.itemsSummary })
                                    val combinedNotes = com.example.util.OrderSummaryHelper.combineNotes(tableActiveOrders.map { it.notes })

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = CafeSurfaceVariant),
                                        border = BorderStroke(1.2.dp, stageColor.copy(alpha = 0.6f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(CafeCyanPrimary)
                                                            .padding(horizontal = 9.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("طاولة $tableNum", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF032125))
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("${tableActiveOrders.size} طلبات", fontSize = 11.sp, color = CafeTextSecondary)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(stageColor.copy(alpha = 0.2f))
                                                        .border(0.8.dp, stageColor, RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(stageLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = stageColor)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("الزبائن: ${customers.joinToString("، ")}", fontSize = 11.sp, color = CafeTextPrimary, fontWeight = FontWeight.Medium)

                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = CafeCardBorder.copy(alpha = 0.6f))
                                            Spacer(modifier = Modifier.height(6.dp))

                                            // تفاصيل المواد المطلوبة مع كمياتها وأسعارها
                                            Text(
                                                text = "المواد والطلبات مع الأسعار:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CafeGold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))

                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (parsedItems.isNotEmpty()) {
                                                    parsedItems.forEach { item ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Color(0xFF031619))
                                                                .border(0.6.dp, CafeCardBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                                .padding(horizontal = 8.dp, vertical = 5.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = item.name,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = CafeTextPrimary,
                                                                modifier = Modifier.weight(1.8f)
                                                            )

                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(CafeCyanPrimary.copy(alpha = 0.12f))
                                                                    .border(0.8.dp, CafeCyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = "عدد  ${item.quantity}",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = CafeCyanPrimary
                                                                )
                                                            }

                                                            Spacer(modifier = Modifier.width(8.dp))

                                                            val priceText = if (item.totalPrice > 0.0) {
                                                                "السعر  ${OrderFeeCalculator.formatPrice(item.totalPrice, item.currency)}"
                                                            } else {
                                                                "السعر  0 ${item.currency}"
                                                            }
                                                            Text(
                                                                text = priceText,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = CafeGold,
                                                                textAlign = TextAlign.End,
                                                                modifier = Modifier.weight(1.4f)
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    tableActiveOrders.forEach { ord ->
                                                        Text(text = ord.itemsSummary, fontSize = 11.sp, color = CafeTextPrimary)
                                                    }
                                                }
                                            }

                                            if (combinedNotes.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("ملاحظات: $combinedNotes", fontSize = 10.sp, color = CafeGold)
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = CafeCardBorder.copy(alpha = 0.6f))
                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("مجموع الطلبات:", fontSize = 11.sp, color = CafeTextSecondary)
                                                Text(OrderFeeCalculator.formatPrice(tableSubtotal, tableCurr), fontSize = 11.sp, color = CafeTextPrimary)
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("رسوم الخدمة عند التسديد:", fontSize = 11.sp, color = CafeGold)
                                                Text(OrderFeeCalculator.formatPrice(tableSettlementFee, tableCurr), fontSize = 11.sp, color = CafeGold)
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("الإجمالي المطلوب للتسديد:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                                                Text(
                                                    text = OrderFeeCalculator.formatPrice(tableSettlementTotal, tableCurr),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = CafeCyanPrimary
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = {
                                                    viewModel.payAllOrdersForTable(tableActiveOrders)
                                                    showActiveTablesDialog = false
                                                    Toast.makeText(context, "تم تسديد حساب طاولة $tableNum بنجاح! 💵", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = OrderStatusPaid,
                                                    contentColor = Color(0xFF032125)
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("تسديد حساب طاولة $tableNum بالكامل 💵", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showActiveTablesDialog = false }) {
                            Text("إغلاق", color = CafeCyanPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // نافذة حوارية: تفاصيل المبيعات المسددة والنسبة المتراكمة والحساب الكلي
            if (showRevenueDetailsDialog) {
                AlertDialog(
                    onDismissRequest = { showRevenueDetailsDialog = false },
                    containerColor = CafeSurface,
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = CafeCyanPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تفاصيل المبيعات ورسوم الخدمة", color = CafeTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("بيانات الفواتير المسددة والمبالغ ورسوم الخدمة التراكمية:", fontSize = 12.sp, color = CafeTextSecondary)

                            // 1. المبيعات بدون النسبة (الصافي)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CafeSurfaceVariant),
                                border = BorderStroke(1.dp, CafeCyanPrimary.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("المبيعات الصافية (بدون النسبة):", fontSize = 12.sp, color = CafeTextSecondary)
                                        Text(netRevenueDisplay, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = CafeCyanPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("إجمالي قيمة المواد والأصناف المباعة بدون رسوم الخدمة", fontSize = 10.sp, color = CafeTextSecondary.copy(alpha = 0.8f))
                                }
                            }

                            // 2. النسبة المتراكمة للمبيعات (رسوم الخدمة)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF261D04)),
                                border = BorderStroke(1.dp, CafeGold.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("النسبة المتراكمة (رسوم الخدمة):", fontSize = 12.sp, color = CafeGold)
                                        Text(accumulatedFeesDisplay, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = CafeGold)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("مجموع رسوم الخدمة المتراكمة من جميع الفواتير المسددة", fontSize = 10.sp, color = CafeGold.copy(alpha = 0.8f))
                                }
                            }

                            // 3. الحساب الكلي مع النسبة
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF031E22)),
                                border = BorderStroke(1.dp, OrderStatusPaid.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("الحساب الكلي شامل النسبة:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                                        Text(totalRevenueDisplay, fontSize = 15.sp, fontWeight = FontWeight.Black, color = OrderStatusPaid)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("المجموع الإجمالي الكلي المسدد مع رسوم الخدمة", fontSize = 10.sp, color = OrderStatusPaid.copy(alpha = 0.8f))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("عدد الفواتير المسددة:", fontSize = 11.sp, color = CafeTextSecondary)
                                Text("${paidOrders.size} فاتورة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showRevenueDetailsDialog = false }) {
                            Text("إغلاق", color = CafeCyanPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // مراحل الطلب الخمس (بدون خيار "الكل" - وموحدة كلياً على فواتير الجلسات)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val cashierStatuses = listOf(
                    OrderStatus.NEW,
                    OrderStatus.PREPARING,
                    OrderStatus.DELIVERED,
                    OrderStatus.PAID,
                    OrderStatus.REJECTED
                )
                items(cashierStatuses) { status ->
                    val count = when (status) {
                        OrderStatus.NEW -> newSessionsCount
                        OrderStatus.PREPARING -> preparingSessionsCount
                        OrderStatus.DELIVERED -> deliveredSessionsCount
                        OrderStatus.PAID -> paidSessionsCount
                        OrderStatus.REJECTED -> rejectedSessionsCount
                        else -> 0
                    }
                    StatusFilterChip(
                        label = "${status.titleAr} ($count)",
                        selected = activeFilter == status,
                        color = getStatusColor(status),
                        onClick = { viewModel.setOrderStatusFilter(status) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // LIST OF TABLE BILLS & SESSIONS
            if (filteredSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFF16474E),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (activeFilter) {
                                OrderStatus.NEW -> "لا توجد طلبات أو جلسات جديدة حالياً"
                                OrderStatus.PREPARING -> "لا توجد جلسات قيد التحضير حالياً"
                                OrderStatus.DELIVERED -> "لا توجد جلسات بانتظار تسديد حساب الطاولة حالياً"
                                OrderStatus.PAID -> "لا توجد فواتير مسددة حالياً"
                                OrderStatus.REJECTED -> "قائمة الطلبات المرفوضة فارغة"
                                else -> "لا توجد بيانات"
                            },
                            color = CafeTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (activeFilter) {
                                OrderStatus.NEW -> "عندما يطلب الزبون من طاولته، ستظهر هنا مع خياري (رفض) أو (تحضير)، ويمكن دمج طلبات نفس الزبون يدوياً في أي وقت."
                                OrderStatus.PREPARING -> "عند الضغط على تحضير، تنتقل الجلسة إلى هنا حتى يكتمل تجهيزها، ثم الضغط على (تسديد حساب الطاولة)."
                                OrderStatus.DELIVERED -> "هنا يتم تسديد حساب الطاولة سواء تسديد فردي لكل شخص على حدى أو تسديد كلي لكامل الطاولة."
                                OrderStatus.PAID -> "تظهر هنا جميع الفواتير التي تم تسديدها بالكامل."
                                else -> "تظهر هنا الطلبات التي تم رفضها قبل التحضير."
                            },
                            color = CafeTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.CUSTOMER_MENU) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CafeCyanPrimary,
                                contentColor = Color(0xFF032125)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("فتح منيو الطاولة وإرسال طلب تجريبي", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredSessions, key = { it.sessionId.ifBlank { "${it.tableNumber}_${it.stage}_${it.lastTimestamp}" } }) { session ->
                        if (session.stage == OrderStatus.PAID) {
                            CompactPaidInvoiceCard(
                                session = session,
                                cafeName = cafeName,
                                onRequestDeletePaidOrder = { order ->
                                    orderForAuthorizedDeletion = order
                                }
                            )
                        } else {
                            UnifiedTableSessionCard(
                                session = session,
                                cafeName = cafeName,
                                onAdvanceSession = { nextStatus ->
                                    viewModel.advanceTableSession(session.orders, nextStatus)
                                    Toast.makeText(
                                        context,
                                        "تم تحويل طاولة ${session.tableNumber} إلى ${nextStatus.titleAr} بنجاح",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onMergeCustomerOrders = { ordersToMerge ->
                                    viewModel.mergeCustomerOrders(ordersToMerge)
                                    Toast.makeText(
                                        context,
                                        "تم دمج طلبات الزبون بنجاح 🔗",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onMergeWithPreparing = { newOrder, preparingOrders ->
                                    viewModel.mergeNewOrderWithPreparing(newOrder, preparingOrders)
                                    Toast.makeText(
                                        context,
                                        "تم دمج الطلب مع طلب التحضير بنجاح 🔗",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onRejectSession = {
                                    viewModel.rejectTableSession(session.orders)
                                    Toast.makeText(
                                        context,
                                        "تم رفض طلبات طاولة ${session.tableNumber} بنجاح ❌",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onPaySingleOrder = { order ->
                                    viewModel.payOrders(listOf(order))
                                    Toast.makeText(
                                        context,
                                        "تم تسديد حساب الزبون (${order.customerName}) بنجاح! ✅",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onPayAllSession = { unpaidOrders ->
                                    viewModel.payAllOrdersForTable(unpaidOrders)
                                    Toast.makeText(
                                        context,
                                        "تم تسديد فاتورة طاولة ${session.tableNumber} بالكامل بنجاح! 💵",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onDeleteSession = {
                                    viewModel.deleteTableSession(session.orders)
                                    Toast.makeText(
                                        context,
                                        "تم مسح سجل طاولة ${session.tableNumber}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onRequestDeletePaidOrder = { order ->
                                    orderForAuthorizedDeletion = order
                                }
                            )
                        }
                    }
                }
            }
        }

        // نافذة التحقق من كود الموافقة لحذف فاتورة مسددة
        val toDelete = orderForAuthorizedDeletion
        if (toDelete != null) {
            InvoiceDeletionDialog(
                order = toDelete,
                cafeName = cafeName,
                onDismiss = { orderForAuthorizedDeletion = null },
                onConfirmDelete = { code, onError ->
                    viewModel.deleteAuthorizedPaidOrder(
                        order = toDelete,
                        code = code,
                        onSuccess = {
                            Toast.makeText(context, "تم حذف الفاتورة بنجاح بعد التحقق من كود الموافقة 🗑️", Toast.LENGTH_LONG).show()
                            orderForAuthorizedDeletion = null
                        },
                        onError = onError
                    )
                }
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    hasArrow: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .height(68.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CafeSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(CafeCardBorder, CafeCardBorderGlow)),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = title, fontSize = 11.sp, color = CafeTextSecondary, maxLines = 1)
                if (hasArrow) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = valueColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor, maxLines = 1)
        }
    }
}

@Composable
fun StatusFilterChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) color.copy(alpha = 0.2f) else CafeSurface)
            .border(1.dp, if (selected) color else CafeCardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) color else CafeTextSecondary
        )
    }
}

/**
 * بطاقة الفاتورة المسددة المدمجة والأنيقة:
 * تعرض الفاتورة كإيصال مسدد مدمج ومرتب دون أي مساحات فارغة زائدة.
 */
@Composable
fun CompactPaidInvoiceCard(
    session: TableSession,
    cafeName: String,
    onRequestDeletePaidOrder: ((OrderRecord) -> Unit)? = null
) {
    val context = LocalContext.current
    val paidTotal = remember(session.orders) { session.orders.sumOf { it.totalPrice } }
    val currency = remember(session.orders) { session.orders.firstOrNull()?.currency ?: "د.ع" }
    val timeFormatted = remember(session.lastTimestamp) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date(session.lastTimestamp))
    }

    val customerNames = remember(session.orders) {
        session.orders.map { it.customerName }.filter { it.isNotBlank() }.distinct()
    }

    val parsedItems = remember(session.orders) {
        com.example.util.OrderSummaryHelper.parseOrderItems(session.orders.map { it.itemsSummary })
    }

    val combinedNotes = remember(session.orders) {
        com.example.util.OrderSummaryHelper.combineNotes(session.orders.map { it.notes })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compact_paid_invoice_${session.tableNumber}_${session.lastTimestamp}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CafeSurface),
        border = BorderStroke(1.dp, OrderStatusPaid.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row: طاولة + توقيت التسديد + شارة مسددة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CafeCyanPrimary)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "طاولة ${session.tableNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF032125)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سُددت: $timeFormatted",
                        fontSize = 11.sp,
                        color = CafeTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(OrderStatusPaid.copy(alpha = 0.18f))
                        .border(0.8.dp, OrderStatusPaid, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "تم التسديد بالكامل ✅",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrderStatusPaid
                    )
                }
            }

            if (customerNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CafeGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "الزبون: ${customerNames.joinToString("، ")}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CafeTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // عرض المواد المطلوبة بأسطر خفيفة ومدمجة
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF031619))
                    .border(0.6.dp, CafeCardBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (parsedItems.isNotEmpty()) {
                    parsedItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.name} × ${item.quantity}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CafeTextPrimary,
                                modifier = Modifier.weight(1.8f)
                            )
                            val priceStr = if (item.totalPrice > 0.0) {
                                OrderFeeCalculator.formatPrice(item.totalPrice, item.currency)
                            } else {
                                "0 ${item.currency}"
                            }
                            Text(
                                text = priceStr,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CafeGold
                            )
                        }
                    }
                } else {
                    session.orders.forEach { ord ->
                        Text(
                            text = ord.itemsSummary,
                            fontSize = 11.sp,
                            color = CafeTextPrimary
                        )
                    }
                }
            }

            if (combinedNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ملاحظات: $combinedNotes",
                    fontSize = 10.sp,
                    color = CafeGold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = CafeCardBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            // سطر المجموع المسدد وأزرار الإجراءات في سطر واحد منظم ومدمج
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // المبلغ المسدد
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "المبلغ المسدد: ",
                        fontSize = 11.sp,
                        color = CafeTextSecondary
                    )
                    Text(
                        text = OrderFeeCalculator.formatPrice(paidTotal, currency),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = OrderStatusPaid
                    )
                }

                // الأزرار
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // طباعة
                    Button(
                        onClick = {
                            com.example.util.ReceiptPrinter.printInvoice(
                                context = context,
                                tableNumber = session.tableNumber,
                                orders = session.orders,
                                cafeName = cafeName
                            )
                            Toast.makeText(context, "جاري إرسال الفاتورة إلى الطابعة 🖨️", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CafeCyanPrimary,
                            contentColor = Color(0xFF032125)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("طباعة 🖨️", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    // طلب حذف
                    if (onRequestDeletePaidOrder != null && session.orders.isNotEmpty()) {
                        Button(
                            onClick = {
                                val targetPaid = session.orders.firstOrNull { it.status == OrderStatus.PAID } ?: session.orders.firstOrNull()
                                if (targetPaid != null) {
                                    onRequestDeletePaidOrder(targetPaid)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC62828).copy(alpha = 0.2f),
                                contentColor = Color(0xFFFF5252)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("طلب حذف 🔒", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * بطاقة فاتورة وجلسة الطاولة الموحدة:
 * تجمع جميع طلبات الطاولة في فاتورة واحدة، تفرز طلبات كل زبون باسمه ومبلغه،
 * وتتيح التسديد الفردي لكل شخص أو التسديد الكلي كمجموعة، وتنتقل بدقة عبر المراحل.
 */
@Composable
fun UnifiedTableSessionCard(
    session: TableSession,
    cafeName: String,
    onAdvanceSession: (OrderStatus) -> Unit,
    onMergeCustomerOrders: (List<OrderRecord>) -> Unit,
    onMergeWithPreparing: (OrderRecord, List<OrderRecord>) -> Unit,
    onRejectSession: () -> Unit,
    onPaySingleOrder: (OrderRecord) -> Unit,
    onPayAllSession: (List<OrderRecord>) -> Unit,
    onDeleteSession: () -> Unit,
    onRequestDeletePaidOrder: ((com.example.data.model.OrderRecord) -> Unit)? = null
) {
    val context = LocalContext.current
    val unpaidOrders = session.orders.filter { it.status != OrderStatus.PAID }
    val paidOrders = session.orders.filter { it.status == OrderStatus.PAID }
    val isFullyPaid = unpaidOrders.isEmpty()

    val timeFormatted = remember(session.lastTimestamp) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date(session.lastTimestamp))
    }

    // حساب المجاميع الجماعية للطلبات غير المسددة
    val groupCurrency = unpaidOrders.firstOrNull()?.currency ?: session.orders.firstOrNull()?.currency ?: "د.ع"
    val groupSubtotal = unpaidOrders.sumOf { it.subtotal }
    val groupFee = OrderFeeCalculator.calculateServiceFee(groupSubtotal, groupCurrency)
    val groupTotal = groupSubtotal + groupFee

    // تجميع الطلبات حسب اسم كل زبون
    val customerGroups = remember(session.orders) {
        session.orders.groupBy { it.customerName }
    }

    val stageColor = getStatusColor(session.stage)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("table_session_card_${session.tableNumber}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CafeSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(stageColor.copy(alpha = 0.8f), CafeCardBorder)),
            width = 1.3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: رقم الطاولة + توقيت الجلسة + شارة المرحلة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CafeCyanPrimary)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "طاولة ${session.tableNumber}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF032125)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "آخر طلب: $timeFormatted",
                        fontSize = 11.sp,
                        color = CafeTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(stageColor.copy(alpha = 0.2f))
                        .border(1.dp, stageColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = session.stage.titleAr,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = stageColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // أسماء الزبائن في هذه الجلسة
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = CafeCyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "زبائن الطاولة (${customerGroups.keys.size}): ${customerGroups.keys.joinToString("، ")}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = CafeCardBorder.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(10.dp))

            // استعراض وفرز طلبات كل شخص على حدى مع مبلغه داخل الفاتورة الموحدة
            Text(
                text = "تفاصيل طلبات كل شخص في الفاتورة:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CafeGold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                customerGroups.forEach { (customerName, customerOrders) ->
                    val isCustFullyPaid = customerOrders.all { it.status == OrderStatus.PAID }
                    val custSubtotal = customerOrders.sumOf { it.subtotal }
                    val custFee = customerOrders.sumOf { it.serviceFee }
                    val custTotal = customerOrders.sumOf { it.totalPrice }
                    val custCurrency = customerOrders.firstOrNull()?.currency ?: "د.ع"
                    val itemsCombined = com.example.util.OrderSummaryHelper.combineAndMultiplySummaries(
                        customerOrders.map { it.itemsSummary }
                    )
                    val notesCombined = com.example.util.OrderSummaryHelper.combineNotes(
                        customerOrders.map { it.notes }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCustFullyPaid) Color(0xFF041F23) else CafeSurfaceVariant)
                            .border(
                                1.dp,
                                if (isCustFullyPaid) CafeCyanPrimary.copy(alpha = 0.35f) else CafeCardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isCustFullyPaid) CafeCyanPrimary else CafeGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "الزبون: $customerName",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CafeTextPrimary
                                )
                            }

                            if (isCustFullyPaid) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(OrderStatusPaid)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "مسدد ✅",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF032125)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF2E2205))
                                        .border(0.8.dp, CafeGold, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "غير مسدد ⏳",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CafeGold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // عرض كل صنف وأمامه كميته وسعره بشكل منسق ومفصل: [اسم الصنف] [عدد X] [السعر X]
                        val parsedItems = remember(customerOrders) {
                            com.example.util.OrderSummaryHelper.parseOrderItems(customerOrders.map { it.itemsSummary })
                        }

                        if (parsedItems.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                parsedItems.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF031619))
                                            .border(0.6.dp, CafeCardBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // اسم الصنف
                                        Text(
                                            text = item.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CafeTextPrimary,
                                            modifier = Modifier.weight(1.8f)
                                        )

                                        // عدد X
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(CafeCyanPrimary.copy(alpha = 0.12f))
                                                .border(0.8.dp, CafeCyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "عدد  ${item.quantity}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CafeCyanPrimary
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // السعر X
                                        val priceText = if (item.totalPrice > 0.0) {
                                            "السعر  ${OrderFeeCalculator.formatPrice(item.totalPrice, item.currency)}"
                                        } else {
                                            "السعر  0 ${item.currency}"
                                        }
                                        Text(
                                            text = priceText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = CafeGold,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.weight(1.4f)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = itemsCombined,
                                fontSize = 12.sp,
                                color = CafeTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (notesCombined.isNotBlank()) {
                            Text(
                                text = "ملاحظات: $notesCombined",
                                fontSize = 10.sp,
                                color = CafeGold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // زر الدمج اليدوي للطلبات المتعددة لنفس الزبون في المرحلتين الأولى والثانية
                        if ((session.stage == OrderStatus.NEW || session.stage == OrderStatus.PREPARING) && customerOrders.size > 1) {
                            Button(
                                onClick = { onMergeCustomerOrders(customerOrders) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CafeGold.copy(alpha = 0.15f),
                                    contentColor = CafeGold
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CafeGold.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = "دمج طلبات $customerName (عدد ${customerOrders.size}) 🔗",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        val custSettlementTotal = OrderFeeCalculator.calculateSettlementTotal(custSubtotal, custCurrency)
                        val custDisplayTotal = if (session.stage == OrderStatus.DELIVERED) custSettlementTotal else custTotal

                        // سطر المبلغ والتسديد الفردي
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المبلغ: ${OrderFeeCalculator.formatPrice(custDisplayTotal, custCurrency)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isCustFullyPaid) CafeCyanPrimary else CafeGold
                            )

                            // زر التسديد الفردي لهذا الشخص (متاح حصراً في مرحلة تسديد حساب الطاولة)
                            if (session.stage == OrderStatus.DELIVERED && !isCustFullyPaid) {
                                Button(
                                    onClick = {
                                        val unpaidCustOrders = customerOrders.filter { it.status != OrderStatus.PAID }
                                        onPayAllSession(unpaidCustOrders)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CafeCyanPrimary,
                                        contentColor = Color(0xFF032125)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "تسديد حساب $customerName 💵",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // قسم الحساب الكلي والإجراءات حسب مرحلة الجلسة
            when (session.stage) {
                OrderStatus.NEW -> {
                    // طلب جديد: يظهر ثلاثة أزرار بنفس القياس (رفض / دمج / أوافق)
                    val thisOrder = session.orders.firstOrNull()
                    val allNewOrdersTotal = session.orders.sumOf { it.totalPrice }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF04181B))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مبلغ هذا الطلب:",
                            fontSize = 11.sp,
                            color = CafeTextSecondary
                        )
                        Text(
                            text = OrderFeeCalculator.formatPrice(allNewOrdersTotal, groupCurrency),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CafeCyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ثلاثة أزرار بنفس القياس تماماً (رفض / دمج / أوافق)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. زر رفض
                        Button(
                            onClick = onRejectSession,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC62828),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("رفض ❌", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // 2. زر دمج (بنفس القياس)
                        val canMerge = session.otherTableOrders.isNotEmpty()
                        Button(
                            onClick = {
                                if (canMerge) {
                                    if (session.hasPreparingOrder && session.preparingOrders.isNotEmpty() && thisOrder != null) {
                                        onMergeWithPreparing(thisOrder, session.preparingOrders)
                                    } else if (session.otherTableOrders.isNotEmpty()) {
                                        onMergeCustomerOrders(session.orders + session.otherTableOrders)
                                    }
                                } else {
                                    Toast.makeText(context, "لا يوجد طلب آخر لنفس الطاولة لدمجه معه", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canMerge) CafeGold.copy(alpha = 0.2f) else CafeSurfaceVariant,
                                contentColor = if (canMerge) CafeGold else CafeTextSecondary.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, if (canMerge) CafeGold else CafeCardBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("دمج 🔗", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // 3. زر أوافق (بنفس القياس - يرسل الطلب للتحضير دون دمج)
                        Button(
                            onClick = { onAdvanceSession(OrderStatus.PREPARING) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrderStatusPreparing,
                                contentColor = Color(0xFF1E1400)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("أوافق ✅", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OrderStatus.PREPARING -> {
                    // قيد التحضير: يظهر زر تم تسليم الطلب وبجانبه زر دمج الطلبات لنفس الطاولة دون أي زر لتسديد الحساب
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF161002))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "يتم الآن تحضير هذا الطلب في المطبخ/البار...",
                            fontSize = 11.sp,
                            color = OrderStatusPreparing
                        )
                        if (session.otherTableOrders.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CafeGold.copy(alpha = 0.2f))
                                    .border(0.8.dp, CafeGold, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "يوجد طلب آخر لنفس الطاولة 🔗",
                                    fontSize = 10.sp,
                                    color = CafeGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // زران متجاوران: دمج الطلبات بجانب تم تسليم الطلب (تم حذف زر تسديد الحساب نهائياً من التحضير)
                    val hasOtherOrdersForTable = session.otherTableOrders.isNotEmpty()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasOtherOrdersForTable) {
                            Button(
                                onClick = {
                                    onMergeCustomerOrders(session.orders + session.otherTableOrders)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CafeGold.copy(alpha = 0.2f),
                                    contentColor = CafeGold
                                ),
                                border = BorderStroke(1.dp, CafeGold),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("دمج الطلبات 🔗", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // زر تم تسليم الطلب لينتقل إلى مرحلة تسديد حساب الطاولة بعد الضغط عليه
                        Button(
                            onClick = { onAdvanceSession(OrderStatus.DELIVERED) },
                            modifier = if (hasOtherOrdersForTable) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrderStatusDelivered,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("تم تسليم الطلب 🍽️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OrderStatus.DELIVERED, OrderStatus.READY -> {
                    // تسديد حساب الطاولة: يحتوي على تسديد الفاتورة (تسديد كلي أو فردي)
                    val groupRawFee = OrderFeeCalculator.calculateServiceFee(groupSubtotal, groupCurrency)
                    val groupSettlementTotal = OrderFeeCalculator.calculateSettlementTotal(groupSubtotal, groupCurrency)
                    val groupSettlementFee = OrderFeeCalculator.calculateSettlementFee(groupSubtotal, groupCurrency)

                    if (!isFullyPaid) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF041B1E)),
                            border = BorderStroke(1.dp, CafeGold.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = CafeGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تسديد حساب الطاولة كمجموعة (عن الطاولة بالكامل):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CafeGold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("مجموع الطلبات غير المسددة:", fontSize = 11.sp, color = CafeTextSecondary)
                                    Text(OrderFeeCalculator.formatPrice(groupSubtotal, groupCurrency), fontSize = 11.sp, color = CafeTextPrimary)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("رسوم الخدمة 1%:", fontSize = 11.sp, color = CafeTextSecondary)
                                    Text(OrderFeeCalculator.formatPrice(groupRawFee, groupCurrency), fontSize = 11.sp, color = CafeTextSecondary)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("رسوم الخدمة عند التسديد:", fontSize = 11.sp, color = CafeGold)
                                    Text(OrderFeeCalculator.formatPrice(groupSettlementFee, groupCurrency), fontSize = 11.sp, color = CafeGold)
                                }

                                HorizontalDivider(color = CafeCardBorder, modifier = Modifier.padding(vertical = 4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("الإجمالي الكلي المطلوب:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                                    Text(
                                        text = OrderFeeCalculator.formatPrice(groupSettlementTotal, groupCurrency),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CafeCyanPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { onPayAllSession(unpaidOrders) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OrderStatusPaid,
                                        contentColor = Color(0xFF032125)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "تسديد الفاتورة بالكامل (دفع كلي / جماعي) 💵",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    } else {
                        // جميع زبائن الطاولة سددوا حساباتهم فردياً
                        Button(
                            onClick = { onAdvanceSession(OrderStatus.PAID) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrderStatusPaid,
                                contentColor = Color(0xFF032125)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("اعتماد الفاتورة كمسددة بالكامل ✅", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OrderStatus.PAID -> {
                    // الفواتير المسددة (مكتملة ومسددة بالكامل وجاهزة للطباعة)
                    val paidTotal = session.orders.sumOf { it.totalPrice }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF021B1F))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المبلغ الإجمالي المسدد:",
                            fontSize = 11.sp,
                            color = CafeTextSecondary
                        )
                        Text(
                            text = OrderFeeCalculator.formatPrice(paidTotal, groupCurrency),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OrderStatusPaid
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "تم تسديد الفاتورة بالكامل ✅",
                                fontSize = 12.sp,
                                color = OrderStatusPaid,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // زر طباعة الفاتورة
                            Button(
                                onClick = {
                                    com.example.util.ReceiptPrinter.printInvoice(
                                        context = context,
                                        tableNumber = session.tableNumber,
                                        orders = session.orders,
                                        cafeName = cafeName
                                    )
                                    Toast.makeText(context, "جاري إرسال الفاتورة إلى الطابعة 🖨️", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CafeCyanPrimary,
                                    contentColor = Color(0xFF032125)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("طباعة الفاتورة 🖨️", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (onRequestDeletePaidOrder != null && session.orders.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        val targetPaid = session.orders.firstOrNull { it.status == OrderStatus.PAID } ?: session.orders.firstOrNull()
                                        if (targetPaid != null) {
                                            onRequestDeletePaidOrder(targetPaid)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFC62828).copy(alpha = 0.2f),
                                        contentColor = Color(0xFFFF5252)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f))
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("طلب حذف 🔒", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CafeSurfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "محفوظة في الأرشيف",
                                    tint = CafeGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "محفوظة في الأرشيف 🔒",
                                    fontSize = 10.sp,
                                    color = CafeTextSecondary
                                )
                            }
                        }
                    }
                }

                OrderStatus.REJECTED, OrderStatus.CANCELLED -> {
                    // الطلبات المرفوضة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "جلسة مرفوضة ❌",
                            fontSize = 12.sp,
                            color = OrderStatusRejected,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { onAdvanceSession(OrderStatus.NEW) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CafeSurfaceVariant,
                                    contentColor = CafeCyanPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text("إعادة للجديد 🔄", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = onDeleteSession,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CafeSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getStatusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.NEW -> OrderStatusNew
        OrderStatus.PREPARING -> OrderStatusPreparing
        OrderStatus.DELIVERED -> OrderStatusDelivered
        OrderStatus.READY -> OrderStatusReady
        OrderStatus.PAID -> OrderStatusPaid
        OrderStatus.REJECTED -> OrderStatusRejected
        OrderStatus.CANCELLED -> OrderStatusCancelled
    }
}
