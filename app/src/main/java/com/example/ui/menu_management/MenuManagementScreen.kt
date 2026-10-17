package com.example.ui.menu_management

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.MenuCategory
import com.example.data.model.MenuItem
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
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
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val allItems by viewModel.menuItems.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var itemToConfigure by remember { mutableStateOf<MenuItem?>(null) }
    var categoryToDelete by remember { mutableStateOf<MenuCategory?>(null) }
    var categoryToEdit by remember { mutableStateOf<MenuCategory?>(null) }

    val activeCatId = selectedCategory ?: categories.firstOrNull()?.id ?: "cat_burgers"
    val activeCat = categories.find { it.id == activeCatId } ?: categories.firstOrNull()
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CafeSurfaceVariant)
                            .testTag("menu_mgmt_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "الرجوع للإعدادات",
                            tint = CafeCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "إدارة القوائم والأصناف",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )
                        Text(
                            text = "تعديل الأسعار، الصور، العروض وحذف الأقسام",
                            fontSize = 11.sp,
                            color = CafeTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { showAddCategoryDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CafeSurfaceVariant,
                            contentColor = CafeCyanPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("قسم جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { showAddItemDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CafeCyanPrimary,
                            contentColor = Color(0xFF032125)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_add_item_dialog_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة صنف", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ACTIVE CATEGORY ACTIONS HEADER (حذف القسم الحالي / تعديله)
            if (activeCat != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الأقسام المتاحة (${categories.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextSecondary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Edit Category Button
                        TextButton(
                            onClick = { categoryToEdit = activeCat },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = CafeCyanPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تعديل القسم", fontSize = 11.sp, color = CafeCyanPrimary)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Delete Category Button (خيار حذف القسم)
                        TextButton(
                            onClick = { categoryToDelete = activeCat },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف القسم", fontSize = 11.sp, color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // CATEGORIES ROW (Supports custom image or icon)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!category.customImageUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = category.customImageUri,
                                    contentDescription = category.nameAr,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = getCategoryIcon(category.iconType),
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF032125) else CafeCyanPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
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

            // ITEMS COUNT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredItems.size} صنف في ${activeCat?.nameAr ?: ""}",
                    fontSize = 12.sp,
                    color = CafeCyanPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "اضغط على الترس ⚙️ أمام أي صنف للخيارات الشاملة",
                    fontSize = 11.sp,
                    color = CafeTextSecondary
                )
            }

            // ITEMS LIST
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems) { item ->
                    ManageItemCard(
                        item = item,
                        onOpenGear = { itemToConfigure = item },
                        onToggleAvailability = { viewModel.toggleItemAvailability(item) }
                    )
                }
            }
        }

        // DIALOG: ITEM CONFIGURATION GEAR (ترس الصنف وخياراته الشاملة)
        itemToConfigure?.let { item ->
            ItemGearSettingsDialog(
                item = item,
                onDismiss = { itemToConfigure = null },
                onSave = { updatedItem ->
                    viewModel.updateMenuItem(updatedItem)
                    itemToConfigure = null
                    Toast.makeText(context, "تم حفظ إعدادات الصنف بنجاح", Toast.LENGTH_SHORT).show()
                },
                onDelete = {
                    viewModel.deleteMenuItem(item)
                    itemToConfigure = null
                    Toast.makeText(context, "تم حذف الصنف", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // DIALOG: ADD NEW ITEM
        if (showAddItemDialog) {
            AddItemDialog(
                categoryId = activeCatId,
                onDismiss = { showAddItemDialog = false },
                onAdd = { nameAr, nameEn, descAr, price, currency, imageType, imageUri, bgImageUri, isOffer, discount, offerTag ->
                    viewModel.addMenuItem(
                        categoryId = activeCatId,
                        nameAr = nameAr,
                        nameEn = nameEn,
                        descriptionAr = descAr,
                        descriptionEn = descAr,
                        price = price,
                        currency = currency,
                        imageType = imageType,
                        imageUri = imageUri,
                        bgImageUri = bgImageUri,
                        isOffer = isOffer,
                        offerDiscountPercent = discount,
                        offerTag = offerTag
                    )
                    showAddItemDialog = false
                    Toast.makeText(context, "تمت إضافة الصنف بنجاح", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // DIALOG: ADD CATEGORY (Allows custom image or icon)
        if (showAddCategoryDialog) {
            CategoryFormDialog(
                title = "إضافة قسم جديد",
                initialNameAr = "",
                initialNameEn = "",
                initialIcon = "burger",
                initialCustomImage = null,
                onDismiss = { showAddCategoryDialog = false },
                onSave = { nameAr, nameEn, iconType, customImageUri ->
                    viewModel.addCategory(nameAr, nameEn, iconType, customImageUri)
                    showAddCategoryDialog = false
                    Toast.makeText(context, "تمت إضافة القسم بنجاح", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // DIALOG: EDIT CATEGORY
        categoryToEdit?.let { cat ->
            CategoryFormDialog(
                title = "تعديل القسم",
                initialNameAr = cat.nameAr,
                initialNameEn = cat.nameEn,
                initialIcon = cat.iconType,
                initialCustomImage = cat.customImageUri,
                onDismiss = { categoryToEdit = null },
                onSave = { nameAr, nameEn, iconType, customImageUri ->
                    viewModel.updateCategory(
                        cat.copy(
                            nameAr = nameAr,
                            nameEn = nameEn,
                            iconType = iconType,
                            customImageUri = customImageUri
                        )
                    )
                    categoryToEdit = null
                    Toast.makeText(context, "تم تعديل القسم بنجاح", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // DIALOG: CONFIRM DELETE CATEGORY
        categoryToDelete?.let { cat ->
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                containerColor = CafeSurface,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "حذف قسم ${cat.nameAr}؟",
                        color = CafeTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "هل أنت متأكد من رغبتك في حذف هذا القسم؟",
                        color = CafeTextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCategory(cat)
                            categoryToDelete = null
                            Toast.makeText(context, "تم حذف القسم بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("نعم، احذف القسم", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToDelete = null }) {
                        Text("إلغاء", color = CafeTextSecondary)
                    }
                }
            )
        }
    }
}

/**
 * ManageItemCard - Features Gear Icon in front of every item as requested
 */
@Composable
fun ManageItemCard(
    item: MenuItem,
    onOpenGear: () -> Unit,
    onToggleAvailability: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CafeSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(CafeCardBorder, CafeCardBorderGlow)),
            width = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Image Preview (Custom or Resource)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF061A1D)),
                contentAlignment = Alignment.Center
            ) {
                if (!item.imageUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = item.imageUri,
                        contentDescription = item.nameAr,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val imgRes = when (item.imageType) {
                        "drink" -> R.drawable.food_drink
                        else -> R.drawable.food_burger
                    }
                    Image(
                        painter = painterResource(id = imgRes),
                        contentDescription = item.nameAr,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.nameAr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isOffer) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CafeGold.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔥 ${item.offerTag}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CafeGold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = com.example.util.OrderFeeCalculator.formatPrice(item.price, item.currency),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CafeCyanPrimary
                    )
                    if (item.isOffer && item.offerDiscountPercent > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "-${item.offerDiscountPercent}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B6B)
                        )
                    }
                }

                Text(
                    text = if (item.isAvailable) "متاح للطلب في المنيو" else "غير متوفر حالياً (مخفي)",
                    fontSize = 11.sp,
                    color = if (item.isAvailable) Color(0xFF4CAF50) else Color(0xFFFF6B6B)
                )
            }

            // Availability Quick Switch
            Switch(
                checked = item.isAvailable,
                onCheckedChange = { onToggleAvailability() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CafeCyanPrimary,
                    checkedTrackColor = Color(0xFF0F3B40),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = CafeInputBg
                ),
                modifier = Modifier.padding(end = 4.dp)
            )

            // GEAR ICON IN FRONT OF EVERY ITEM (ترس الإعدادات الشاملة لكل صنف)
            IconButton(
                onClick = onOpenGear,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CafeSurfaceVariant)
                    .border(1.dp, CafeCyanPrimary.copy(alpha = 0.4f), CircleShape)
                    .testTag("item_gear_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "خيارات وترس الصنف",
                    tint = CafeCyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * ItemGearSettingsDialog - Contains all required gear options:
 * 1. Price modification (تعديل السعر)
 * 2. Visibility / availability toggle (إظهار كمتوفرة أو إخفاؤها كغير متوفرة)
 * 3. Item image picker (اختيار صورة للصنف)
 * 4. Item background image picker (صورة خلفية للصنف أيضاً)
 * 5. Show as Offer toggle & discount (إظهار كعرض)
 */
@Composable
fun ItemGearSettingsDialog(
    item: MenuItem,
    onDismiss: () -> Unit,
    onSave: (MenuItem) -> Unit,
    onDelete: () -> Unit
) {
    var priceStr by remember { mutableStateOf(item.price.toString()) }
    var currency by remember { mutableStateOf(item.currency) }
    var isAvailable by remember { mutableStateOf(item.isAvailable) }
    var itemImageUri by remember { mutableStateOf(item.imageUri) }
    var itemBgImageUri by remember { mutableStateOf(item.bgImageUri) }
    var isOffer by remember { mutableStateOf(item.isOffer) }
    var discountPercentStr by remember { mutableStateOf(item.offerDiscountPercent.toString()) }
    var offerTag by remember { mutableStateOf(item.offerTag) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val itemImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            itemImageUri = uri.toString()
        }
    }

    val itemBgPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            itemBgImageUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CafeSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = CafeCyanPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "خيارات الصنف",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextPrimary
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = CafeTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = item.nameAr,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeGold
                )

                HorizontalDivider(color = CafeCardBorder)

                // 1. خيار تعديل السعر والعملة
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "1. تعديل السعر والعملة:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeCyanPrimary
                    )

                    // اختيار العملة (دينار عراقي أو دولار)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val currOptions = listOf("د.ع" to "دينار عراقي (د.ع)", "$" to "دولار أمريكي ($)")
                        currOptions.forEach { (code, label) ->
                            val isSel = currency == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) CafeCyanPrimary else CafeInputBg)
                                    .border(1.dp, if (isSel) CafeCyanPrimary else CafeInputBorder, RoundedCornerShape(10.dp))
                                    .clickable { currency = code }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color(0xFF032125) else CafeTextPrimary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("السعر ($currency)", color = CafeTextSecondary) },
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

                // 2. خيار إظهارها في المنيو كمتوفرة أو إخفاؤها كغير متوفرة
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CafeInputBg)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "2. حالة التوفر في المنيو:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CafeCyanPrimary
                            )
                            Text(
                                text = if (isAvailable) "إظهار كمتوفرة للزبائن" else "إخفاء كغير متوفرة للطلب",
                                fontSize = 11.sp,
                                color = if (isAvailable) Color(0xFF4CAF50) else Color(0xFFFF6B6B)
                            )
                        }
                        Switch(
                            checked = isAvailable,
                            onCheckedChange = { isAvailable = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CafeCyanPrimary,
                                checkedTrackColor = Color(0xFF0F3B40),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = CafeSurfaceVariant
                            )
                        )
                    }
                }

                // 3. خيار اختيار صورة للصنف
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CafeInputBg)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "3. صورة الصنف (رفع من الهاتف):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeCyanPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(55.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, CafeCyanPrimary, RoundedCornerShape(10.dp))
                                .background(Color(0xFF051C1F)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!itemImageUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = itemImageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = CafeTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            OutlinedButton(
                                onClick = {
                                    itemImagePicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اختيار صورة للصنف", fontSize = 11.sp, color = CafeCyanPrimary)
                            }

                            if (!itemImageUri.isNullOrEmpty()) {
                                TextButton(
                                    onClick = { itemImageUri = null },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("إعادة للأصل", fontSize = 10.sp, color = Color(0xFFFF6B6B))
                                }
                            }
                        }
                    }
                }

                // 4. خيار صورة خلفية للصنف أيضاً
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CafeInputBg)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "4. صورة خلفية للصنف (اختياري):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeCyanPrimary
                    )
                    Text(
                        text = "تظهر كخلفية جمالية لبطاقة الصنف في المنيو وصفحة العروض",
                        fontSize = 10.sp,
                        color = CafeTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(55.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, CafeGold, RoundedCornerShape(10.dp))
                                .background(Color(0xFF051C1F)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!itemBgImageUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = itemBgImageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Wallpaper,
                                    contentDescription = null,
                                    tint = CafeGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            OutlinedButton(
                                onClick = {
                                    itemBgPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اختيار صورة خلفية", fontSize = 11.sp, color = CafeGold)
                            }

                            if (!itemBgImageUri.isNullOrEmpty()) {
                                TextButton(
                                    onClick = { itemBgImageUri = null },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("إزالة الخلفية", fontSize = 10.sp, color = Color(0xFFFF6B6B))
                                }
                            }
                        }
                    }
                }

                // 5. خيار إظهار كعرض (في صفحة العروض والتخفيضات والمواد الجديدة)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CafeInputBg)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = CafeGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "5. إظهار كعرض في المنيو:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CafeGold
                                )
                            }
                            Text(
                                text = "يظهر في صفحة العروض والتخفيضات والمواد الجديدة",
                                fontSize = 10.sp,
                                color = CafeTextSecondary
                            )
                        }
                        Switch(
                            checked = isOffer,
                            onCheckedChange = { isOffer = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CafeGold,
                                checkedTrackColor = Color(0xFF382F0F)
                            )
                        )
                    }

                    if (isOffer) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = discountPercentStr,
                                onValueChange = { discountPercentStr = it },
                                label = { Text("نسبة الخصم %", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CafeSurface,
                                    unfocusedContainerColor = CafeSurface,
                                    focusedBorderColor = CafeGold,
                                    unfocusedBorderColor = CafeInputBorder,
                                    focusedTextColor = CafeTextPrimary,
                                    unfocusedTextColor = CafeTextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = offerTag,
                                onValueChange = { offerTag = it },
                                label = { Text("شارة العرض", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CafeSurface,
                                    unfocusedContainerColor = CafeSurface,
                                    focusedBorderColor = CafeGold,
                                    unfocusedBorderColor = CafeInputBorder,
                                    focusedTextColor = CafeTextPrimary,
                                    unfocusedTextColor = CafeTextPrimary
                                )
                            )
                        }
                    }
                }

                // Delete Item Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDeleteConfirm = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حذف هذا الصنف نهائياً", color = Color(0xFFFF6B6B), fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceStr.toDoubleOrNull() ?: item.price
                    val disc = discountPercentStr.toIntOrNull() ?: 0
                    val updated = item.copy(
                        price = p,
                        currency = currency,
                        isAvailable = isAvailable,
                        imageUri = itemImageUri,
                        bgImageUri = itemBgImageUri,
                        isOffer = isOffer,
                        offerDiscountPercent = disc,
                        offerTag = offerTag.ifBlank { "عرض خاص" }
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CafeCyanPrimary,
                    contentColor = Color(0xFF032125)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ التعديلات", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = CafeTextSecondary)
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = CafeSurface,
            shape = RoundedCornerShape(20.dp),
            title = { Text("تأكيد حذف الصنف", color = CafeTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف ${item.nameAr} نهائياً؟", color = CafeTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("نعم، احذف", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء", color = CafeTextSecondary)
                }
            }
        )
    }
}

