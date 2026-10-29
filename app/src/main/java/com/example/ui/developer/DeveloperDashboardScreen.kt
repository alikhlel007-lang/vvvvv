package com.example.ui.developer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CafeCyanPrimary
import com.example.ui.theme.CafeGold
import com.example.ui.theme.CafeTextSecondary
import com.example.util.OrderFeeCalculator

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeveloperDashboardScreen(
    viewModel: DeveloperViewModel,
    onOpenCashierApp: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cafeterias by viewModel.filteredCafeterias.collectAsState()
    val allCafes by viewModel.allCafeterias.collectAsState()

    val totalSales by viewModel.totalSystemSales.collectAsState()
    val totalFees by viewModel.totalServiceFees.collectAsState()
    val collectedFees by viewModel.totalCollectedFees.collectAsState()
    val remainingFees by viewModel.totalRemainingFees.collectAsState()

    val cafeForCollect by viewModel.cafeForCollection.collectAsState()
    val cafeForHistory by viewModel.cafeForHistory.collectAsState()
    val historyRecords by viewModel.historyRecords.collectAsState()
    val historyLoading by viewModel.historyLoading.collectAsState()

    val statusMsg by viewModel.statusMessage.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMsg, errorMsg) {
        val msg = statusMsg ?: errorMsg
        if (!msg.isNullOrBlank()) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020C0E),
                        Color(0xFF051B20),
                        Color(0xFF01080A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Surface(
                color = Color(0xFF04161A),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(CafeCyanPrimary, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF021215))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "لوحة تحكم المطور",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تحصيل رسوم الخدمة 1% • إدارة النظام",
                                color = CafeCyanPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.refreshDashboard() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = CafeCyanPrimary)
                        }
                        if (onOpenCashierApp != null) {
                            IconButton(onClick = onOpenCashierApp) {
                                Icon(Icons.Default.Storefront, contentDescription = "تطبيق الكاشير", tint = CafeGold)
                            }
                        }
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "خروج", tint = Color(0xFFFF5252))
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Summary Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "الكافتيريات المسجلة",
                            value = "${allCafes.size}",
                            subtitle = "كافتيريا نشطة في النظام",
                            icon = Icons.Default.Storefront,
                            accentColor = CafeCyanPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "إجمالي المبيعات",
                            value = OrderFeeCalculator.formatPrice(totalSales, "د.ع"),
                            subtitle = "المبيعات المسددة بالكامل",
                            icon = Icons.Default.Payments,
                            accentColor = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "استحقاق المطور (1%)",
                            value = OrderFeeCalculator.formatPrice(totalFees, "د.ع"),
                            subtitle = "مجموع رسوم الخدمة",
                            icon = Icons.Default.MonetizationOn,
                            accentColor = CafeGold,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "المتبقي للتحصيل",
                            value = OrderFeeCalculator.formatPrice(remainingFees, "د.ع"),
                            subtitle = "مستحقات غير محصلة بعد",
                            icon = Icons.Default.ReceiptLong,
                            accentColor = if (remainingFees > 0) Color(0xFFFF8A80) else Color(0xFF81C784),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = {
                            Text(
                                "ابحث بالرقم التعريفي (مثل #10482) أو اسم الكافتيريا أو الهاتف...",
                                color = CafeTextSecondary,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = CafeCyanPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح", tint = CafeTextSecondary)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CafeCyanPrimary,
                            unfocusedBorderColor = Color(0xFF0E3F47),
                            focusedContainerColor = Color(0xFF04171B),
                            unfocusedContainerColor = Color(0xFF04171B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CafeCyanPrimary)
                        }
                    }
                } else if (cafeterias.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF051A1E)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = CafeTextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (searchQuery.isBlank()) "لا توجد كافتيريات مسجلة حتى الآن" else "لم يتم العثور على نتائج للبحث",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "عندما تسجل كافتيريا جديدة أو تبدأ طلباتها ستظهر هنا فوراً",
                                    color = CafeTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    items(cafeterias, key = { it.id }) { cafe ->
                        CafeteriaAdminCard(
                            cafe = cafe,
                            onOpenInvoices = { viewModel.openInvoices(cafe) },
                            onCollectFee = { viewModel.openCollectDialog(cafe) },
                            onOpenHistory = { viewModel.openHistoryDialog(cafe) },
                            onToggleStatus = { viewModel.toggleCafeStatus(cafe) },
                            onCopyId = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Cafeteria ID", "#${cafe.numericId}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ الرقم التعريفي: #${cafe.numericId}", Toast.LENGTH_SHORT).show()
                            },
                            onWhatsApp = {
                                if (cafe.phone.isNotBlank()) {
                                    val cleanPhone = cafe.phone.trim().removePrefix("+").removePrefix("00")
                                    val formattedPhone = if (cleanPhone.startsWith("07")) "964" + cleanPhone.substring(1) else cleanPhone
                                    val msg = "مرحباً إدارة ${cafe.cafeName} 👋\nنظام Q-Cafe لإدارة الكافتيريات."
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formattedPhone?text=${Uri.encode(msg)}"))
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "تطبيق واتساب غير مثبت", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "رقم الهاتف غير مسجل", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCall = {
                                if (cafe.phone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cafe.phone.trim()}"))
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "رقم الهاتف غير مسجل", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Dialog for Collecting Fee
        if (cafeForCollect != null) {
            DeveloperCollectFeeDialog(
                cafe = cafeForCollect!!,
                onDismiss = { viewModel.closeCollectDialog() },
                onConfirm = { amount, note ->
                    viewModel.confirmFeeCollection(cafeForCollect!!, amount, note)
                }
            )
        }

        // Dialog for Collections History
        if (cafeForHistory != null) {
            DeveloperFeeHistoryDialog(
                cafe = cafeForHistory!!,
                records = historyRecords,
                isLoading = historyLoading,
                onDismiss = { viewModel.closeHistoryDialog() }
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF051E23)),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = CafeTextSecondary)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 9.sp, color = CafeTextSecondary)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CafeteriaAdminCard(
    cafe: CafeteriaAdminSummary,
    onOpenInvoices: () -> Unit,
    onCollectFee: () -> Unit,
    onOpenHistory: () -> Unit,
    onToggleStatus: () -> Unit,
    onCopyId: () -> Unit,
    onWhatsApp: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF051B20)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Cafe Name + Numeric ID Badge + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cafe.cafeName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "المالك: ${cafe.ownerName.ifBlank { "غير مسجل" }}",
                        fontSize = 12.sp,
                        color = CafeTextSecondary
                    )
                }

                // Numeric ID Badge (Clickable to copy)
                Surface(
                    onClick = onCopyId,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0C3840),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "معرّف: #${cafe.numericId}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeCyanPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "نسخ المعرف",
                            tint = CafeCyanPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Active / Blocked Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (cafe.isActive) Color(0xFF1B4D2E) else Color(0xFF5A1C20)
                ) {
                    Text(
                        text = if (cafe.isActive) "نشط" else "موقوف",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (cafe.isActive) Color(0xFF81C784) else Color(0xFFFF8A80),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contact info row
            if (cafe.phone.isNotBlank() || cafe.email.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cafe.phone.isNotBlank()) {
                        Text(
                            text = "📞 ${cafe.phone}",
                            fontSize = 12.sp,
                            color = CafeCyanPrimary,
                            modifier = Modifier.clickable { onCall() }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    if (cafe.email.isNotBlank()) {
                        Text(
                            text = "✉️ ${cafe.email}",
                            fontSize = 11.sp,
                            color = CafeTextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Financial Summary Row
            Surface(
                color = Color(0xFF08262C),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "المبيعات (${cafe.paidOrdersCount} طلب)", fontSize = 10.sp, color = CafeTextSecondary)
                        Text(
                            text = OrderFeeCalculator.formatPrice(cafe.totalSales, cafe.currency),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column {
                        Text(text = "العمولة (1%)", fontSize = 10.sp, color = CafeTextSecondary)
                        Text(
                            text = OrderFeeCalculator.formatPrice(cafe.serviceFeeTotal, cafe.currency),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeGold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "المتبقي للتحصيل", fontSize = 10.sp, color = CafeTextSecondary)
                        Text(
                            text = OrderFeeCalculator.formatPrice(cafe.remainingFee, cafe.currency),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cafe.remainingFee > 0) Color(0xFFFF8A80) else Color(0xFF81C784)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Invoices button
                Button(
                    onClick = onOpenInvoices,
                    colors = ButtonDefaults.buttonColors(containerColor = CafeCyanPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF021215), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("الفواتير والأرشيف", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF021215))
                }

                // Collect Fee button
                Button(
                    onClick = onCollectFee,
                    colors = ButtonDefaults.buttonColors(containerColor = CafeGold),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF021215), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تحصيل رسوم", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF021215))
                }

                // History button
                OutlinedButton(
                    onClick = onOpenHistory,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CafeTextSecondary),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("السجل", fontSize = 12.sp)
                }

                // WhatsApp button
                if (cafe.phone.isNotBlank()) {
                    OutlinedButton(
                        onClick = onWhatsApp,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4CAF50)),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("💬 واتساب", fontSize = 12.sp)
                    }
                }

                // Toggle Active / Block button
                OutlinedButton(
                    onClick = onToggleStatus,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (cafe.isActive) Color(0xFFFF5252) else Color(0xFF81C784)
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        if (cafe.isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (cafe.isActive) "إيقاف" else "تفعيل", fontSize = 12.sp)
                }
            }
        }
    }
}
