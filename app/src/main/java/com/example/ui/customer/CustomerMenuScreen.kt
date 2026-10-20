package com.example.ui.customer

import com.example.util.OrderFeeCalculator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Path
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    val activeMenuTab by viewModel.customerMenuTab.collectAsState()

    // Dynamic Theme Customization
    val themeAccentHex by viewModel.themeAccentColorHex.collectAsState()
    val cardRadiusDp by viewModel.cardCornerRadiusDp.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()

    val accentColor = remember(themeAccentHex) {
        try {
            Color(android.graphics.Color.parseColor(themeAccentHex))
        } catch (_: Exception) {
            CafeCyanPrimary
        }
    }

    val context = LocalContext.current
    var showSearchBox by remember { mutableStateOf(false) }
    var showCartSheet by remember { mutableStateOf(false) }
    var showTablePicker by remember { mutableStateOf(false) }
    var orderSubmittedSuccessId by remember { mutableStateOf<String?>(null) }
    var submittedCustomerName by remember { mutableStateOf("") }
    var customerNameInput by remember { mutableStateOf("") }
    var showNameError by remember { mutableStateOf(false) }
    var itemForDetails by remember { mutableStateOf<MenuItem?>(null) }
    var customerNotes by remember { mutableStateOf("") }

    // Active category
    val activeCatId = selectedCategory ?: categories.firstOrNull()?.id ?: "cat_burgers"
    val activeCat = categories.find { it.id == activeCatId } ?: categories.firstOrNull()

    // Filter items for regular menu
    val filteredItems = allItems.filter { item ->
        val matchesCategory = (selectedCategory == null || item.categoryId == activeCatId)
        val matchesSearch = searchQuery.isBlank() ||
                item.nameAr.contains(searchQuery, ignoreCase = true) ||
                item.nameEn.contains(searchQuery, ignoreCase = true) ||
                item.descriptionAr.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    // Filter items for offers page (isOffer == true)
    val offerItems = allItems.filter { it.isOffer }

    val cartCount = cart.sumOf { it.quantity }
    val cartTotal = cart.sumOf { it.menuItem.price * it.quantity }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CafeDarkBg, CafeDarkBgGradientEnd, Color(0xFF031417))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // UNIFIED CYBER PANEL - EXACTLY MATCHING "بعد" IN SCREENSHOT
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF021013)),
                border = BorderStroke(
                    width = 1.6.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF00E5FF),
                            Color(0xFF007E84),
                            Color(0xFF00E5FF)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(9.dp)
                ) {
                    // TOP HEADER ROW: Right (Menu/Cashier Switcher), Left (VIP Table Indicator)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. In RTL, First child appears on the RIGHT: Fast Switcher between Menu & Cashier
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CafeSurfaceVariant)
                                .border(1.dp, CafeCyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Active Menu Tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CafeCyanPrimary)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = Color(0xFF032125),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "المنيو",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF032125)
                                    )
                                }
                            }

                            // Inactive Cashier Tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.navigateTo(AppScreen.CASHIER_ORDERS) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("menu_to_cashier_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Payment,
                                        contentDescription = null,
                                        tint = CafeCyanPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "الكاشير",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // 2. In RTL, Second child appears on the LEFT: Table Indicator with Golden Crown and VIP badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF0F0B02))
                                .border(1.6.dp, CafeGold, RoundedCornerShape(18.dp))
                                .clickable { showTablePicker = true }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = CafeGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "VIP",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CafeGold
                                    )
                                }

                                Text(
                                    text = "طاولة $selectedTableNumber",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // 3 HERO INTERACTIVE NAVIGATION CARDS (قائمة الطلب، العروض والجديد، ألعاب وترفيه)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(182.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // In RTL, FIRST CHILD appears on the RIGHT: 2 Stacked Cards (العروض والجديد + ألعاب وترفيه)
                        Column(
                            modifier = Modifier
                                .weight(1.15f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // A. TOP STACKED CARD: "العروض والجديد" with flame icon & "New" badge
                            val isOffersSelected = activeMenuTab == 1
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clickable { viewModel.setCustomerMenuTab(1) }
                                    .testTag("hero_tab_offers"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF120324)),
                                border = BorderStroke(
                                    width = if (isOffersSelected) 2.dp else 1.3.dp,
                                    brush = if (isOffersSelected)
                                        Brush.horizontalGradient(listOf(Color(0xFFD500F9), Color(0xFF7C4DFF)))
                                    else
                                        Brush.horizontalGradient(listOf(Color(0xFF7C4DFF).copy(alpha = 0.6f), Color(0xFF311B92)))
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    if (isOffersSelected) Color(0xFF240645) else Color(0xFF17042E),
                                                    Color(0xFF0C0117)
                                                )
                                            )
                                        ),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Right side in RTL: Flame Icon + Text
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(34.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(verticalArrangement = Arrangement.Center) {
                                            Text(
                                                text = "العروض",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "والجديد",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    // Left side in RTL: "New" vertical badge
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(26.dp)
                                            .background(Color(0xFF7C4DFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "New",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.rotate(-90f)
                                        )
                                    }
                                }
                            }

                            // B. BOTTOM STACKED CARD: "ألعاب وترفيه" with controller icon
                            val isGamesSelected = activeMenuTab == 2
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clickable { viewModel.setCustomerMenuTab(2) }
                                    .testTag("hero_tab_games"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF021330)),
                                border = BorderStroke(
                                    width = if (isGamesSelected) 2.dp else 1.3.dp,
                                    brush = if (isGamesSelected)
                                        Brush.horizontalGradient(listOf(Color(0xFF2979FF), Color(0xFF00E5FF)))
                                    else
                                        Brush.horizontalGradient(listOf(Color(0xFF1E88E5).copy(alpha = 0.6f), Color(0xFF0D47A1)))
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    if (isGamesSelected) Color(0xFF06255B) else Color(0xFF031638),
                                                    Color(0xFF020B1C)
                                                )
                                            )
                                        )
                                        .padding(horizontal = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(34.dp)
                                    )

                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = "ألعاب",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "وترفيه",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // In RTL, SECOND CHILD appears on the LEFT: TALL GOLDEN CARD "قائمة الطلب"
                        val isMenuSelected = activeMenuTab == 0
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { viewModel.setCustomerMenuTab(0) }
                                .testTag("hero_tab_menu_list"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0701)),
                            border = BorderStroke(
                                width = if (isMenuSelected) 2.dp else 1.3.dp,
                                brush = if (isMenuSelected)
                                    Brush.verticalGradient(listOf(Color(0xFFFFD54F), CafeGold, Color(0xFF8D6E12)))
                                else
                                    Brush.verticalGradient(listOf(CafeGold.copy(alpha = 0.6f), Color(0xFF3E2D07)))
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                if (isMenuSelected) Color(0xFF1E1502) else Color(0xFF120C02),
                                                Color(0xFF070500)
                                            )
                                        )
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Bottom subtle glowing golden curved wave matching "بعد" in screenshot
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val wavePath = Path().apply {
                                        moveTo(0f, size.height * 0.88f)
                                        quadraticBezierTo(
                                            size.width * 0.55f, size.height * 0.96f,
                                            size.width, size.height * 0.76f
                                        )
                                        lineTo(size.width, size.height)
                                        lineTo(0f, size.height)
                                        close()
                                    }
                                    drawPath(
                                        path = wavePath,
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                CafeGold.copy(alpha = 0.35f),
                                                Color(0xFFFFD54F).copy(alpha = 0.08f)
                                            )
                                        )
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Circular Emblem with fork & knife
                                    Box(
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF110B01))
                                            .border(2.dp, CafeGold, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Restaurant,
                                            contentDescription = null,
                                            tint = CafeGold,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "قائمة الطلب",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isMenuSelected) Color.White else Color(0xFFE0E0E0),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // RENDER ACTIVE PAGE CONTENT
            when (activeMenuTab) {
                0 -> {
                    // PAGE 1: Standard Ordering Page
                    StandardOrderingPage(
                        categories = categories,
                        activeCat = activeCat,
                        activeCatId = activeCatId,
                        filteredItems = filteredItems,
                        cart = cart,
                        cartCount = cartCount,
                        accentColor = accentColor,
                        cardRadiusDp = cardRadiusDp,
                        fontScale = fontScale,
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onAddToCart = { viewModel.addToCart(it) },
                        onRemoveFromCart = { viewModel.removeFromCart(it) },
                        onShowDetails = { itemForDetails = it }
                    )
                }
                1 -> {
                    // PAGE 2: العروض والتخفيضات والمواد الجديدة
                    OffersAndNewItemsPage(
                        offerItems = offerItems,
                        cart = cart,
                        cartCount = cartCount,
                        accentColor = accentColor,
                        cardRadiusDp = cardRadiusDp,
                        fontScale = fontScale,
                        onAddToCart = { viewModel.addToCart(it) },
                        onRemoveFromCart = { viewModel.removeFromCart(it) },
                        onGoToMenu = { viewModel.setCustomerMenuTab(0) },
                        onShowDetails = { itemForDetails = it }
                    )
                }
                2 -> {
                    // PAGE 3: الألعاب والترفيه
                    GamesAndEntertainmentPage(
                        tableNumber = selectedTableNumber,
                        onOrderMore = { viewModel.setCustomerMenuTab(0) }
                    )
                }
            }
        }

        // FLOATING BOTTOM CART BAR - Appears as soon as user starts selecting items
        AnimatedVisibility(
            visible = cartCount > 0,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(accentColor, Color(0xFF00ADB5), Color(0xFF007E84))
                        )
                    )
                    .clickable { showCartSheet = true }
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .testTag("bottom_cart_bar")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF032125)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$cartCount",
                                color = accentColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "سلة طلب طاولة $selectedTableNumber",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF032125)
                            )
                            Text(
                                text = OrderFeeCalculator.formatPrice(cartTotal, cart.firstOrNull()?.menuItem?.currency ?: "د.ع"),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF032125)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "إتمام وتأكيد الطلب",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF032125)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
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
                            text = "سلة طلب طاولة $selectedTableNumber",
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
                            .heightIn(max = 240.dp),
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
                                        text = OrderFeeCalculator.formatPrice(cartItem.menuItem.price * cartItem.quantity, cartItem.menuItem.currency),
                                        fontSize = 13.sp,
                                        color = accentColor
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
                                            tint = accentColor,
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
                                            .background(accentColor)
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

                    // Customer Name Input Field (MANDATORY USER REQUEST: اجباري ان يكتب الزبون اسمة في الطلب)
                    OutlinedTextField(
                        value = customerNameInput,
                        onValueChange = {
                            customerNameInput = it
                            if (showNameError && it.trim().isNotBlank()) {
                                showNameError = false
                            }
                        },
                        label = {
                            Text(
                                text = "اسم الزبون (إجباري لإتمام الطلب) *",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showNameError) Color(0xFFFF5252) else accentColor
                            )
                        },
                        placeholder = { Text("يرجى كتابة اسمك هنا لإرسال طلبك إلى الكاشير", fontSize = 11.sp, color = CafeTextSecondary) },
                        isError = showNameError,
                        supportingText = {
                            if (showNameError) {
                                Text(
                                    text = "⚠️ كتابة اسم الزبون إجبارية لإتمام وإرسال الطلب!",
                                    color = Color(0xFFFF5252),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (showNameError) Color(0xFFFF5252) else accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CafeInputBg,
                            unfocusedContainerColor = CafeInputBg,
                            focusedBorderColor = if (showNameError) Color(0xFFFF5252) else accentColor,
                            unfocusedBorderColor = if (showNameError) Color(0xFFFF5252) else CafeInputBorder,
                            focusedTextColor = CafeTextPrimary,
                            unfocusedTextColor = CafeTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Special Notes
                    OutlinedTextField(
                        value = customerNotes,
                        onValueChange = { customerNotes = it },
                        placeholder = { Text("ملاحظات خاصة (مثال: بدون سكر، زيادة صوص...)", fontSize = 11.sp, color = CafeTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CafeInputBg,
                            unfocusedContainerColor = CafeInputBg,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = CafeInputBorder,
                            focusedTextColor = CafeTextPrimary,
                            unfocusedTextColor = CafeTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val cartCurrency = cart.firstOrNull()?.menuItem?.currency ?: "د.ع"
                    val cartFee = OrderFeeCalculator.calculateServiceFee(cartTotal, cartCurrency)
                    val cartFinalTotal = cartTotal + cartFee

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CafeInputBg)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مجموع الطلب:", fontSize = 12.sp, color = CafeTextSecondary)
                            Text(OrderFeeCalculator.formatPrice(cartTotal, cartCurrency), fontSize = 13.sp, color = CafeTextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نسبة الخدمة 1% (أقرب 250 د.ع):", fontSize = 11.sp, color = CafeGold)
                            Text(OrderFeeCalculator.formatPrice(cartFee, cartCurrency), fontSize = 12.sp, color = CafeGold, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = CafeCardBorder, modifier = Modifier.padding(vertical = 2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الإجمالي الكلي المطلوب:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                            Text(OrderFeeCalculator.formatPrice(cartFinalTotal, cartCurrency), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    CafePrimaryButton(
                        text = "إرسال الطلب إلى الكاشير",
                        onClick = {
                            val trimmedName = customerNameInput.trim()
                            if (trimmedName.isBlank()) {
                                showNameError = true
                                Toast.makeText(context, "يرجى كتابة اسمك أولاً لإتمام الطلب (إجباري)", Toast.LENGTH_LONG).show()
                                return@CafePrimaryButton
                            }
                            viewModel.submitOrder(
                                customerName = trimmedName,
                                customerNotes = customerNotes
                            ) { orderId ->
                                orderSubmittedSuccessId = orderId
                                submittedCustomerName = trimmedName
                                customerNotes = ""
                                customerNameInput = ""
                                showNameError = false
                                showCartSheet = false
                            }
                        },
                        testTag = "submit_order_to_cashier_btn"
                    )
                }
            }
        }

        // TABLE PICKER DIALOG
        if (showTablePicker) {
            AlertDialog(
                onDismissRequest = { showTablePicker = false },
                containerColor = CafeSurface,
                shape = RoundedCornerShape(20.dp),
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
                                        .background(if (isCur) accentColor else CafeSurfaceVariant)
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
                        Text("إغلاق", color = accentColor)
                    }
                }
            )
        }

        // ORDER SUCCESS DIALOG
        if (orderSubmittedSuccessId != null) {
            AlertDialog(
                onDismissRequest = { orderSubmittedSuccessId = null },
                containerColor = CafeSurface,
                shape = RoundedCornerShape(20.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accentColor,
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
                            text = "أهلاً بك يا $submittedCustomerName! طلبك لطاولة $selectedTableNumber وصل إلى لوحة الكاشير ويتم تحضيره الآن.",
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

        // ITEM DETAILS & GALLERY PHOTOS DIALOG (خيار مزيد من التفاصيل وصور)
        if (itemForDetails != null) {
            ItemDetailsAndGalleryDialog(
                item = itemForDetails!!,
                onDismiss = { itemForDetails = null },
                inCartCount = cart.find { it.menuItem.id == itemForDetails!!.id }?.quantity ?: 0,
                onAddToCart = { viewModel.addToCart(itemForDetails!!) },
                onRemoveFromCart = { viewModel.removeFromCart(itemForDetails!!) },
                accentColor = accentColor,
                fontScale = fontScale
            )
        }
    }
}

/**
 * Tab item component for the 3-page navigation
 */
@Composable
fun MenuPageTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    badge: String?,
    modifier: Modifier = Modifier,
    activeColor: Color = CafeCyanPrimary,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(if (isSelected) activeColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF032125) else CafeTextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF032125) else CafeTextPrimary,
                maxLines = 1
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF032125) else activeColor)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) activeColor else Color(0xFF032125)
                    )
                }
            }
        }
    }
}

