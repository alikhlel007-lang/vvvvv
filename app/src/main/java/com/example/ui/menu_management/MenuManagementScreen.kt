package com.example.ui.menu_management

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MenuItem
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.cashier.BottomNavItem
import com.example.ui.customer.getCategoryIcon
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

@Composable
fun MenuManagementScreen(viewModel: MainViewModel) {
    val categories by viewModel.categories.collectAsState()
    val allItems by viewModel.menuItems.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val activeCatId = selectedCategory ?: categories.firstOrNull()?.id ?: "cat_burgers"
    val filteredItems = allItems.filter { it.categoryId == activeCatId }

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
                        text = "إدارة القوائم والأصناف",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextPrimary
                    )
                    Text(
                        text = "تعديل وإضافة الأصناف والأسعار",
                        fontSize = 12.sp,
                        color = CafeTextSecondary
                    )
                }

                Row {
                    Button(
                        onClick = { showAddCategoryDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CafeSurfaceVariant, contentColor = CafeCyanPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+ قسم جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { showAddItemDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CafeCyanPrimary, contentColor = Color(0xFF032125)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_add_item_dialog_btn")
                    ) {
                        Text("+ إضافة صنف", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // CATEGORIES ROW
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category.id == activeCatId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) CafeCyanPrimary else CafeSurface)
                            .border(1.dp, if (isSelected) CafeCyanPrimary else CafeCardBorder, RoundedCornerShape(14.dp))
                            .clickable { viewModel.selectCategory(category.id) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getCategoryIcon(category.iconType),
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF032125) else CafeCyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.nameAr,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF032125) else CafeTextPrimary
                            )
                        }
                    }
                }
            }

            // ITEMS LIST
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems) { item ->
                    ManageItemCard(
                        item = item,
                        onToggleAvailability = { viewModel.toggleItemAvailability(item) },
                        onDelete = { viewModel.deleteMenuItem(item) }
                    )
                }
            }
        }

        // ADD NEW ITEM DIALOG
        if (showAddItemDialog) {
            AddItemDialog(
                categoryId = activeCatId,
                onDismiss = { showAddItemDialog = false },
                onAdd = { nameAr, nameEn, descAr, price, imageType ->
                    viewModel.addMenuItem(
                        categoryId = activeCatId,
                        nameAr = nameAr,
                        nameEn = nameEn,
                        descriptionAr = descAr,
                        descriptionEn = descAr,
                        price = price,
                        imageType = imageType
                    )
                    showAddItemDialog = false
                }
            )
        }

        // ADD CATEGORY DIALOG
        if (showAddCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { showAddCategoryDialog = false },
                onAdd = { nameAr, nameEn, iconType ->
                    viewModel.addCategory(nameAr, nameEn, iconType)
                    showAddCategoryDialog = false
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
                    isSelected = true,
                    onClick = { /* already here */ }
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
fun ManageItemCard(
    item: MenuItem,
    onToggleAvailability: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CafeSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CafeCardBorder, CafeCardBorderGlow)), width = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imgRes = when (item.imageType) {
                "drink" -> R.drawable.food_drink
                else -> R.drawable.food_burger
            }
            Image(
                painter = painterResource(id = imgRes),
                contentDescription = item.nameAr,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nameAr,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = String.format("%.2f $", item.price),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CafeCyanPrimary
                )
                Text(
                    text = if (item.isAvailable) "متاح للطلب" else "غير متوفر حالياً",
                    fontSize = 11.sp,
                    color = if (item.isAvailable) Color(0xFF4CAF50) else Color(0xFFFF6B6B)
                )
            }

            // Availability Switch
            Switch(
                checked = item.isAvailable,
                onCheckedChange = { onToggleAvailability() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CafeCyanPrimary,
                    checkedTrackColor = Color(0xFF0F3B40),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = CafeInputBg
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف الصنف", tint = Color(0xFFFF6B6B), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AddItemDialog(
    categoryId: String,
    onDismiss: () -> Unit,
    onAdd: (nameAr: String, nameEn: String, descAr: String, price: Double, imageType: String) -> Unit
) {
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var descAr by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var imageType by remember { mutableStateOf("burger") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CafeSurface,
        title = { Text("إضافة صنف جديد", color = CafeTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("اسم الصنف (بالعربية)", color = CafeTextSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("add_item_name_input"),
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
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("اسم الصنف (بالإنجليزية)", color = CafeTextSecondary) },
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
                    value = descAr,
                    onValueChange = { descAr = it },
                    label = { Text("الوصف والمكونات", color = CafeTextSecondary) },
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
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("السعر ($)", color = CafeTextSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("add_item_price_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CafeInputBg,
                        unfocusedContainerColor = CafeInputBg,
                        focusedBorderColor = CafeCyanPrimary,
                        unfocusedBorderColor = CafeInputBorder,
                        focusedTextColor = CafeTextPrimary,
                        unfocusedTextColor = CafeTextPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf("burger" to "برجر", "drink" to "مشروب", "fries" to "مقبلات", "sweets" to "حلوى")
                    types.forEach { (type, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (imageType == type) CafeCyanPrimary else CafeInputBg)
                                .clickable { imageType = type }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (imageType == type) Color(0xFF032125) else CafeTextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 3.0
                    if (nameAr.isNotBlank()) {
                        onAdd(nameAr, nameEn.ifBlank { nameAr }, descAr, price, imageType)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CafeCyanPrimary, contentColor = Color(0xFF032125)),
                modifier = Modifier.testTag("confirm_add_item_btn")
            ) {
                Text("حفظ الصنف", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = CafeTextSecondary)
            }
        }
    )
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (nameAr: String, nameEn: String, iconType: String) -> Unit
) {
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var iconType by remember { mutableStateOf("burger") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CafeSurface,
        title = { Text("إضافة قسم جديد", color = CafeTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("اسم القسم (بالعربية)", color = CafeTextSecondary) },
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
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("اسم القسم (بالإنجليزية)", color = CafeTextSecondary) },
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
                    if (nameAr.isNotBlank()) {
                        onAdd(nameAr, nameEn.ifBlank { nameAr }, iconType)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CafeCyanPrimary, contentColor = Color(0xFF032125))
            ) {
                Text("إضافة القسم", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = CafeTextSecondary)
            }
        }
    )
}
