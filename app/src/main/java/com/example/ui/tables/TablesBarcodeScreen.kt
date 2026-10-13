package com.example.ui.tables

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CafeTable
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.cashier.BottomNavItem
import com.example.ui.theme.CafeCardBorder
import com.example.ui.theme.CafeCardBorderGlow
import com.example.ui.theme.CafeCyanPrimary
import com.example.ui.theme.CafeDarkBg
import com.example.ui.theme.CafeDarkBgGradientEnd
import com.example.ui.theme.CafeGold
import com.example.ui.theme.CafeInputBg
import com.example.ui.theme.CafeInputBorder
import com.example.ui.theme.CafeSurface
import com.example.ui.theme.CafeSurfaceVariant
import com.example.ui.theme.CafeTextPrimary
import com.example.ui.theme.CafeTextSecondary
import com.example.util.PdfExporter
import com.example.util.QrCodeGenerator

@Composable
fun TablesBarcodeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val tables by viewModel.tables.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddTableDialog by remember { mutableStateOf(false) }
    var isExportingPdf by remember { mutableStateOf(false) }

    val cafeName = currentUser?.cafeName ?: "CAFE LUXURY"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CafeDarkBg, CafeDarkBgGradientEnd, Color(0xFF021215))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "باركودات الطاولات",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextPrimary
                    )
                    Text(
                        text = "${tables.size} طاولة مسجلة في الكافيه",
                        fontSize = 12.sp,
                        color = CafeTextSecondary
                    )
                }

                Button(
                    onClick = { showAddTableDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CafeCyanPrimary, contentColor = Color(0xFF032125)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_table_btn")
                ) {
                    Text("+ إضافة طاولة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // PDF EXPORT HERO CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable {
                        isExportingPdf = true
                        val file = PdfExporter.generateAndShareTablesPdf(context, cafeName, tables)
                        isExportingPdf = false
                        if (file != null) {
                            Toast.makeText(context, "تم تجهيز ملف PDF بنجاح!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "فشل إنشاء ملف PDF", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .testTag("export_pdf_button"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CafeSurfaceVariant),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(CafeCyanPrimary, CafeGold)),
                    width = 1.4.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(CafeCyanPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            tint = Color(0xFF032125),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تحميل وطباعة باركودات الطاولات (PDF)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "جاهز للطباعة بحجم A4 وبطاقات مخصصة لكل طاولة",
                            fontSize = 11.sp,
                            color = CafeTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CafeCyanPrimary)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isExportingPdf) "جاري التحميل..." else "تصدير الآن",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF032125)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TABLES GRID
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tables) { table ->
                    TableQrCard(
                        table = table,
                        onOpenMenu = {
                            viewModel.setSelectedTableNumber(table.tableNumber)
                            viewModel.navigateTo(AppScreen.CUSTOMER_MENU)
                        },
                        onDelete = { viewModel.deleteTable(table) }
                    )
                }
            }
        }

        // ADD TABLE DIALOG
        if (showAddTableDialog) {
            AddTableDialog(
                currentCount = tables.size,
                onDismiss = { showAddTableDialog = false },
                onAdd = { number, label, capacity ->
                    viewModel.addTable(number, label, capacity)
                    showAddTableDialog = false
                }
            )
        }

        // BOTTOM NAV
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
                    isSelected = false,
                    onClick = { viewModel.navigateTo(AppScreen.CASHIER_ORDERS) }
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
                    isSelected = true,
                    onClick = { /* already here */ }
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
fun TableQrCard(
    table: CafeTable,
    onOpenMenu: () -> Unit,
    onDelete: () -> Unit
) {
    val qrBitmap = remember(table.qrPayload) {
        QrCodeGenerator.generateQrBitmap(
            content = table.qrPayload,
            size = 280,
            darkColor = android.graphics.Color.WHITE,
            lightColor = android.graphics.Color.TRANSPARENT
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("table_card_${table.tableNumber}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CafeSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(listOf(CafeCardBorder, CafeCardBorderGlow)),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Table Number & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = table.label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeCyanPrimary
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف الطاولة",
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // QR Code Box
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF061A1D))
                    .border(1.dp, CafeCardBorder, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code ${table.label}",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = CafeCyanPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${table.capacity} مقاعد",
                fontSize = 11.sp,
                color = CafeTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Button to simulate customer scanning this QR code
            Button(
                onClick = onOpenMenu,
                colors = ButtonDefaults.buttonColors(containerColor = CafeSurfaceVariant, contentColor = CafeCyanPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(36.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("فتح منيو الطاولة", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun AddTableDialog(
    currentCount: Int,
    onDismiss: () -> Unit,
    onAdd: (number: Int, label: String, capacity: Int) -> Unit
) {
    var numberStr by remember { mutableStateOf("${currentCount + 1}") }
    var label by remember { mutableStateOf("طاولة ${currentCount + 1}") }
    var capacityStr by remember { mutableStateOf("4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CafeSurface,
        title = { Text("إضافة طاولة جديدة", color = CafeTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = numberStr,
                    onValueChange = {
                        numberStr = it
                        label = "طاولة $it"
                    },
                    label = { Text("رقم الطاولة", color = CafeTextSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("add_table_number_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CafeInputBg,
                        unfocusedContainerColor = CafeInputBg,
                        focusedBorderColor = CafeCyanPrimary,
                        unfocusedBorderColor = CafeInputBorder,
                        focusedTextColor = CafeTextPrimary,
                        unfocusedTextColor = CafeTextPrimary
                    )
                )

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("تسمية الطاولة (مثال: طاولة 5 - قسم العوائل)", color = CafeTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CafeInputBg,
                        unfocusedContainerColor = CafeInputBg,
                        focusedBorderColor = CafeCyanPrimary,
                        unfocusedBorderColor = CafeInputBorder,
                        focusedTextColor = CafeTextPrimary,
                        unfocusedTextColor = CafeTextPrimary
                    )
                )

                OutlinedTextField(
                    value = capacityStr,
                    onValueChange = { capacityStr = it },
                    label = { Text("عدد المقاعد / السعة", color = CafeTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CafeInputBg,
                        unfocusedContainerColor = CafeInputBg,
                        focusedBorderColor = CafeCyanPrimary,
                        unfocusedBorderColor = CafeInputBorder,
                        focusedTextColor = CafeTextPrimary,
                        unfocusedTextColor = CafeTextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val num = numberStr.toIntOrNull() ?: (currentCount + 1)
                    val cap = capacityStr.toIntOrNull() ?: 4
                    onAdd(num, label, cap)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CafeCyanPrimary, contentColor = Color(0xFF032125))
            ) {
                Text("توليد الباركود وحفظ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = CafeTextSecondary)
            }
        }
    )
}