/**
 * PAGE 0: Standard Ordering Page
 */
@Composable
fun StandardOrderingPage(
    categories: List<MenuCategory>,
    activeCat: MenuCategory?,
    activeCatId: String,
    filteredItems: List<MenuItem>,
    cart: List<CartItem>,
    cartCount: Int,
    accentColor: Color,
    cardRadiusDp: Int,
    fontScale: Float,
    onSelectCategory: (String) -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    onRemoveFromCart: (MenuItem) -> Unit,
    onShowDetails: (MenuItem) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // HORIZONTAL CATEGORIES CAROUSEL
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { category ->
                val isSelected = category.id == activeCatId
                CategoryPill(
                    category = category,
                    isSelected = isSelected,
                    accentColor = accentColor,
                    onClick = { onSelectCategory(category.id) }
                )
            }
        }

        // SECTION HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredItems.size} صنف",
                fontSize = (13 * fontScale).sp,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = activeCat?.nameAr ?: "الأصناف",
                    fontSize = (17 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = getCategoryIcon(activeCat?.iconType ?: "burger"),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredItems) { item ->
                val quantityInCart = cart.find { it.menuItem.id == item.id }?.quantity ?: 0
                MenuItemCard(
                    item = item,
                    quantityInCart = quantityInCart,
                    accentColor = accentColor,
                    cardRadiusDp = cardRadiusDp,
                    fontScale = fontScale,
                    onAddToCart = { onAddToCart(item) },
                    onRemoveFromCart = { onRemoveFromCart(item) },
                    onShowDetails = { onShowDetails(item) }
                )
            }
        }
    }
}

