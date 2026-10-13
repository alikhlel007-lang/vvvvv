package com.example.ui.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CartItem
import com.example.data.model.MenuCategory
import com.example.data.model.MenuItem
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.CafePrimaryButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMenuScreen(viewModel: MainViewModel) {
    val categories by viewModel.categories.collectAsState()
    val allItems by viewModel.menuItems.collectAsState()
    val tables by viewModel.tables.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val selectedTableNumber by viewModel.selectedTableNumber.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showSearchBox by remember { mutableStateOf(false) }
    var showCartSheet by remember { mutableStateOf(false) }
    var showTablePicker by remember { mutableStateOf(false) }
    var orderSubmittedSuccessId by remember { mutableStateOf<String?>(null) }
    var customerNotes by remember { mutableStateOf("") }

    // Active category
    val activeCatId = selectedCategory ?: categories.firstOrNull()?.id ?: "cat_burgers"
    val activeCat = categories.find { it.id == activeCatId } ?: categories.firstOrNull()

    // Filter items
    val filteredItems = allItems.filter { item ->
        val matchesCategory = (selectedCategory == null || item.categoryId == activeCatId)
        val matchesSearch = searchQuery.isBlank() ||
                item.nameAr.contains(searchQuery, ignoreCase = true) ||
                item.nameEn.contains(searchQuery, ignoreCase = true) ||
                item.descriptionAr.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val cartCount = cart.sumOf { it.quantity }
    val cartTotal = cart.sumOf { it.menuItem.price * it.quantity }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        CafeDarkBg,
                        CafeDarkBgGradientEnd,
                        Color(0xFF031417)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TOP HEADER BAR (Hamburger, CAFE LOGO, Search)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Return to Cashier Dashboard / Drawer icon
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.CASHIER_ORDERS) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CafeSurfaceVariant)
                        .testTag("menu_to_cashier_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = "لوحة الكاشير",
                        tint = CafeCyanPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Middle Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showTablePicker = true }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cafe_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CAFE LUXURY",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CafeTextPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "طاولة $selectedTableNumber (تغيير)",
                            fontSize = 11.sp,
                            color = CafeGold,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Search Toggle
                IconButton(
                    onClick = { showSearchBox = !showSearchBox },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CafeSurfaceVariant)
                        .testTag("menu_search_toggle")
                ) {
                    Icon(
                        imageVector = if (showSearchBox) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = CafeCyanPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Search box if toggled
            AnimatedVisibility(visible = showSearchBox) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("ابحث عن صنف أو مشروب...", color = CafeTextSecondary, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("menu_search_input"),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CafeInputBg,
                            unfocusedContainerColor = CafeInputBg,
                            focusedBorderColor = CafeCyanPrimary,
                            unfocusedBorderColor = CafeInputBorder,
                            focusedTextColor = CafeTextPrimary,
                            unfocusedTextColor = CafeTextPrimary
                        ),
                        singleLine = true
                    )
                }
            }

            // HORIZONTAL CATEGORIES CAROUSEL (Matching Screenshot 2)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category.id == activeCatId
                    CategoryPill(
                        category = category,
                        isSelected = isSelected,
                        onClick = { viewModel.selectCategory(category.id) }
                    )
                }
            }

            // SECTION HEADER (Category Title + Item Count)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredItems.size} صنف",
                    fontSize = 13.sp,
                    color = CafeCyanPrimary,
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = activeCat?.nameAr ?: "الأصناف",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = getCategoryIcon(activeCat?.iconType ?: "burger"),
                        contentDescription = null,
                        tint = CafeCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // MENU FOOD ITEMS LIST
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = if (cartCount > 0) 100.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredItems) { item ->
                    MenuItemCard(
                        item = item,
                        onAddToCart = { viewModel.addToCart(item) }
                    )
                }
            }
        }

        // FLOATING BOTTOM CART BAR (Matching Screenshot 2 bottom: Cart badge, Total $, "عرض الطلب ->")
        AnimatedVisibility(
            visible = cartCount > 0,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                shape = RoundedCornerShape(34.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF032226)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(CafeCyanPrimary, CafeCardBorderGlow)),
                    width = 1.2.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cart icon with count badge
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(CafeSurfaceVariant)
                            .clickable { showCartSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "السلة",
                            tint = CafeCyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        // Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(CafeCyanPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$cartCount",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF032125)
                            )
                        }
                    }

                    // Total Sum
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showCartSheet = true }
                    ) {
                        Text(
                            text = "المجموع الكلي",
                            fontSize = 11.sp,
                            color = CafeTextSecondary
                        )
                        Text(
                            text = String.format("%.2f $", cartTotal),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CafeCyanPrimary
                        )
                    }

                    // "عرض الطلب ->" Action Button
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(CafeCyanPrimary)
                            .clickable { showCartSheet = true }
                            .padding(horizontal = 20.dp)
                            .testTag("view_order_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "عرض الطلب",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF032125)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF032125),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // CART & CHECKOUT BOTTOM SHEET
        if (showCartSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCartSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = CafeSurface,
                dragHandle = null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showCartSheet = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = CafeTextSecondary)
                        }
                        Text(
                            text = "طلب طاولة $selectedTableNumber",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("تفريغ", color = Color(0xFFFF6B6B), fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = CafeCardBorder, modifier = Modifier.padding(vertical = 10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(cart) { cartItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CafeSurfaceVariant)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cartItem.menuItem.nameAr,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CafeTextPrimary
                                    )
                                    Text(
                                        text = String.format("%.2f $", cartItem.menuItem.price * cartItem.quantity),
                                        fontSize = 13.sp,
                                        color = CafeCyanPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.removeFromCart(cartItem.menuItem) },
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(CafeInputBg)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "تقليل",
                                            tint = CafeCyanPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Text(
                                        text = "${cartItem.quantity}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CafeTextPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                    IconButton(
                                        onClick = { viewModel.addToCart(cartItem.menuItem) },
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(CafeCyanPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "زيادة",
                                            tint = Color(0xFF032125),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Special Notes
                    OutlinedTextField(
                        value = customerNotes,
                        onValueChange = { customerNotes = it },
                        placeholder = { Text("ملاحظات خاصة (مثال: بدون سكر، بدون بصل، زيادة صوص...)", fontSize = 12.sp, color = CafeTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CafeInputBg,
                            unfocusedContainerColor = CafeInputBg,
                            focusedBorderColor = CafeCyanPrimary,
                            unfocusedBorderColor = CafeInputBorder,
                            focusedTextColor = CafeTextPrimary,
                            unfocusedTextColor = CafeTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الإجمالي الكلي:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                        Text(String.format("%.2f $", cartTotal), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = CafeCyanPrimary)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    CafePrimaryButton(
                        text = "إرسال الطلب إلى الكاشير",
                        onClick = {
                            viewModel.submitOrder(customerNotes) { orderId ->
                                orderSubmittedSuccessId = orderId
                                customerNotes = ""
                                showCartSheet = false
                            }
                        },
                        testTag = "submit_order_to_cashier_btn"
                    )
                }
            }
        }

        // TABLE PICKER DIALOG (Allow simulating scanning any table QR!)
        if (showTablePicker) {
            AlertDialog(
                onDismissRequest = { showTablePicker = false },
                containerColor = CafeSurface,
                title = { Text("اختر رقم الطاولة", color = CafeTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("اختر الطاولة لتجربة إرسال الطلب منها:", color = CafeTextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(tables) { table ->
                                val isCur = table.tableNumber == selectedTableNumber
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isCur) CafeCyanPrimary else CafeSurfaceVariant)
                                        .clickable {
                                            viewModel.setSelectedTableNumber(table.tableNumber)
                                            showTablePicker = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${table.tableNumber}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCur) Color(0xFF032125) else CafeTextPrimary
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTablePicker = false }) {
                        Text("إغلاق", color = CafeCyanPrimary)
                    }
                }
            )
        }

        // ORDER SUCCESS DIALOG
        if (orderSubmittedSuccessId != null) {
            AlertDialog(
                onDismissRequest = { orderSubmittedSuccessId = null },
                containerColor = CafeSurface,
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CafeCyanPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                },
                title = {
                    Text(
                        text = "تم إرسال الطلب بنجاح!",
                        color = CafeTextPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "طلبك لطاولة $selectedTableNumber وصل إلى لوحة الكاشير ويتم تحضيره الآن.",
                            color = CafeTextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "رقم الطلب: #${orderSubmittedSuccessId}",
                            color = CafeGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                confirmButton = {
                    CafePrimaryButton(
                        text = "الانتقال للوحة الكاشير",
                        onClick = {
                            orderSubmittedSuccessId = null
                            viewModel.navigateTo(AppScreen.CASHIER_ORDERS)
                        },
                        showArrow = true
                    )
                },
                dismissButton = {
                    TextButton(onClick = { orderSubmittedSuccessId = null }) {
                        Text("البقاء في المنيو", color = CafeTextSecondary)
                    }
                }
            )
        }
    }
}

// Category Pill matching Screenshot 2
@Composable
fun CategoryPill(
    category: MenuCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(74.dp)
            .height(84.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) CafeCyanPrimary else CafeSurface)
            .border(
                width = 1.dp,
                color = if (isSelected) CafeCyanPrimary else CafeCardBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getCategoryIcon(category.iconType),
                contentDescription = category.nameAr,
                tint = if (isSelected) Color(0xFF032125) else CafeCyanPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = category.nameAr,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF032125) else CafeTextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Food Item Card matching Screenshot 2
@Composable
fun MenuItemCard(
    item: MenuItem,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .testTag("item_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CafeSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(CafeCardBorder, CafeCardBorderGlow)),
            width = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Thumbnail on Left (Image)
            Box(
                modifier = Modifier
                    .size(95.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF061A1D)),
                contentAlignment = Alignment.Center
            ) {
                val imageRes = when (item.imageType) {
                    "drink" -> R.drawable.food_drink
                    else -> R.drawable.food_burger
                }
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = item.nameAr,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details on Right
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.nameAr,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.descriptionAr,
                        fontSize = 11.sp,
                        color = CafeTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp
                    )
                }

                // Bottom row: Price and Plus Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("%.2f $", item.price),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CafeCyanPrimary
                    )

                    // Glowing Cyan Plus Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CafeCyanPrimary)
                            .clickable(onClick = onAddToCart)
                            .testTag("add_item_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة للسلة",
                            tint = Color(0xFF032125),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

fun getCategoryIcon(type: String): ImageVector {
    return when (type.lowercase()) {
        "burger", "burgers" -> Icons.Default.Fastfood
        "fries", "appetizers" -> Icons.Default.Fastfood
        "drink", "drinks" -> Icons.Default.LocalDrink
        "meals", "meal" -> Icons.Default.Restaurant
        "sweets", "dessert", "cake" -> Icons.Default.Cake
        "pizza" -> Icons.Default.LocalPizza
        else -> Icons.Default.LocalCafe
    }
}