/**
 * AddItemDialog
 */
@Composable
fun AddItemDialog(
    categoryId: String,
    onDismiss: () -> Unit,
    onAdd: (
        nameAr: String,
        nameEn: String,
        descAr: String,
        price: Double,
        currency: String,
        imageType: String,
        imageUri: String?,
        bgImageUri: String?,
        isOffer: Boolean,
        discountPercent: Int,
        offerTag: String
    ) -> Unit
) {
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var descAr by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("د.ع") } // الافتراضي: دينار
    var imageType by remember { mutableStateOf("burger") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var bgImageUri by remember { mutableStateOf<String?>(null) }
    var isOffer by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) imageUri = uri.toString()
    }

    val bgPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) bgImageUri = uri.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CafeSurface,
        shape = RoundedCornerShape(20.dp),
        title = { Text("إضافة صنف جديد", color = CafeTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

                // خيار العملة: دولار / دينار (الافتراضي دينار)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "عملة الصنف:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val currOptions = listOf("د.ع" to "دينار (د.ع)", "$" to "دولار ($)")
                        currOptions.forEach { (code, label) ->
                            val isSel = currency == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) CafeCyanPrimary else CafeInputBg)
                                    .border(1.dp, if (isSel) CafeCyanPrimary else CafeInputBorder, RoundedCornerShape(10.dp))
                                    .clickable { currency = code }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color(0xFF032125) else CafeTextPrimary
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("السعر ($currency)", color = CafeTextSecondary) },
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

                // Image Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (imageUri != null) "تم اختيار صورة" else "صورة الصنف", fontSize = 11.sp, color = CafeCyanPrimary)
                    }

                    OutlinedButton(
                        onClick = {
                            bgPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (bgImageUri != null) "تمت الخلفية" else "صورة خلفية", fontSize = 11.sp, color = CafeGold)
                    }
                }

                // Show as Offer Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CafeInputBg)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("إظهار في صفحة العروض والتخفيضات؟", fontSize = 11.sp, color = CafeTextPrimary)
                    Switch(
                        checked = isOffer,
                        onCheckedChange = { isOffer = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CafeGold)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 3.0
                    if (nameAr.isNotBlank()) {
                        onAdd(
                            nameAr,
                            nameEn.ifBlank { nameAr },
                            descAr,
                            price,
                            currency,
                            imageType,
                            imageUri,
                            bgImageUri,
                            isOffer,
                            if (isOffer) 15 else 0,
                            if (isOffer) "عرض خاص" else ""
                        )
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

/**
 * CategoryFormDialog - Supports uploading custom image from device OR choosing an icon!
 */
@Composable
fun CategoryFormDialog(
    title: String,
    initialNameAr: String,
    initialNameEn: String,
    initialIcon: String,
    initialCustomImage: String?,
    onDismiss: () -> Unit,
    onSave: (nameAr: String, nameEn: String, iconType: String, customImageUri: String?) -> Unit
) {
    var nameAr by remember { mutableStateOf(initialNameAr) }
    var nameEn by remember { mutableStateOf(initialNameEn) }
    var iconType by remember { mutableStateOf(initialIcon) }
    var customImageUri by remember { mutableStateOf(initialCustomImage) }

    val categoryPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customImageUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CafeSurface,
        shape = RoundedCornerShape(20.dp),
        title = { Text(title, color = CafeTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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

                // Option 1: رفع صورة من الجهاز للقسم
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CafeInputBg)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "1. رفع صورة للقسم من الجهاز:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeCyanPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, CafeCyanPrimary, RoundedCornerShape(10.dp))
                                .background(Color(0xFF04171A)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!customImageUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = customImageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = CafeTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            OutlinedButton(
                                onClick = {
                                    categoryPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اختيار صورة للقسم", fontSize = 11.sp, color = CafeCyanPrimary)
                            }

                            if (!customImageUri.isNullOrEmpty()) {
                                TextButton(
                                    onClick = { customImageUri = null },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("حذف الصورة والاعتماد على الأيقونة", fontSize = 10.sp, color = Color(0xFFFF6B6B))
                                }
                            }
                        }
                    }
                }

                // Option 2: أو اختيار أيقونة
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CafeInputBg)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "2. أو اختيار أيقونة القسم:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeCyanPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val icons = listOf(
                        "burger" to Icons.Default.Fastfood,
                        "fries" to Icons.Default.Fastfood,
                        "drink" to Icons.Default.LocalDrink,
                        "meals" to Icons.Default.Restaurant,
                        "sweets" to Icons.Default.Cake,
                        "pizza" to Icons.Default.LocalPizza,
                        "cafe" to Icons.Default.LocalCafe
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(icons) { (type, iconVector) ->
                            val isSel = iconType == type
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) CafeCyanPrimary else CafeSurfaceVariant)
                                    .border(1.dp, if (isSel) CafeCyanPrimary else CafeInputBorder, RoundedCornerShape(10.dp))
                                    .clickable { iconType = type },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = type,
                                    tint = if (isSel) Color(0xFF032125) else CafeTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameAr.isNotBlank()) {
                        onSave(nameAr, nameEn.ifBlank { nameAr }, iconType, customImageUri)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CafeCyanPrimary,
                    contentColor = Color(0xFF032125)
                )
            ) {
                Text("حفظ القسم", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = CafeTextSecondary)
            }
        }
    )
}
