package com.example.ui.cashier

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    val lastTimestamp: Long
)

@Composable
fun CashierOrdersScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val orders by viewModel.orders.collectAsState()
    val activeFilter by viewModel.orderStatusFilter.collectAsState()

    // تجميع كافة الطلبات في جلسات وفواتير موحدة حسب كل طاولة
    // (التخلص من الطلبات الفردية المنعزلة، وفاتورة واحدة متكاملة لكل جلسة)
    val tableSessions = remember(orders) {
        val byTable = orders.groupBy { it.tableNumber }
        val sessionsList = mutableListOf<TableSession>()

        byTable.forEach { (tableNumber, tableOrders) ->
            val activeOrders = tableOrders.filter {
                it.status != OrderStatus.PAID &&
                it.status != OrderStatus.CANCELLED &&
                it.status != OrderStatus.REJECTED
            }

            if (activeOrders.isNotEmpty()) {
                // تحديد مرحلة الجلسة الحالية
                val stage = when {
                    activeOrders.any { it.status == OrderStatus.DELIVERED || it.status == OrderStatus.READY } -> OrderStatus.DELIVERED
                    activeOrders.any { it.status == OrderStatus.PREPARING } -> OrderStatus.PREPARING
                    else -> OrderStatus.NEW
                }

                // تضمين الطلبات النشطة والطلبات المسددة فردياً ضمن نفس الجلسة لتبقى موحدة في فاتورة واحدة
                val earliestActiveTime = activeOrders.minOf { it.timestamp }
                val sessionOrders = tableOrders.filter {
                    it in activeOrders || (it.status == OrderStatus.PAID && it.timestamp >= earliestActiveTime - 14400000L)
                }.sortedBy { it.timestamp }

                sessionsList.add(
                    TableSession(
                        tableNumber = tableNumber,
                        orders = sessionOrders,
                        stage = stage,
                        lastTimestamp = sessionOrders.maxOf { it.timestamp }
                    )
                )
            } else {
                // في حال كانت جميع طلبات الطاولة مسددة أو ملغية/مرفوضة
                val paidOrders = tableOrders.filter { it.status == OrderStatus.PAID }
                if (paidOrders.isNotEmpty()) {
                    sessionsList.add(
                        TableSession(
                            tableNumber = tableNumber,
                            orders = paidOrders.sortedBy { it.timestamp },
                            stage = OrderStatus.PAID,
                            lastTimestamp = paidOrders.maxOf { it.timestamp }
                        )
                    )
                }

                val rejectedOrders = tableOrders.filter { it.status == OrderStatus.REJECTED || it.status == OrderStatus.CANCELLED }
                if (rejectedOrders.isNotEmpty()) {
                    sessionsList.add(
                        TableSession(
                            tableNumber = tableNumber,
                            orders = rejectedOrders.sortedBy { it.timestamp },
                            stage = OrderStatus.REJECTED,
                            lastTimestamp = rejectedOrders.maxOf { it.timestamp }
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

    val activeTablesCount = tableSessions.count {
        it.stage != OrderStatus.PAID && it.stage != OrderStatus.REJECTED
    }

    val cafeName by viewModel.cafeName.collectAsState()

    // حساب إجمالي الإيرادات المسددة
    val paidOrders = orders.filter { it.status == OrderStatus.PAID }
    val totalRevenueIqd = paidOrders.filter { it.currency != "$" && it.currency != "USD" }.sumOf { it.totalPrice }
    val totalRevenueUsd = paidOrders.filter { it.currency == "$" || it.currency == "USD" }.sumOf { it.totalPrice }

    val revenueDisplay = when {
        totalRevenueIqd > 0 && totalRevenueUsd > 0 ->
            "${OrderFeeCalculator.formatPrice(totalRevenueIqd, "د.ع")} + ${OrderFeeCalculator.formatPrice(totalRevenueUsd, "$")}"
        totalRevenueUsd > 0 -> OrderFeeCalculator.formatPrice(totalRevenueUsd, "$")
        else -> OrderFeeCalculator.formatPrice(totalRevenueIqd, "د.ع")
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // RIGHT SIDE: التبديل السريع
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CafeSurfaceVariant)
                        .border(1.dp, CafeCardBorder, RoundedCornerShape(20.dp))
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Active Cashier Tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(CafeCyanPrimary)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = Color(0xFF032125),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "الكاشير",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF032125)
                            )
                        }
                    }

                    // Inactive Menu Tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.navigateTo(AppScreen.CUSTOMER_MENU) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("cashier_to_menu_switch_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = CafeCyanPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تفقد المنيو",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CafeTextSecondary
                            )
                        }
                    }
                }

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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CafeSurfaceVariant)
                        .border(1.2.dp, CafeCardBorderGlow, CircleShape)
                        .testTag("cashier_gear_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "الإعدادات",
                        tint = CafeCyanPrimary,
                        modifier = Modifier.size(22.dp)
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
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "طاولات نشطة",
                    value = "$activeTablesCount",
                    valueColor = CafeGold,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "المبيعات المسددة",
                    value = revenueDisplay,
                    valueColor = CafeCyanPrimary,
                    modifier = Modifier.weight(1.5f)
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
                                OrderStatus.DELIVERED -> "لا توجد جلسات مسلّمة بانتظار تسديد الفاتورة"
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
                                OrderStatus.NEW -> "عندما يطلب الزبون من طاولته، ستظهر الفاتورة هنا مع خياري (رفض) أو (تحضير)، وأي طلب إضافي لنفس الطاولة سينضم لنفس الفاتورة تلقائياً."
                                OrderStatus.PREPARING -> "عند الضغط على تحضير، تنتقل الجلسة إلى هنا حتى يكتمل تجهيزها ويتم الضغط على (تم التسليم)."
                                OrderStatus.DELIVERED -> "هنا يتم تسديد الفاتورة سواء تسديد فردي لكل شخص على حدى أو تسديد كلي لكامل الطاولة."
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
                    items(filteredSessions, key = { "${it.tableNumber}_${it.stage}_${it.lastTimestamp}" }) { session ->
                        UnifiedTableSessionCard(
                            session = session,
                            onAdvanceSession = { nextStatus ->
                                viewModel.advanceTableSession(session.orders, nextStatus)
                                Toast.makeText(
                                    context,
                                    "تم تحويل طاولة ${session.tableNumber} إلى ${nextStatus.titleAr} بنجاح",
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
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(68.dp),
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
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = CafeTextSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor, maxLines = 1)
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
 * بطاقة فاتورة وجلسة الطاولة الموحدة:
 * تجمع جميع طلبات الطاولة في فاتورة واحدة، تفرز طلبات كل زبون باسمه ومبلغه،
 * وتتيح التسديد الفردي لكل شخص أو التسديد الكلي كمجموعة، وتنتقل بدقة عبر المراحل.
 */
@Composable
fun UnifiedTableSessionCard(
    session: TableSession,
    onAdvanceSession: (OrderStatus) -> Unit,
    onRejectSession: () -> Unit,
    onPaySingleOrder: (OrderRecord) -> Unit,
    onPayAllSession: (List<OrderRecord>) -> Unit,
    onDeleteSession: () -> Unit
) {
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
                    val itemsCombined = customerOrders.joinToString(" + ") { it.itemsSummary }
                    val notesCombined = customerOrders.map { it.notes }.filter { it.isNotBlank() }.joinToString(" | ")

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

                        // الأصناف
                        Text(
                            text = itemsCombined,
                            fontSize = 12.sp,
                            color = CafeTextPrimary,
                            fontWeight = FontWeight.Medium
                        )

                        if (notesCombined.isNotBlank()) {
                            Text(
                                text = "ملاحظات: $notesCombined",
                                fontSize = 10.sp,
                                color = CafeGold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // سطر المبلغ والتسديد الفردي
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المبلغ: ${OrderFeeCalculator.formatPrice(custTotal, custCurrency)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isCustFullyPaid) CafeCyanPrimary else CafeGold
                            )

                            // زر التسديد الفردي لهذا الشخص (متاح في مرحلة تم التسليم أو التحضير)
                            if ((session.stage == OrderStatus.DELIVERED || session.stage == OrderStatus.PREPARING) && !isCustFullyPaid) {
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
                    // طلب جديد: يحتوي على "رفض" و "تحضير"
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
                            text = "إجمالي طلبات الجلسة:",
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onRejectSession,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC62828),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("رفض ❌", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = { onAdvanceSession(OrderStatus.PREPARING) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrderStatusPreparing,
                                contentColor = Color(0xFF1E1400)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("تحضير 🍳", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OrderStatus.PREPARING -> {
                    // تحضير: يحتوي على "تم التسليم"
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
                            text = "يتم الآن تحضير أصناف الطاولة في المطبخ/البار...",
                            fontSize = 11.sp,
                            color = OrderStatusPreparing
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onAdvanceSession(OrderStatus.DELIVERED) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrderStatusDelivered,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("تم التسليم 🍽️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OrderStatus.DELIVERED, OrderStatus.READY -> {
                    // تم التسليم: يحتوي على تسديد الفاتورة (تسديد كلي أو فردي)
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
                                        text = "تسديد الفاتورة كمجموعة (عن الطاولة بالكامل):",
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
                                    Text("نسبة الخدمة 1% (أقرب 250 د.ع):", fontSize = 11.sp, color = CafeGold)
                                    Text(OrderFeeCalculator.formatPrice(groupFee, groupCurrency), fontSize = 11.sp, color = CafeGold)
                                }

                                HorizontalDivider(color = CafeCardBorder, modifier = Modifier.padding(vertical = 4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("الإجمالي الكلي المطلوب:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                                    Text(
                                        text = OrderFeeCalculator.formatPrice(groupTotal, groupCurrency),
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
                    // تسديد الفاتورة (مكتملة ومسددة بالكامل)
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
                        Text(
                            text = "تم تسديد الفاتورة بالكامل ✅",
                            fontSize = 12.sp,
                            color = OrderStatusPaid,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = onDeleteSession,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CafeSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "مسح السجل",
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(18.dp)
                            )
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
