package com.example.ui.cashier

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun CashierOrdersScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val orders by viewModel.orders.collectAsState()
    val activeFilter by viewModel.orderStatusFilter.collectAsState()
    var cashierViewMode by remember { mutableIntStateOf(0) } // 0 = Individual Orders, 1 = Table Sessions Billing

    val filteredOrders = when (activeFilter) {
        OrderStatus.DELIVERED, OrderStatus.READY -> orders.filter { it.status == OrderStatus.DELIVERED || it.status == OrderStatus.READY }
        OrderStatus.REJECTED, OrderStatus.CANCELLED -> orders.filter { it.status == OrderStatus.REJECTED || it.status == OrderStatus.CANCELLED }
        else -> orders.filter { it.status == activeFilter }
    }

    val newOrdersCount = orders.count { it.status == OrderStatus.NEW }
    val activeTablesCount = orders
        .filter { it.status != OrderStatus.PAID && it.status != OrderStatus.CANCELLED && it.status != OrderStatus.REJECTED }
        .map { it.tableNumber }
        .distinct()
        .size

    val cafeName by viewModel.cafeName.collectAsState()

    // Total sales calculation
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
            // TOP BAR: Right (Switcher: Cashier/Menu), Center (Cafe Name), Left (Settings Gear)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // RIGHT SIDE: Fast Switcher between Cashier & Menu Inspection
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

                    // Inactive Menu Tab (Click to inspect menu)
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

                // CENTER: Cafe Name & Subtitle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = cafeName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CafeTextPrimary
                    )
                    Text(
                        text = if (newOrdersCount > 0) "$newOrdersCount طلبات جديدة" else "لوحة الكاشير والحسابات",
                        fontSize = 11.sp,
                        color = if (newOrdersCount > 0) CafeCyanPrimary else CafeGold,
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
                    value = "$newOrdersCount",
                    valueColor = if (newOrdersCount > 0) CafeCyanPrimary else CafeTextPrimary,
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

            Spacer(modifier = Modifier.height(8.dp))

            // VIEW MODE TOGGLE (Requirement 5: Individual Orders vs Table Sessions Group Billing)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tab 0: Individual Orders
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (cashierViewMode == 0) CafeCyanPrimary else CafeSurfaceVariant)
                        .border(1.dp, if (cashierViewMode == 0) CafeCyanPrimary else CafeCardBorder, RoundedCornerShape(14.dp))
                        .clickable { cashierViewMode = 0 }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = if (cashierViewMode == 0) Color(0xFF032125) else CafeCyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الطلبات الفردية (${orders.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cashierViewMode == 0) Color(0xFF032125) else CafeTextPrimary
                        )
                    }
                }

                // Tab 1: Table Sessions Billing
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (cashierViewMode == 1) CafeCyanPrimary else CafeSurfaceVariant)
                        .border(1.dp, if (cashierViewMode == 1) CafeCyanPrimary else CafeCardBorder, RoundedCornerShape(14.dp))
                        .clickable { cashierViewMode = 1 }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = if (cashierViewMode == 1) Color(0xFF032125) else CafeCyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "جلسات وحساب الطاولات ($activeTablesCount)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cashierViewMode == 1) Color(0xFF032125) else CafeTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (cashierViewMode == 0) {
                // STATUS FILTER ROW FOR INDIVIDUAL ORDERS
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
                            OrderStatus.DELIVERED -> orders.count { it.status == OrderStatus.DELIVERED || it.status == OrderStatus.READY }
                            OrderStatus.REJECTED -> orders.count { it.status == OrderStatus.REJECTED || it.status == OrderStatus.CANCELLED }
                            else -> orders.count { it.status == status }
                        }
                        StatusFilterChip(
                            label = "${status.titleAr} ($count)",
                            selected = activeFilter == status || (status == OrderStatus.DELIVERED && activeFilter == OrderStatus.READY),
                            color = getStatusColor(status),
                            onClick = { viewModel.setOrderStatusFilter(status) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ORDERS LIST
                if (filteredOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                tint = Color(0xFF16474E),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "لا توجد طلبات في هذه الحالة حالياً",
                                color = CafeTextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.CUSTOMER_MENU) },
                                colors = ButtonDefaults.buttonColors(containerColor = CafeCyanPrimary, contentColor = Color(0xFF032125)),
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredOrders, key = { it.id }) { order ->
                            CashierOrderCard(
                                order = order,
                                onAdvanceStatus = { nextStatus ->
                                    viewModel.updateOrderStatus(order, nextStatus)
                                },
                                onReject = {
                                    viewModel.rejectOrder(order)
                                    Toast.makeText(context, "تم رفض طلب الزبون ${order.customerName} بنجاح", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    viewModel.deleteOrder(order)
                                }
                            )
                        }
                    }
                }
            } else {
                // TABLE SESSIONS BILLING VIEW (Requirement 5)
                val activeSessionOrders = orders.filter {
                    it.status != OrderStatus.CANCELLED && it.status != OrderStatus.REJECTED
                }
                val tableSessions = activeSessionOrders.groupBy { it.tableNumber }

                if (tableSessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFF16474E),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "لا توجد جلسات طاولات نشطة حالياً",
                                color = CafeTextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "عندما يطلب الزبائن من نفس الطاولة، ستجتمع طلباتهم هنا لتسديدها فردياً أو جماعياً",
                                color = CafeTextSecondary.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
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
                        items(tableSessions.keys.toList().sorted()) { tableNum ->
                            val sessionOrders = tableSessions[tableNum] ?: emptyList()
                            TableSessionBillingCard(
                                tableNumber = tableNum,
                                sessionOrders = sessionOrders,
                                onPaySingleOrder = { order ->
                                    viewModel.updateOrderStatus(order, OrderStatus.PAID)
                                    Toast.makeText(context, "تم تسديد طلب الزبون ${order.customerName} بنجاح!", Toast.LENGTH_SHORT).show()
                                },
                                onPayAllSession = { unpaidOrders ->
                                    viewModel.payAllOrdersForTable(unpaidOrders)
                                    Toast.makeText(context, "تم تسديد حساب الطاولة $tableNum بالكامل بنجاح!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
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
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CafeCardBorder, CafeCardBorderGlow)), width = 1.dp)
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
 * Individual Order Card with reject option and 1% fee breakdown
 */
@Composable
fun CashierOrderCard(
    order: OrderRecord,
    onAdvanceStatus: (OrderStatus) -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = getStatusColor(order.status)
    val timeFormatted = formatTimestamp(order.timestamp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CafeSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    statusColor.copy(alpha = 0.5f),
                    CafeCardBorder
                )
            ),
            width = 1.2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Table badge, Order ID, Time, Status badge
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
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "طاولة ${order.tableNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF032125)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "#${order.id}",
                        fontSize = 12.sp,
                        color = CafeTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = CafeTextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = order.status.titleAr,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Customer Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CafeCyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الزبون: ${order.customerName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Order items summary
            Text(
                text = order.itemsSummary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = CafeTextPrimary,
                lineHeight = 20.sp
            )

            // Special notes if any
            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ملاحظة: ",
                        fontSize = 12.sp,
                        color = CafeGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.notes,
                        fontSize = 12.sp,
                        color = CafeTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price Breakdown Box (Subtotal + 1% fee + Total)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF04181B))
                    .border(0.8.dp, CafeCardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "سعر الطلب: ${OrderFeeCalculator.formatPrice(order.subtotal, order.currency)}",
                        fontSize = 11.sp,
                        color = CafeTextSecondary
                    )
                    Text(
                        text = "نسبة 1% (أقرب 250 د.ع): ${OrderFeeCalculator.formatPrice(order.serviceFee, order.currency)}",
                        fontSize = 10.sp,
                        color = CafeGold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "المبلغ المطلوب", fontSize = 9.sp, color = CafeTextSecondary)
                    Text(
                        text = OrderFeeCalculator.formatPrice(order.totalPrice, order.currency),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CafeCyanPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom action row: Status advance or Reject
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (order.status) {
                    OrderStatus.NEW -> {
                        // طلب جديد: يحتوي على "رفض" و "تحضير"
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onReject,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFC62828),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("رفض ❌", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onAdvanceStatus(OrderStatus.PREPARING) },
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
                        Button(
                            onClick = { onAdvanceStatus(OrderStatus.DELIVERED) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrderStatusDelivered, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("تم التسليم 🍽️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.DELIVERED, OrderStatus.READY -> {
                        // تم التسليم: يحتوي على "تسديد الفاتورة"
                        Button(
                            onClick = { onAdvanceStatus(OrderStatus.PAID) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrderStatusPaid, contentColor = Color(0xFF032125)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("تسديد الفاتورة 💵", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.PAID -> {
                        // تم تسديد الفاتورة
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "تم تسديد الفاتورة ✅",
                                fontSize = 11.sp,
                                color = OrderStatusPaid,
                                modifier = Modifier.padding(end = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = onDelete,
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
                    OrderStatus.REJECTED, OrderStatus.CANCELLED -> {
                        // الطلبات المرفوضة
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "طلب مرفوض ❌",
                                fontSize = 11.sp,
                                color = OrderStatusRejected,
                                modifier = Modifier.padding(end = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { onAdvanceStatus(OrderStatus.NEW) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CafeSurfaceVariant,
                                    contentColor = CafeCyanPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text("إعادة للجديد 🔄", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CafeSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف نهائي",
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

/**
 * Table Session Card (Requirement 5: Grouping separate orders of the same table in a single session)
 * Allows individual settlement per customer or complete collective settlement with 1% calculated on grand total.
 */
@Composable
fun TableSessionBillingCard(
    tableNumber: Int,
    sessionOrders: List<OrderRecord>,
    onPaySingleOrder: (OrderRecord) -> Unit,
    onPayAllSession: (List<OrderRecord>) -> Unit
) {
    val unpaidOrders = sessionOrders.filter { it.status != OrderStatus.PAID }
    val paidOrders = sessionOrders.filter { it.status == OrderStatus.PAID }
    val isFullyPaid = unpaidOrders.isEmpty()

    // Collective calculation for unpaid orders
    val groupCurrency = unpaidOrders.firstOrNull()?.currency ?: "د.ع"
    val groupSubtotal = unpaidOrders.sumOf { it.subtotal }
    val groupFee = OrderFeeCalculator.calculateServiceFee(groupSubtotal, groupCurrency)
    val groupTotal = groupSubtotal + groupFee

    // Individual payments sum for comparison (Requirement 5 mentions individual sum could be slightly higher due to 250 IQD min)
    val individualTotalSum = unpaidOrders.sumOf { it.totalPrice }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("table_session_card_$tableNumber"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CafeSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(CafeCyanPrimary, CafeGold)),
            width = 1.3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Table Number + Session Status
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
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "طاولة $tableNumber (جلسة نشطة)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF032125)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${sessionOrders.size} طلبات إجمالية",
                        fontSize = 12.sp,
                        color = CafeTextSecondary
                    )
                }

                if (isFullyPaid) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(OrderStatusPaid.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("تم تسديد الجلسة بالكامل ✅", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrderStatusPaid)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CafeGold.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("${unpaidOrders.size} طلبات بانتظار التسديد", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CafeGold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customers in this session
            val customerNames = sessionOrders.map { it.customerName }.distinct().joinToString("، ")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = CafeCyanPrimary, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "زبائن الجلسة: $customerNames",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CafeTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = CafeCardBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))

            // Sub-list: Individual Orders within this session
            Text(
                text = "الطلبات المنفردة لكل زبون في الجلسة:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CafeTextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sessionOrders.forEach { order ->
                    val isPaid = order.status == OrderStatus.PAID
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isPaid) Color(0xFF041F23) else CafeSurfaceVariant)
                            .border(1.dp, if (isPaid) CafeCyanPrimary.copy(alpha = 0.3f) else CafeCardBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "الزبون: ${order.customerName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CafeTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${order.status.titleAr})",
                                    fontSize = 10.sp,
                                    color = getStatusColor(order.status)
                                )
                            }
                            Text(
                                text = order.itemsSummary,
                                fontSize = 11.sp,
                                color = CafeTextSecondary,
                                maxLines = 1
                            )
                            Text(
                                text = "الطلب: ${OrderFeeCalculator.formatPrice(order.subtotal, order.currency)} + نسبة: ${OrderFeeCalculator.formatPrice(order.serviceFee, order.currency)} = ${OrderFeeCalculator.formatPrice(order.totalPrice, order.currency)}",
                                fontSize = 10.sp,
                                color = CafeGold
                            )
                        }

                        if (isPaid) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(OrderStatusPaid)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("مسدد ✅", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF032125))
                            }
                        } else {
                            Button(
                                onClick = { onPaySingleOrder(order) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CafeCyanPrimary,
                                    contentColor = Color(0xFF032125)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "تسديد منفرد 💵",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // COLLECTIVE BILLING SECTION (Only if there are unpaid orders)
            if (!isFullyPaid) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF041B1E)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CafeGold.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = CafeGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "التسديد الجماعي عن الطاولة بالكامل:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CafeGold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مجموع طلبات الجلسة (${unpaidOrders.size} طلبات):", fontSize = 11.sp, color = CafeTextSecondary)
                            Text(OrderFeeCalculator.formatPrice(groupSubtotal, groupCurrency), fontSize = 11.sp, color = CafeTextPrimary, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نسبة 1% على إجمالي الجلسة (أقرب 250 د.ع):", fontSize = 11.sp, color = CafeGold)
                            Text(OrderFeeCalculator.formatPrice(groupFee, groupCurrency), fontSize = 11.sp, color = CafeGold, fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = CafeCardBorder, modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("المبلغ الإجمالي للتسديد الجماعي:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                            Text(
                                text = OrderFeeCalculator.formatPrice(groupTotal, groupCurrency),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CafeCyanPrimary
                            )
                        }

                        if (individualTotalSum > groupTotal) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💡 توفير التسديد الجماعي: ${OrderFeeCalculator.formatPrice(individualTotalSum - groupTotal, groupCurrency)} (حساب النسبة على المجموع الإجمالي أرخص من تسديد كل شخص منفرداً بالحد الأدنى).",
                                fontSize = 10.sp,
                                color = CafeGoldLight,
                                lineHeight = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onPayAllSession(unpaidOrders) },
                            colors = ButtonDefaults.buttonColors(containerColor = CafeCyanPrimary, contentColor = Color(0xFF032125)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "تسديد حساب الطاولة بالكامل (${OrderFeeCalculator.formatPrice(groupTotal, groupCurrency)}) 💳",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
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
        OrderStatus.DELIVERED, OrderStatus.READY -> OrderStatusDelivered
        OrderStatus.PAID -> OrderStatusPaid
        OrderStatus.CANCELLED -> OrderStatusCancelled
        OrderStatus.REJECTED -> OrderStatusRejected
    }
}

fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (60 * 1000)
    return when {
        minutes < 1 -> "الآن"
        minutes < 60 -> "منذ $minutes دقيقة"
        else -> {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
