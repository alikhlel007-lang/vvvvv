package com.example.ui.customer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.CafeCyanPrimary
import com.example.ui.theme.CafeGold
import com.example.ui.theme.CafeInputBorder
import com.example.ui.theme.CafeTextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CustomerMenuScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val rawBaseUrl by viewModel.webMenuUrl.collectAsState()
    val registeredTables by viewModel.tables.collectAsState()

    val cafeId = currentUser?.id ?: "itNW7gdqekQp538fOVzIbjG4BAp1"
    var selectedTableNumber by remember { mutableIntStateOf(1) }
    var showCustomTableDialog by remember { mutableStateOf(false) }
    var customTableInput by remember { mutableStateOf("") }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoadingPage by remember { mutableStateOf(true) }
    var pageTitle by remember { mutableStateOf("المنيو الذكي") }
    var isFullscreenMode by remember { mutableStateOf(false) }

    // Helper to calculate target URL for the selected table
    fun getTargetUrl(tableNum: Int): String {
        val clean = rawBaseUrl.trim()
        val baseWithoutQuery = if (clean.contains("?")) clean.substringBefore("?") else clean
        val normalized = if (baseWithoutQuery.endsWith("/")) baseWithoutQuery else "$baseWithoutQuery/"
        val tableObj = registeredTables.find { it.tableNumber == tableNum }
        val sec = tableObj?.securityToken ?: ""
        return "${normalized}?cafeteriaId=${cafeId}&c=${cafeId}&table=${tableNum}&t=${tableNum}&sec=${sec}"
    }

    // Handle back button: go back in WebView history if possible, else return to Cashier Orders
    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            viewModel.navigateTo(AppScreen.CASHIER_ORDERS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0908))
    ) {
        // --- 1. Top Control Bar for Cashier ---
        if (!isFullscreenMode) {
            Surface(
                color = Color(0xFF14100E),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Main Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back to Cashier button labeled "الكاشير"
                        Surface(
                            onClick = { viewModel.navigateTo(AppScreen.CASHIER_ORDERS) },
                            shape = RoundedCornerShape(12.dp),
                            color = CafeCyanPrimary,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = Color(0xFF032125),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "الكاشير",
                                    color = Color(0xFF032125),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Right actions: Fullscreen toggle & Refresh
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isFullscreenMode = true }) {
                                Icon(
                                    Icons.Default.Fullscreen,
                                    contentDescription = "إخفاء الشريط / ملء الشاشة",
                                    tint = CafeCyanPrimary
                                )
                            }
                            IconButton(onClick = {
                                isLoadingPage = true
                                webViewInstance?.reload()
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "تحديث المنيو", tint = CafeGold)
                            }
                        }
                    }

                // Table Selector Dropdown (قائمة منسدلة لاختيار الطاولة)
                var tableDropdownExpanded by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0C0A))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TableRestaurant,
                            contentDescription = null,
                            tint = CafeGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اختر الطاولة:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextSecondary
                        )
                    }

                    Box {
                        Surface(
                            onClick = { tableDropdownExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1B1512),
                            border = BorderStroke(1.dp, CafeCyanPrimary.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "طاولة $selectedTableNumber",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CafeCyanPrimary
                                )
                                Icon(
                                    imageVector = if (tableDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = CafeCyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = tableDropdownExpanded,
                            onDismissRequest = { tableDropdownExpanded = false },
                            modifier = Modifier
                                .background(Color(0xFF181310))
                                .border(1.dp, CafeInputBorder, RoundedCornerShape(8.dp))
                        ) {
                            val tableNumbers = if (registeredTables.isNotEmpty()) {
                                registeredTables.map { it.tableNumber }
                            } else {
                                (1..10).toList()
                            }
                            tableNumbers.forEach { num ->
                                val isSelected = selectedTableNumber == num
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "طاولة $num",
                                            color = if (isSelected) CafeCyanPrimary else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.TableRestaurant,
                                            contentDescription = null,
                                            tint = if (isSelected) CafeCyanPrimary else CafeTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    onClick = {
                                        tableDropdownExpanded = false
                                        if (selectedTableNumber != num) {
                                            selectedTableNumber = num
                                            isLoadingPage = true
                                            webViewInstance?.loadUrl(getTargetUrl(num))
                                        }
                                    }
                                )
                            }

                            // خيار إدخال طاولة أخرى مخصصة
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "+ طاولة أخرى...",
                                        color = CafeGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = CafeGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    tableDropdownExpanded = false
                                    customTableInput = selectedTableNumber.toString()
                                    showCustomTableDialog = true
                                }
                            )
                        }
                    }
                }

                // Loading progress indicator
                if (isLoadingPage) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = CafeCyanPrimary,
                        trackColor = Color(0xFF1F1916)
                    )
                }
            }
        }
    }

        // --- 2. In-App WebView displaying the real Web Menu ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(Color(0xFF0A0908))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            builtInZoomControls = false
                            displayZoomControls = false
                            cacheMode = WebSettings.LOAD_DEFAULT
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            userAgentString = userAgentString + " QCafeAndroidApp"
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoadingPage = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoadingPage = false
                                view?.title?.let { if (it.isNotBlank()) pageTitle = it }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val u = request?.url?.toString() ?: return false
                                // Stay inside WebView for menu pages
                                if (u.startsWith("http://") || u.startsWith("https://")) {
                                    return false
                                }
                                return try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(u))
                                    ctx.startActivity(intent)
                                    true
                                } catch (_: Exception) {
                                    false
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                title?.let { if (it.isNotBlank()) pageTitle = it }
                            }
                        }

                        webViewInstance = this
                        loadUrl(getTargetUrl(selectedTableNumber))
                    }
                },
                update = { webView ->
                    webViewInstance = webView
                }
            )

            if (isFullscreenMode) {
                // Floating Action Bar in fullscreen mode
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { viewModel.navigateTo(AppScreen.CASHIER_ORDERS) },
                        shape = RoundedCornerShape(12.dp),
                        color = CafeCyanPrimary.copy(alpha = 0.95f),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color(0xFF032125),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "الكاشير",
                                color = Color(0xFF032125),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Surface(
                        onClick = { isFullscreenMode = false },
                        shape = CircleShape,
                        color = Color(0xDD1F1916),
                        border = BorderStroke(1.dp, CafeCyanPrimary),
                        shadowElevation = 6.dp
                    ) {
                        IconButton(
                            onClick = { isFullscreenMode = false },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.FullscreenExit,
                                contentDescription = "إظهار شريط التحكم",
                                tint = CafeCyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog for entering a custom table number
    if (showCustomTableDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTableDialog = false },
            containerColor = Color(0xFF181310),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Chair, contentDescription = null, tint = CafeCyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تحديد رقم الطاولة للطلب",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "اكتب رقم الطاولة التي ترغب بفتح المنيو والطلب لها:",
                        color = CafeTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customTableInput,
                        onValueChange = { customTableInput = it },
                        label = { Text("رقم الطاولة", color = CafeTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CafeCyanPrimary,
                            unfocusedBorderColor = Color(0xFF382F2A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = customTableInput.toIntOrNull() ?: 1
                        selectedTableNumber = num
                        showCustomTableDialog = false
                        isLoadingPage = true
                        webViewInstance?.loadUrl(getTargetUrl(num))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CafeCyanPrimary)
                ) {
                    Text("عرض المنيو للطاولة", color = Color(0xFF051B20), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTableDialog = false }) {
                    Text("إلغاء", color = CafeTextSecondary)
                }
            }
        )
    }

    // Cleanup WebView when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.stopLoading()
        }
    }
}

fun getCategoryIcon(iconType: String): ImageVector {
    return when (iconType.lowercase().trim()) {
        "coffee", "hot", "قهوة", "مشروبات ساخنة" -> Icons.Default.LocalCafe
        "cold", "drink", "بارد", "عصائر", "مشروبات باردة" -> Icons.Default.LocalDrink
        "cake", "sweets", "حلويات", "كيك" -> Icons.Default.Cake
        "burger", "food", "مأكولات", "برجر" -> Icons.Default.Fastfood
        "pizza", "بيتزا" -> Icons.Default.LocalPizza
        "bakery", "معجنات" -> Icons.Default.BakeryDining
        "restaurant", "وجبات" -> Icons.Default.Restaurant
        "hookah", "shisha", "شيشة", "أرجيلة" -> Icons.Default.Air
        "fire", "عروض" -> Icons.Default.LocalFireDepartment
        else -> Icons.Default.RestaurantMenu
    }
}