/**
 * PAGE 1: Offers, Discounts & New Items Page (صفحة العروض والتخفيضات والمواد الجديدة)
 */
@Composable
fun OffersAndNewItemsPage(
    offerItems: List<MenuItem>,
    cart: List<CartItem>,
    cartCount: Int,
    accentColor: Color,
    cardRadiusDp: Int,
    fontScale: Float,
    onAddToCart: (MenuItem) -> Unit,
    onRemoveFromCart: (MenuItem) -> Unit,
    onGoToMenu: () -> Unit,
    onShowDetails: (MenuItem) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = if (cartCount > 0) 100.dp else 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Offer Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cardRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1705)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(CafeGold, Color(0xFFFFB300))),
                    width = 1.2.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = CafeGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "أقوى العروض والخصومات!",
                                fontSize = (16 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = CafeGold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "اكتشف أحدث الإضافات والخصومات الحصرية المتاحة لك الآن",
                            fontSize = (11 * fontScale).sp,
                            color = CafeTextSecondary,
                            lineHeight = 15.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(CafeGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔥", fontSize = 22.sp)
                    }
                }
            }
        }

        // OFFERS ITEMS LIST
        if (offerItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    shape = RoundedCornerShape(cardRadiusDp.dp),
                    colors = CardDefaults.cardColors(containerColor = CafeSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = CafeTextSecondary, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لا توجد عروض مضافة حالياً",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )
                        Text(
                            text = "يمكن لصاحب الكافتيريا تفعيل أي صنف كعرض من خلال ترس الإعدادات أمام الصنف",
                            fontSize = 12.sp,
                            color = CafeTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onGoToMenu,
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color(0xFF032125)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("تصفح قائمة الطعام الكاملة", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(offerItems) { item ->
                val quantityInCart = cart.find { it.menuItem.id == item.id }?.quantity ?: 0
                OfferItemCard(
                    item = item,
                    quantityInCart = quantityInCart,
                    accentColor = accentColor,
                    cardRadiusDp = cardRadiusDp,
                    fontScale = fontScale,
                    onAddToCart = { onAddToCart(item) },
                    onRemoveFromCart = { onRemoveFromCart(item) },
                    onShowDetails = { onShowDetails(item) }
                )
            }
        }
    }
}

/**
 * Offer Item Card with clear proportions and Stepper
 */
@Composable
fun OfferItemCard(
    item: MenuItem,
    quantityInCart: Int,
    accentColor: Color,
    cardRadiusDp: Int,
    fontScale: Float,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
    onShowDetails: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("offer_card_${item.id}"),
        shape = RoundedCornerShape(cardRadiusDp.dp),
        colors = CardDefaults.cardColors(containerColor = CafeSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(CafeGold.copy(alpha = 0.6f), CafeCardBorder)),
            width = 1.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Optional Top Banner if bgImageUri is provided
            if (!item.bgImageUri.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    AsyncImage(
                        model = item.bgImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, CafeSurface)
                                )
                            )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Top Tag & Discount Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CafeGold)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🔥 ${item.offerTag}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF032125)
                        )
                    }

                    if (item.offerDiscountPercent > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFD32F2F))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "خصم ${item.offerDiscountPercent}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Food Thumbnail - Beautiful 90dp container
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF061A1D))
                            .border(1.dp, CafeCardBorder, RoundedCornerShape(16.dp))
                            .clickable { onShowDetails() },
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

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.nameAr,
                            fontSize = (15 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.descriptionAr,
                            fontSize = (11 * fontScale).sp,
                            color = CafeTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // خيار مزيد من التفاصيل وصور
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onShowDetails() }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = CafeGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (item.galleryImages.isNotEmpty()) "مزيد من التفاصيل وصور (${item.galleryImages.size + if (item.imageUri != null) 1 else 0}) 📸" else "مزيد من التفاصيل وصور 📸",
                                fontSize = (11 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = CafeGold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Price Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val originalPrice = if (item.offerDiscountPercent > 0) {
                                item.price / (1.0 - (item.offerDiscountPercent / 100.0))
                            } else null

                            Text(
                                text = OrderFeeCalculator.formatPrice(item.price, item.currency),
                                fontSize = (16 * fontScale).sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CafeGold
                            )

                            if (originalPrice != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = OrderFeeCalculator.formatPrice(originalPrice, item.currency),
                                    fontSize = 12.sp,
                                    color = CafeTextSecondary,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                    }

                    // STEPPER (OR ORDER BUTTON IF 0)
                    if (quantityInCart == 0) {
                        Button(
                            onClick = onAddToCart,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CafeGold,
                                contentColor = Color(0xFF032125)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طلب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Interactive Stepper
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CafeSurfaceVariant)
                                .border(1.2.dp, CafeGold, RoundedCornerShape(12.dp))
                                .padding(horizontal = 2.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = onRemoveFromCart,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(CafeInputBg)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "تقليل",
                                    tint = CafeGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "$quantityInCart",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CafeTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = onAddToCart,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(CafeGold)
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
        }
    }
}

