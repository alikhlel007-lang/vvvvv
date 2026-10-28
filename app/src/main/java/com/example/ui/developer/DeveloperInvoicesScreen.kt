package com.example.ui.developer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeveloperInvoicesScreen(
    viewModel: DeveloperViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val cafe by viewModel.selectedCafe.collectAsState()
    val invoices by viewModel.selectedCafeInvoices.collectAsState()
    val isLoading by viewModel.invoicesLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var invoiceToDelete by remember { mutableStateOf<AdminInvoiceItem?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val filteredInvoices = remember(invoices, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) {
            invoices
        } else {
            val cleanQ = q.removePrefix("#")
            invoices.filter {
                it.invoiceCode.contains(cleanQ) ||
                        it.tableNumber.toString().contains(cleanQ) ||
                        it.customerName.lowercase().contains(q) ||
                        it.itemsSummary.lowercase().contains(q) ||
                        it.id.lowercase().contains(q)
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault()) }

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
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Surface(
                color = Color(0xFF04161A),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = CafeCyanPrimary)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "فواتير ${cafe?.cafeName ?: "الكافتيريا"}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "المعرف: #${cafe?.numericId ?: ""} • ${invoices.size} فاتورة مسددة",
                            fontSize = 11.sp,
                            color = CafeCyanPrimary
                        )
                    }

                    IconButton(onClick = { cafe?.id?.let { viewModel.loadCafeInvoices(it) } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = CafeCyanPrimary)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bulk Delete Warning & Action Card
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF330E12)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "إدارة وحذف الفواتير الشاملة",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF8A80)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "يمكنك كأدمن حذف أي فاتورة فردية أو تصفير كافة فواتير هذه الكافتيريا دفعة واحدة بعد استلام رسوم الخدمة (1%).",
                                fontSize = 11.sp,
                                color = Color(0xFFFFCDD2)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showDeleteAllDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(10.dp),
                                enabled = invoices.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "حذف كافة فواتير الكافتيريا (${invoices.size}) دفعة واحدة ⚠️",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Invoices Search Field
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "ابحث بكود الفاتورة (#...) أو رقم الطاولة أو التفاصيل...",
                                color = CafeTextSecondary,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = CafeCyanPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
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
                } else if (filteredInvoices.isEmpty()) {
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
                                    Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = CafeTextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (searchQuery.isBlank()) "لا توجد فواتير مسجلة لهذه الكافتيريا" else "لا توجد نتائج تطابق البحث",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    items(filteredInvoices, key = { it.id }) { item ->
                        InvoiceItemCard(
                            item = item,
                            dateFormat = dateFormat,
                            onDelete = { invoiceToDelete = item },
                            onCopyCode = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Invoice Code", item.invoiceCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ كود الفاتورة: #${item.invoiceCode}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Delete Single Invoice Confirmation Dialog
        if (invoiceToDelete != null) {
            val inv = invoiceToDelete!!
            AlertDialog(
                onDismissRequest = { invoiceToDelete = null },
                containerColor = Color(0xFF091E23),
                title = {
                    Text("حذف الفاتورة #${inv.invoiceCode}", color = Color.White, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            text = "هل أنت متأكد من حذف هذه الفاتورة لطاولة ${inv.tableNumber} بمبلغ ${OrderFeeCalculator.formatPrice(inv.totalPrice, inv.currency)}؟",
                            color = Color(0xFFE0E0E0),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "سيتم حذف الفاتورة نهائياً من قاعدة بيانات فايربيس وتحديث المبيعات.",
                            color = Color(0xFFFF8A80),
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            cafe?.id?.let { cafeId ->
                                viewModel.deleteSingleInvoice(cafeId, inv.id)
                            }
                            invoiceToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("نعم، احذف الفاتورة", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { invoiceToDelete = null }) {
                        Text("إلغاء", color = CafeTextSecondary)
                    }
                }
            )
        }

        // Delete All Invoices Confirmation Dialog
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                containerColor = Color(0xFF091E23),
                icon = {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(36.dp))
                },
                title = {
                    Text("تصفير وحذف كافة فواتير الكافتيريا؟", color = Color.White, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            text = "أنت على وشك حذف كافة الفواتير المسددة (${invoices.size} فاتورة) الخاصة بـ ${cafe?.cafeName}.",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "تحذير: هذا الإجراء لا يمكن التراجع عنه. تأكد من استلام رسوم الخدمة 1% بالكامل قبل المتابعة.",
                            color = Color(0xFFFF8A80),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteAllDialog = false
                            viewModel.deleteAllInvoicesForSelectedCafe {
                                // back or refresh
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("تأكيد الحذف النهائي الشامل", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllDialog = false }) {
                        Text("إلغاء", color = CafeTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun InvoiceItemCard(
    item: AdminInvoiceItem,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit,
    onCopyCode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF051B20)),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Table Number + Invoice Code Badge + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CafeCyanPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "طاولة ${item.tableNumber}",
                            color = CafeCyanPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        onClick = onCopyCode,
                        color = Color(0xFF0B2E35),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "كود: #${item.invoiceCode}",
                                color = CafeGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "نسخ الكود",
                                tint = CafeGold,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف الفاتورة",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Items Summary
            if (item.itemsSummary.isNotBlank()) {
                Text(
                    text = item.itemsSummary,
                    fontSize = 12.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Price & Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(item.timestamp)),
                    fontSize = 11.sp,
                    color = CafeTextSecondary
                )

                Text(
                    text = OrderFeeCalculator.formatPrice(item.totalPrice, item.currency),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeCyanPrimary
                )
            }
        }
    }
}
