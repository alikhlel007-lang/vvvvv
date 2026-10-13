package com.example.ui.cashier

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
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.CafeSurface
import com.example.ui.theme.CafeSurfaceVariant
import com.example.ui.theme.CafeTextPrimary
import com.example.ui.theme.CafeTextSecondary
import com.example.ui.theme.OrderStatusCancelled
import com.example.ui.theme.OrderStatusNew
import com.example.ui.theme.OrderStatusPaid
import com.example.ui.theme.OrderStatusPreparing
import com.example.ui.theme.OrderStatusReady
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CashierOrdersScreen(viewModel: MainViewModel) {
    val orders by viewModel.orders.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeFilter by viewModel.orderStatusFilter.collectAsState()

    val filteredOrders = if (activeFilter == null) {
        orders
    } else {
        orders.filter { it.status == activeFilter }
    }

    val newOrdersCount = orders.count { it.status == OrderStatus.NEW }
    val todayRevenue = orders.filter { it.status == OrderStatus.PAID || it.status == OrderStatus.READY }.sumOf { it.totalPrice }
    val activeTablesCount = orders.filter { it.status != OrderStatus.PAID && it.status != OrderStatus.CANCELLED }.map { it.tableNumber }.distinct().size

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
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CafeSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (newOrdersCount > 0) CafeCyanPrimary else CafeTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "لوحة الكاشير والطلبات",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )
                        Text(
                            text = currentUser?.cafeName ?: "CAFE LUXURY",
                            fontSize = 12.sp,
                            color = CafeGold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Switch to live customer menu preview
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.CUSTOMER_MENU) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CafeSurfaceVariant)
                            .testTag("cashier_to_customer_preview_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "معاينة المنيو",
                            tint = CafeCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Logout
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CafeSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "تسجيل خروج",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // METRIC CARDS ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                    title = "إجمالي المبيعات",
                    value = String.format("%.1f $", todayRevenue),
                    valueColor = CafeCyanPrimary,
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // STATUS FILTER ROW
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    StatusFilterChip(
                        label = "الكل (${orders.size})",
                        selected = activeFilter == null,
                        color = CafeCyanPrimary,
                        onClick = { viewModel.setOrderStatusFilter(null) }
                    )
                }
                items(OrderStatus.values()) { status ->
                    val count = orders.count { it.status == status }
                    StatusFilterChip(
                        label = "${status.titleAr} ($count)",
                        selected = activeFilter == status,
                        color = getStatusColor(status),
                        onClick = { viewModel.setOrderStatusFilter(status) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد طلبات في هذه الحالة حالياً",
                            color = CafeTextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders) { order ->
                        CashierOrderCard(
                            order = order,
                            onAdvanceStatus = { nextStatus ->
                                viewModel.updateOrderStatus(order, nextStatus)
                            },
                            onDelete = {
                                viewModel.deleteOrder(order)
                            }
                        )
                    }
                }
            }
        }

        // BOTTOM NAVIGATION BAR (Cashier, Menu, Tables, Customer Mode)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(CafeSurface)
                .border(1.dp, CafeCardBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    title = "الكاشير",
                    icon = Icons.Default.Payment,
                    isSelected = true,
                    onClick = { /* already here */ },
                    badgeCount = newOrdersCount
                )
                BottomNavItem(
                    title = "القوائم والأصناف",
                    icon = Icons.Default.RestaurantMenu,
                    isSelected = false,
                    onClick = { viewModel.navigateTo(AppScreen.MENU_MANAGEMENT) }
                )
                BottomNavItem(
                    title = "باركود الطاولات",
                    icon = Icons.Default.QrCode,
                    isSelected = false,
                    onClick = { viewModel.navigateTo(AppScreen.TABLES_BARCODE) }
                )
                BottomNavItem(
                    title = "منيو الزبائن",
                    icon = Icons.Default.Fastfood,
                    isSelected = false,
                    onClick = { viewModel.navigateTo(AppScreen.CUSTOMER_MENU) }
                )
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
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = CafeTextSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor, maxLines = 1)
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

@Composable
fun CashierOrderCard(
    order: OrderRecord,
    onAdvanceStatus: (OrderStatus) -> Unit,
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

            HorizontalDivider(color = CafeCardBorder, modifier = Modifier.padding(vertical = 10.dp))

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
                        text = "ملاحظة الزبون: ",
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

            // Bottom action row: Total price and Status advance buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "المبلغ المطلوب", fontSize = 10.sp, color = CafeTextSecondary)
                    Text(
                        text = String.format("%.2f $", order.totalPrice),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CafeCyanPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (order.status) {
                        OrderStatus.NEW -> {
                            Button(
                                onClick = { onAdvanceStatus(OrderStatus.PREPARING) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrderStatusPreparing, contentColor = Color(0xFF1E1400)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("بدء التحضير 🍳", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OrderStatus.PREPARING -> {
                            Button(
                                onClick = { onAdvanceStatus(OrderStatus.READY) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrderStatusReady, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("جاهز للتقديم 🍽️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OrderStatus.READY -> {
                            Button(
                                onClick = { onAdvanceStatus(OrderStatus.PAID) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrderStatusPaid, contentColor = Color(0xFF032125)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("تم الدفع والحساب 💵", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OrderStatus.PAID, OrderStatus.CANCELLED -> {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CafeSurfaceVariant)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFFF6B6B), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) CafeCyanPrimary else CafeTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(CafeCyanPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$badgeCount",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF032125)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) CafeCyanPrimary else CafeTextSecondary
        )
    }
}

fun getStatusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.NEW -> OrderStatusNew
        OrderStatus.PREPARING -> OrderStatusPreparing
        OrderStatus.READY -> OrderStatusReady
        OrderStatus.PAID -> OrderStatusPaid
        OrderStatus.CANCELLED -> OrderStatusCancelled
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