/**
 * PAGE 2: Games & Entertainment Page
 */
@Composable
fun GamesAndEntertainmentPage(
    tableNumber: Int,
    onOrderMore: () -> Unit
) {
    var luckySpinResult by remember { mutableStateOf<String?>(null) }

    // Tic Tac Toe State
    var board by remember { mutableStateOf(List(9) { "" }) }
    var currentPlayer by remember { mutableStateOf("X") }
    var winner by remember { mutableStateOf<String?>(null) }

    // Cafe Trivia State
    val triviaList = remember {
        listOf(
            "هل تعلم؟ القهوة هي ثاني أكثر السلع تداولاً في العالم بعد النفط الخام!" to "معلومة قهوة ☕",
            "هل تعلم؟ أول مقهى موثق في التاريخ تأسس في إسطنبول عام 1555م." to "تاريخ الكافيهات 🏛️",
            "سر الرغوة الذهبية (الكريما) في الإسبريسو يأتي من ثاني أكسيد الكربون الناتج عن تحميص البن!" to "سر النكهة ✨",
            "شرب كوب ماء قبل القهوة ينظف براعم التذوق لديك لتستمتع بكل إيحاءات البن الفاخر!" to "نصيحة باريستا 💧",
            "طاولة $tableNumber هي أكثر الطاولات حظاً وإشراقاً في كافيه اليوم!" to "طاولة اليوم 🌟"
        )
    }
    var triviaIndex by remember { mutableIntStateOf(0) }

    fun checkWinner(b: List<String>): String? {
        val winningLines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (line in winningLines) {
            val (x, y, z) = line
            if (b[x].isNotEmpty() && b[x] == b[y] && b[y] == b[z]) {
                return b[x]
            }
        }
        if (b.all { it.isNotEmpty() }) return "تعادل"
        return null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140F2D)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF7C4DFF), CafeCyanPrimary)),
                    width = 1.2.dp
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
                            .background(Color(0xFF7C4DFF).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = Color(0xFFB388FF),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "ركن الألعاب والترفيه 🎲",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE0D8FF)
                        )
                        Text(
                            text = "استمتع بالألعاب والتسلية على طاولة $tableNumber أثناء تحضير طلبك!",
                            fontSize = 12.sp,
                            color = CafeTextSecondary
                        )
                    }
                }
            }
        }

        // GAME 1: عجلـة الحظ والمفاجآت
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CafeSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(CafeCardBorder, CafeCardBorderGlow)),
                    width = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Casino, contentDescription = null, tint = CafeGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "عجلة الحظ الترفيهية للكافيه 🎡",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CafeInputBg)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = luckySpinResult ?: "اضغط زر 'تدوير العجلة' لاكتشاف مفاجأتك اليومية!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (luckySpinResult != null) CafeGold else CafeTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val options = listOf(
                                "🎉 ابتسم! طلبك يُحضّر الآن بكل عناية وحب في المطبخ!",
                                "☕ قهوة ثانية مع ابتسامة عريضة اليوم!",
                                "✨ طاولة $tableNumber هي أكثر الطاولات تميزاً وأناقة اليوم!",
                                "🍪 قطعة كوكيز هدية مع وجبتك القادمة!",
                                "🌟 يومك مشرق ومليء بالإنجازات الجميلة!",
                                "❤️ شكراً لاختيارك كافتيريتنا، وجودك يسعدنا دائماً!"
                            )
                            luckySpinResult = options.random()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF032125)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تدوير العجلة واكتشاف الحظ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // GAME 2: لعبة إكس أو (XO) لشخصين على الطاولة
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CafeSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(CafeCardBorder, CafeCardBorderGlow)),
                    width = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "لعبة إكس أو (XO) للتسلية ❌⭕",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )

                        IconButton(
                            onClick = {
                                board = List(9) { "" }
                                currentPlayer = "X"
                                winner = null
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "إعادة اللعبة", tint = CafeCyanPrimary)
                        }
                    }

                    Text(
                        text = when {
                            winner == "تعادل" -> "انتهت اللعبة بالتعادل! 🤝"
                            winner != null -> "الفائز هو اللاعب: $winner 🏆 مبروك!"
                            else -> "دور اللاعب: $currentPlayer"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (winner != null) CafeGold else CafeCyanPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3x3 Grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0..2) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (col in 0..2) {
                                    val index = row * 3 + col
                                    val cell = board[index]
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CafeInputBg)
                                            .border(1.dp, CafeInputBorder, RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (cell.isEmpty() && winner == null) {
                                                    val newBoard = board.toMutableList()
                                                    newBoard[index] = currentPlayer
                                                    board = newBoard
                                                    val w = checkWinner(newBoard)
                                                    if (w != null) {
                                                        winner = w
                                                    } else {
                                                        currentPlayer = if (currentPlayer == "X") "O" else "X"
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cell,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (cell == "X") CafeCyanPrimary else CafeGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // GAME 3: هل تعلم وكويز القهوة
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CafeSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(CafeCardBorder, CafeCardBorderGlow)),
                    width = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val curTrivia = triviaList[triviaIndex % triviaList.size]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = CafeCyanPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = curTrivia.second,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeCyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = curTrivia.first,
                        fontSize = 13.sp,
                        color = CafeTextPrimary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { triviaIndex++ }
                        ) {
                            Text("معلومة أخرى 💡", color = CafeCyanPrimary, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onOrderMore,
                            colors = ButtonDefaults.buttonColors(containerColor = CafeSurfaceVariant, contentColor = CafeCyanPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("الرجوع لقائمة الطلب", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Category Pill supporting custom uploaded image or icon
 */
@Composable
fun CategoryPill(
    category: MenuCategory,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(88.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) accentColor else CafeSurface)
            .border(
                width = 1.dp,
                color = if (isSelected) accentColor else CafeCardBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!category.customImageUri.isNullOrEmpty()) {
                AsyncImage(
                    model = category.customImageUri,
                    contentDescription = category.nameAr,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, if (isSelected) Color(0xFF032125) else accentColor, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = getCategoryIcon(category.iconType),
                    contentDescription = category.nameAr,
                    tint = if (isSelected) Color(0xFF032125) else accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
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

/**
 * Food Item Card supporting custom image, background image, availability, and STEPPER (+ / -)
 */
@Composable
fun MenuItemCard(
    item: MenuItem,
    quantityInCart: Int,
    accentColor: Color,
    cardRadiusDp: Int,
    fontScale: Float,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
    onShowDetails: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("item_card_${item.id}"),
        shape = RoundedCornerShape(cardRadiusDp.dp),
        colors = CardDefaults.cardColors(containerColor = CafeSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(CafeCardBorder, CafeCardBorderGlow)),
            width = 1.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Background Image if provided - Rendered as a sleek food banner header
            if (!item.bgImageUri.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    AsyncImage(
                        model = item.bgImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, CafeSurface)
                                )
                            )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Food Thumbnail - Beautiful square aspect ratio
                Box(
                    modifier = Modifier
                        .size(94.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF061A1D))
                        .border(1.dp, CafeCardBorder, RoundedCornerShape(16.dp))
                        .clickable { onShowDetails() },
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

                    // Not available overlay badge
                    if (!item.isAvailable) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xCC000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "غير متوفر",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Details & Controls
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.nameAr,
                                fontSize = (15 * fontScale).sp,
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
                                        .background(CafeGold.copy(alpha = 0.25f))
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
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.descriptionAr,
                            fontSize = (11 * fontScale).sp,
                            color = CafeTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // خيار مزيد من التفاصيل وصور
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onShowDetails() }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (item.galleryImages.isNotEmpty()) "مزيد من التفاصيل وصور (${item.galleryImages.size + if (item.imageUri != null) 1 else 0}) 📸" else "مزيد من التفاصيل وصور 📸",
                                fontSize = (11 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bottom row: Price and Stepper (+ / -)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = OrderFeeCalculator.formatPrice(item.price, item.currency),
                            fontSize = (15 * fontScale).sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )

                        if (item.isAvailable) {
                            if (quantityInCart == 0) {
                                // Default Order Button
                                Button(
                                    onClick = onAddToCart,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        contentColor = Color(0xFF032125)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("add_item_${item.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("طلب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Interactive Stepper directly on Card
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CafeSurfaceVariant)
                                        .border(1.2.dp, accentColor, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 3.dp, vertical = 2.dp)
                                ) {
                                    // Minus button (-)
                                    IconButton(
                                        onClick = onRemoveFromCart,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(CafeInputBg)
                                            .testTag("decrease_item_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "تقليل الكمية",
                                            tint = accentColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    Text(
                                        text = "$quantityInCart",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CafeTextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    // Plus button (+)
                                    IconButton(
                                        onClick = onAddToCart,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(accentColor)
                                            .testTag("increase_item_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "زيادة الكمية",
                                            tint = Color(0xFF032125),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "غير متوفر للطلب",
                                fontSize = 11.sp,
                                color = Color(0xFFFF6B6B),
                                fontWeight = FontWeight.Bold
                            )
                        }
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

/**
 * خيار "مزيد من التفاصيل وصور":
 * يعرض ألبوم صور متعددة للصنف مع شرح تفصيلي ومكونات وسعر والتحكم بالكمية
 */
@Composable
fun ItemDetailsAndGalleryDialog(
    item: MenuItem,
    onDismiss: () -> Unit,
    inCartCount: Int,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
    accentColor: Color,
    fontScale: Float = 1.0f
) {
    val allPhotos = remember(item) {
        buildList {
            item.imageUri?.takeIf { it.isNotBlank() }?.let { add(it) }
            addAll(item.galleryImages.filter { it.isNotBlank() })
            item.bgImageUri?.takeIf { it.isNotBlank() }?.let { add(it) }
        }.distinct()
    }

    var activePhotoIndex by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CafeSurface,
        shape = RoundedCornerShape(24.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top header: Close button + Offer/Availability badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CafeSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = CafeTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (item.isOffer) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CafeGold.copy(alpha = 0.25f))
                                    .border(1.dp, CafeGold, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "🔥 ${item.offerTag}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CafeGold
                                )
                            }
                        }

                        if (!item.isAvailable) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33D32F2F))
                                    .border(1.dp, Color(0xFFD32F2F), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "غير متوفر حالياً",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6B6B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Photos Section
                if (allPhotos.isNotEmpty()) {
                    val safeIdx = activePhotoIndex.coerceIn(0, allPhotos.size - 1)
                    val currentUri = allPhotos[safeIdx]

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF041215))
                            .border(1.2.dp, CafeCardBorderGlow, RoundedCornerShape(18.dp))
                    ) {
                        AsyncImage(
                            model = currentUri,
                            contentDescription = item.nameAr,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Photo Counter Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xCC000000))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "صورة ${safeIdx + 1} من ${allPhotos.size}",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Thumbnails list if multiple photos
                    if (allPhotos.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(allPhotos) { idx, uri ->
                                val isSelected = idx == safeIdx
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 0.8.dp,
                                            color = if (isSelected) accentColor else CafeCardBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { activePhotoIndex = idx }
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Themed Fallback Illustration
                    val imgRes = when (item.imageType) {
                        "drink" -> R.drawable.food_drink
                        else -> R.drawable.food_burger
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF041215))
                            .border(1.dp, CafeCardBorder, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = imgRes),
                            contentDescription = item.nameAr,
                            modifier = Modifier.size(90.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item Name (Arabic & English)
                Text(
                    text = item.nameAr,
                    fontSize = (18 * fontScale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CafeTextPrimary,
                    textAlign = TextAlign.Center
                )

                if (item.nameEn.isNotBlank()) {
                    Text(
                        text = item.nameEn,
                        fontSize = (12 * fontScale).sp,
                        color = CafeTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Price Row
                Text(
                    text = OrderFeeCalculator.formatPrice(item.price, item.currency),
                    fontSize = (19 * fontScale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Explanation & Description Details Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CafeInputBg)
                        .border(1.dp, CafeCardBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = CafeGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "شرح وتفاصيل الطلب:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeGold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.descriptionAr.ifBlank { "طلب شهي ومميز يتم تحضيره طازجاً من أجود المكونات حسب اختيارك." },
                        fontSize = (13 * fontScale).sp,
                        color = CafeTextPrimary,
                        lineHeight = 20.sp
                    )

                    if (item.descriptionEn.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.descriptionEn,
                            fontSize = (11 * fontScale).sp,
                            color = CafeTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stepper Controls
                if (item.isAvailable) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CafeSurfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الكمية في السلة:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary,
                            modifier = Modifier.padding(start = 6.dp)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onRemoveFromCart,
                                enabled = inCartCount > 0,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (inCartCount > 0) CafeInputBg else Color(0x33FFFFFF))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "تقليل",
                                    tint = if (inCartCount > 0) accentColor else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "$inCartCount",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CafeTextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            IconButton(
                                onClick = onAddToCart,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
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
                } else {
                    Text(
                        text = "هذا الصنف غير متوفر حالياً للطلب",
                        fontSize = 12.sp,
                        color = Color(0xFFFF6B6B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color(0xFF032125)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إغلاق التفاصيل", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    )
}
