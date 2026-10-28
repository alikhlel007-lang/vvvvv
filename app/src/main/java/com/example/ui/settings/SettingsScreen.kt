package com.example.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.DeleteSweep
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStatus
import com.example.ui.components.BulkInvoiceDeletionDialog
import com.example.ui.components.InvoiceDeletionDialog
import com.example.util.OrderFeeCalculator
import com.example.util.ReceiptPrinter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
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
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val cafeName by viewModel.cafeName.collectAsState()
    val cafeOwnerName by viewModel.cafeOwnerName.collectAsState()
    val cafePhone by viewModel.cafePhone.collectAsState()
    val cafeEmail by viewModel.cafeEmail.collectAsState()
    val customLogoUri by viewModel.cafeCustomLogoUri.collectAsState()

    val tables by viewModel.tables.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()
    val orders by viewModel.orders.collectAsState()
    var invoiceSearchQuery by remember { mutableStateOf("") }

    // Dynamic Theme Customization States
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

    // Editable profile fields
    var editCafeName by remember(cafeName) { mutableStateOf(cafeName) }
    var editOwnerName by remember(cafeOwnerName) { mutableStateOf(cafeOwnerName) }
    var editPhone by remember(cafePhone) { mutableStateOf(cafePhone) }
    var editEmail by remember(cafeEmail) { mutableStateOf(cafeEmail) }
    var editLogoUri by remember(customLogoUri) { mutableStateOf(customLogoUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            editLogoUri = uri.toString()
        }
    }

    // Accordion State - None expanded by default so all options are at the same clean level
    var expandedSection by remember { mutableStateOf<String?>(null) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var orderToDelete by remember { mutableStateOf<OrderRecord?>(null) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }

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
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.CASHIER_ORDERS) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CafeSurfaceVariant,
                            contentColor = accentColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Text(
                            text = "عودة",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "الإعدادات العامة",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )
                        Text(
                            text = cafeName,
                            fontSize = 12.sp,
                            color = accentColor
                        )
                    }
                }

                // Quick preview menu button
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.CUSTOMER_MENU) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CafeSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "معاينة المنيو",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ==========================================
                // 1. بيانات الكافتيريا والشعار
                // ==========================================
                item {
                    val isExpanded = expandedSection == "cafe_profile"
                    SettingsOptionCard(
                        title = "بيانات الكافتيريا والشعار",
                        subtitle = "اسم الكافتيريا، الشعار، اسم المالك، الهاتف والبريد",
                        icon = Icons.Default.Business,
                        accentColor = accentColor,
                        isExpanded = isExpanded,
                        onHeaderClick = {
                            expandedSection = if (isExpanded) null else "cafe_profile"
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Logo Upload and Preview
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CafeInputBg)
                                    .border(1.dp, CafeInputBorder, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, Brush.radialGradient(listOf(accentColor, CafeGold)), CircleShape)
                                            .background(Color(0xFF071F22)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!editLogoUri.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = editLogoUri,
                                                contentDescription = "شعار الكافتيريا",
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.cafe_logo),
                                                contentDescription = "الشعار الافتراضي",
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "شعار الكافتيريا في المنيو",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CafeTextPrimary
                                        )
                                        Text(
                                            text = "يظهر في أعلى المينيو عند مسح QR",
                                            fontSize = 11.sp,
                                            color = CafeTextSecondary
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = CafeSurfaceVariant,
                                        contentColor = accentColor
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text("تغيير الشعار", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // 1. Cafe Name
                            OutlinedTextField(
                                value = editCafeName,
                                onValueChange = { editCafeName = it },
                                label = { Text("اسم الكافتيريا", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Business, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.fillMaxWidth().testTag("settings_cafe_name_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CafeInputBg,
                                    unfocusedContainerColor = CafeInputBg,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = CafeInputBorder,
                                    focusedTextColor = CafeTextPrimary,
                                    unfocusedTextColor = CafeTextPrimary
                                ),
                                singleLine = true
                            )

                            // 2. Owner Name
                            OutlinedTextField(
                                value = editOwnerName,
                                onValueChange = { editOwnerName = it },
                                label = { Text("اسم صاحب الكافتيريا", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.fillMaxWidth().testTag("settings_owner_name_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CafeInputBg,
                                    unfocusedContainerColor = CafeInputBg,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = CafeInputBorder,
                                    focusedTextColor = CafeTextPrimary,
                                    unfocusedTextColor = CafeTextPrimary
                                ),
                                singleLine = true
                            )

                            // 3. Phone Number
                            OutlinedTextField(
                                value = editPhone,
                                onValueChange = { editPhone = it },
                                label = { Text("رقم الهاتف", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.fillMaxWidth().testTag("settings_phone_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CafeInputBg,
                                    unfocusedContainerColor = CafeInputBg,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = CafeInputBorder,
                                    focusedTextColor = CafeTextPrimary,
                                    unfocusedTextColor = CafeTextPrimary
                                ),
                                singleLine = true
                            )

                            // 4. Email
                            OutlinedTextField(
                                value = editEmail,
                                onValueChange = { editEmail = it },
                                label = { Text("البريد الإلكتروني", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.fillMaxWidth().testTag("settings_email_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CafeInputBg,
                                    unfocusedContainerColor = CafeInputBg,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = CafeInputBorder,
                                    focusedTextColor = CafeTextPrimary,
                                    unfocusedTextColor = CafeTextPrimary
                                ),
                                singleLine = true
                            )

                            // Save Button
                            Button(
                                onClick = {
                                    viewModel.updateCafeProfile(
                                        name = editCafeName,
                                        phone = editPhone,
                                        ownerName = editOwnerName,
                                        email = editEmail,
                                        customLogoUri = editLogoUri
                                    )
                                    Toast.makeText(context, "تم حفظ بيانات وشعار الكافتيريا بنجاح!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("settings_save_profile_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = Color(0xFF032125)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("حفظ بيانات الكافتيريا والشعار", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 2. الطاولات والباركود
                // ==========================================
                item {
                    val isExpanded = expandedSection == "tables_barcode"
                    SettingsOptionCard(
                        title = "الطاولات والباركود",
                        subtitle = "توليد كودات QR لجميع الطاولات وتصدير ملف PDF للطباعة",
                        icon = Icons.Default.QrCode,
                        accentColor = accentColor,
                        isExpanded = isExpanded,
                        onHeaderClick = {
                            expandedSection = if (isExpanded) null else "tables_barcode"
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CafeInputBg)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "طاولات الكافتيريا المسجلة",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CafeTextPrimary
                                        )
                                        Text(
                                            text = "إجمالي ${tables.size} طاولة مع كود QR مخصص لكل طاولة",
                                            fontSize = 11.sp,
                                            color = CafeTextSecondary
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.TABLES_BARCODE) },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CafeSurfaceVariant,
                                    contentColor = accentColor
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(accentColor, CafeCardBorderGlow)),
                                    width = 1.dp
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("إدارة باركودات الطاولات وتصدير PDF للطباعة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 3. القوائم والأصناف
                // ==========================================
                item {
                    val isExpanded = expandedSection == "menu_items"
                    SettingsOptionCard(
                        title = "القوائم والأصناف",
                        subtitle = "إضافة وتعديل الأقسام والأسعار والعروض والصور",
                        icon = Icons.Default.RestaurantMenu,
                        accentColor = accentColor,
                        isExpanded = isExpanded,
                        onHeaderClick = {
                            expandedSection = if (isExpanded) null else "menu_items"
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CafeInputBg)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.RestaurantMenu,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "مخزون القائمة والأقسام",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CafeTextPrimary
                                        )
                                        Text(
                                            text = "${categories.size} أقسام • ${menuItems.size} وجبة ومشروب",
                                            fontSize = 11.sp,
                                            color = CafeTextSecondary
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.MENU_MANAGEMENT) },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CafeSurfaceVariant,
                                    contentColor = accentColor
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(accentColor, CafeCardBorderGlow)),
                                    width = 1.dp
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("فتح إدارة القوائم والأصناف والأسعار", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. تعديل التصميم العام
                // ==========================================
                item {
                    val isExpanded = expandedSection == "general_design"
                    SettingsOptionCard(
                        title = "تعديل التصميم العام",
                        subtitle = "التلاعب بالألوان الرئيسية، استدارة البطاقات وحجم الخطوط",
                        icon = Icons.Default.Palette,
                        accentColor = accentColor,
                        isExpanded = isExpanded,
                        onHeaderClick = {
                            expandedSection = if (isExpanded) null else "general_design"
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // 1. Theme Color Palette
                            Text(
                                text = "اختيار لون السمة الرئيسي (Accent Color):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CafeTextPrimary
                            )

                            val colorOptions = listOf(
                                Triple("#00DFC0", "نيون سماوي", Color(0xFF00DFC0)),
                                Triple("#F5B041", "ذهبي فاخر", Color(0xFFF5B041)),
                                Triple("#10B981", "زمردي ملكي", Color(0xFF10B981)),
                                Triple("#FF6F00", "برتقالي دافئ", Color(0xFFFF6F00)),
                                Triple("#2979FF", "أزرق ملكي", Color(0xFF2979FF)),
                                Triple("#A855F7", "بنفسجي نيون", Color(0xFFA855F7)),
                                Triple("#EF4444", "أحمر ياقوتي", Color(0xFFEF4444))
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(colorOptions) { (hex, name, color) ->
                                    val isSelected = themeAccentHex.equals(hex, ignoreCase = true)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            viewModel.updateThemeAccentColor(hex)
                                            Toast.makeText(context, "تم تطبيق اللون: $name", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) Color.White else Color.Transparent,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color(0xFF032125),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = name,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) accentColor else CafeTextSecondary
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = CafeCardBorder, modifier = Modifier.padding(vertical = 4.dp))

                            // 2. Card Corner Style
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.RoundedCorner, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "نمط استدارة البطاقات والأزرار:", fontSize = 12.sp, color = CafeTextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val cornerOptions = listOf(
                                    Pair(22, "عصري مستدير"),
                                    Pair(14, "متوازن أنيق"),
                                    Pair(8, "كلاسيكي")
                                )
                                cornerOptions.forEach { (radius, label) ->
                                    val isCur = cardRadiusDp == radius
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(radius.dp))
                                            .background(if (isCur) accentColor else CafeInputBg)
                                            .border(1.dp, if (isCur) accentColor else CafeInputBorder, RoundedCornerShape(radius.dp))
                                            .clickable { viewModel.updateCardCornerRadius(radius) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCur) Color(0xFF032125) else CafeTextPrimary
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = CafeCardBorder, modifier = Modifier.padding(vertical = 4.dp))

                            // 3. Font Scale
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.FormatSize, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "حجم خط القوائم والنصوص:", fontSize = 12.sp, color = CafeTextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val fontOptions = listOf(
                                    Pair(1.0f, "حجم عادي (100%)"),
                                    Pair(1.15f, "مكبّر للقراءة (+15%)")
                                )
                                fontOptions.forEach { (scale, label) ->
                                    val isCur = fontScale == scale
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isCur) accentColor else CafeInputBg)
                                            .border(1.dp, if (isCur) accentColor else CafeInputBorder, RoundedCornerShape(12.dp))
                                            .clickable { viewModel.updateFontScale(scale) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCur) Color(0xFF032125) else CafeTextPrimary
                                        )
                                    }
                                }
                            }

                            // 4. Live Preview Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(cardRadiusDp.dp))
                                    .background(CafeSurfaceVariant)
                                    .border(1.2.dp, accentColor, RoundedCornerShape(cardRadiusDp.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "✨ معاينة حية للمظهر الجديد",
                                            fontSize = (13 * fontScale).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CafeTextPrimary
                                        )
                                        Text(
                                            text = "يتم تطبيق هذا النمط فوراً عبر كافة صفحات التطبيق",
                                            fontSize = (11 * fontScale).sp,
                                            color = CafeTextSecondary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(cardRadiusDp.dp / 2))
                                            .background(accentColor)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "زر نموذجي",
                                            fontSize = (11 * fontScale).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF032125)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 5. أرشيف الفواتير المسددة (سجل دائم ومحمي)
                // ==========================================
                item {
                    val isExpanded = expandedSection == "paid_invoices_archive"
                    val paidOrders = remember(orders) {
                        orders.filter { it.status == OrderStatus.PAID }.sortedByDescending { it.timestamp }
                    }
                    val filteredPaidOrders = remember(paidOrders, invoiceSearchQuery) {
                        if (invoiceSearchQuery.isBlank()) {
                            paidOrders
                        } else {
                            val q = invoiceSearchQuery.trim().lowercase()
                            paidOrders.filter {
                                "طاولة ${it.tableNumber}".lowercase().contains(q) ||
                                it.tableNumber.toString().contains(q) ||
                                it.customerName.lowercase().contains(q) ||
                                it.itemsSummary.lowercase().contains(q)
                            }
                        }
                    }

                    val totalPaidAmount = remember(paidOrders) { paidOrders.sumOf { it.totalPrice } }
                    val totalServiceFees = remember(paidOrders) { paidOrders.sumOf { it.serviceFee } }
                    val mainCurrency = paidOrders.firstOrNull()?.currency ?: "د.ع"

                    SettingsOptionCard(
                        title = "أرشيف الفواتير المسددة (سجل دائم)",
                        subtitle = "حفظ محمي لجميع الفواتير المسددة - الحذف من لوحة المطور حصراً",
                        icon = Icons.Default.Receipt,
                        accentColor = CafeGold,
                        isExpanded = isExpanded,
                        onHeaderClick = {
                            expandedSection = if (isExpanded) null else "paid_invoices_archive"
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // 1. إحصائيات سريعة للفواتير المحفوظة
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CafeInputBg)
                                    .border(1.dp, CafeInputBorder, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("الفواتير المحفوظة", fontSize = 11.sp, color = CafeTextSecondary)
                                    Text("${paidOrders.size}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CafeCyanPrimary)
                                }
                                Box(modifier = Modifier.width(1.dp).height(28.dp).background(CafeInputBorder))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("المبيعات المسددة", fontSize = 11.sp, color = CafeTextSecondary)
                                    Text(
                                        OrderFeeCalculator.formatPrice(totalPaidAmount, mainCurrency),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                                Box(modifier = Modifier.width(1.dp).height(28.dp).background(CafeInputBorder))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("رسوم الخدمة (1%)", fontSize = 11.sp, color = CafeTextSecondary)
                                    Text(
                                        OrderFeeCalculator.formatPrice(totalServiceFees, mainCurrency),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CafeGold
                                    )
                                }
                            }

                            // 2. تنبيه الحماية والأمان الصارم
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF2A2004))
                                    .border(1.dp, CafeGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "حماية الحسابات",
                                    tint = CafeGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تنبيه نظام: يتم حفظ الفواتير المسددة هنا تلقائياً ولا يمكن حذفها من التطبيق. حذف الفواتير وتصفية الحسابات يتم حصراً من لوحة تحكم المطور/الإدارة لضمان نزاهة التدقيق وتتبع رسوم الخدمة (1%).",
                                    fontSize = 11.sp,
                                    color = CafeGold,
                                    lineHeight = 16.sp
                                )
                            }

                            // زر تصفير وحذف كافة الفواتير المسددة (محمي بكود موافقة شامل من المطور)
                            if (paidOrders.isNotEmpty()) {
                                Button(
                                    onClick = { showBulkDeleteDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF351214),
                                        contentColor = Color(0xFFFF6B6B)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تصفير الأرشيف بالكامل (${paidOrders.size} فواتير) ⚠️🔒",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // 3. شريط البحث
                            OutlinedTextField(
                                value = invoiceSearchQuery,
                                onValueChange = { invoiceSearchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("بحث برقم الطاولة أو اسم الزبون أو المادة...", fontSize = 12.sp, color = CafeTextSecondary) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CafeTextSecondary) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CafeGold,
                                    unfocusedBorderColor = CafeInputBorder,
                                    focusedContainerColor = CafeInputBg,
                                    unfocusedContainerColor = CafeInputBg,
                                    focusedTextColor = CafeTextPrimary,
                                    unfocusedTextColor = CafeTextPrimary
                                )
                            )

                            // 4. قائمة الفواتير
                            if (filteredPaidOrders.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CafeInputBg)
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Receipt,
                                            contentDescription = null,
                                            tint = CafeTextSecondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (paidOrders.isEmpty()) "لا توجد فواتير مسددة محفوظة حتى الآن" else "لا توجد نتائج مطابقة للبحث",
                                            fontSize = 12.sp,
                                            color = CafeTextSecondary
                                        )
                                    }
                                }
                            } else {
                                val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd • hh:mm a", Locale("ar")) }
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    filteredPaidOrders.forEach { invoice ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = CafeSurfaceVariant),
                                            border = BorderStroke(1.dp, CafeCardBorder)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
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
                                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                                        ) {
                                                            Text(
                                                                text = "طاولة ${invoice.tableNumber}",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = Color(0xFF032125)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = invoice.customerName.ifBlank { "زبون" },
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = CafeTextPrimary
                                                        )
                                                    }

                                                    Text(
                                                        text = dateFormat.format(Date(invoice.timestamp)),
                                                        fontSize = 10.sp,
                                                        color = CafeTextSecondary
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = invoice.itemsSummary.ifBlank { "تفاصيل الطلب المسدد" },
                                                    fontSize = 11.sp,
                                                    color = CafeTextSecondary,
                                                    lineHeight = 15.sp
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))
                                                HorizontalDivider(color = CafeCardBorder, thickness = 0.5.dp)
                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text("المجموع: ", fontSize = 11.sp, color = CafeTextSecondary)
                                                            Text(
                                                                text = OrderFeeCalculator.formatPrice(invoice.totalPrice, invoice.currency),
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = Color(0xFF4CAF50)
                                                            )
                                                        }
                                                        Text(
                                                            text = "رسوم الخدمة (1%): ${OrderFeeCalculator.formatPrice(invoice.serviceFee, invoice.currency)}",
                                                            fontSize = 10.sp,
                                                            color = CafeGold
                                                        )
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        // زر طباعة الفاتورة
                                                        Button(
                                                            onClick = {
                                                                ReceiptPrinter.printInvoice(
                                                                    context = context,
                                                                    tableNumber = invoice.tableNumber,
                                                                    orders = listOf(invoice),
                                                                    cafeName = cafeName
                                                                )
                                                                Toast.makeText(context, "جاري إرسال الفاتورة إلى الطابعة 🖨️", Toast.LENGTH_SHORT).show()
                                                            },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = CafeCyanPrimary,
                                                                contentColor = Color(0xFF032125)
                                                            ),
                                                            shape = RoundedCornerShape(8.dp),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                                                        ) {
                                                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("طباعة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }

                                                        Spacer(modifier = Modifier.width(6.dp))

                                                        Button(
                                                            onClick = { orderToDelete = invoice },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = Color(0xFF2A1515),
                                                                contentColor = Color(0xFFFF6B6B)
                                                            ),
                                                            shape = RoundedCornerShape(8.dp),
                                                            border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.5f)),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp)
                                                        ) {
                                                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(11.dp))
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text("طلب حذف 🔒", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 6. تسجيل الخروج
                // ==========================================
                item {
                    val isExpanded = expandedSection == "logout"
                    SettingsOptionCard(
                        title = "تسجيل الخروج",
                        subtitle = "تسجيل الخروج من الحساب وقفل لوحة التحكم",
                        icon = Icons.Default.Logout,
                        accentColor = Color(0xFFFF5252),
                        isExpanded = isExpanded,
                        onHeaderClick = {
                            expandedSection = if (isExpanded) null else "logout"
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = cafeOwnerName.ifBlank { currentUser?.fullName ?: "مدير الكافتيريا" },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CafeTextPrimary
                                    )
                                    Text(
                                        text = cafeEmail.ifBlank { currentUser?.email ?: "admin@cafe.com" },
                                        fontSize = 12.sp,
                                        color = CafeTextSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "صاحب الكافتيريا",
                                        fontSize = 11.sp,
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = { showLogoutConfirmDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("settings_logout_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3B1519),
                                    contentColor = Color(0xFFFF6B6B)
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFFC62828))),
                                    width = 1.2.dp
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تسجيل الخروج من الحساب",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG: Logout Confirmation
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            containerColor = CafeSurface,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "تأكيد تسجيل الخروج",
                    color = CafeTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في تسجيل الخروج؟ ستحتاج لتسجيل الدخول مرة أخرى للوصول إلى لوحة التحكم.",
                    color = CafeTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("نعم، تسجيل الخروج", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("إلغاء", color = CafeTextSecondary)
                }
            }
        )
    }

    // DIALOG: Protected Invoice Deletion with Developer WhatsApp Approval Code
    if (orderToDelete != null) {
        InvoiceDeletionDialog(
            order = orderToDelete!!,
            cafeName = cafeName,
            onDismiss = { orderToDelete = null },
            onConfirmDelete = { code, onError ->
                val target = orderToDelete ?: return@InvoiceDeletionDialog
                viewModel.deleteAuthorizedPaidOrder(
                    order = target,
                    code = code,
                    onSuccess = {
                        orderToDelete = null
                        Toast.makeText(context, "تم حذف الفاتورة بنجاح بعد التحقق من كود الإدارة ✅", Toast.LENGTH_SHORT).show()
                    },
                    onError = onError
                )
            }
        )
    }

    // DIALOG: Protected Bulk Invoice Deletion (Wipe Archive) with Developer Approval
    if (showBulkDeleteDialog) {
        val paidOrders = orders.filter { it.status == OrderStatus.PAID }
        val totalAmount = paidOrders.sumOf { it.totalPrice }
        val totalFee = paidOrders.sumOf { it.serviceFee }
        val curr = paidOrders.firstOrNull()?.currency ?: "د.ع"

        BulkInvoiceDeletionDialog(
            invoiceCount = paidOrders.size,
            totalSalesText = OrderFeeCalculator.formatPrice(totalAmount, curr),
            totalFeeText = OrderFeeCalculator.formatPrice(totalFee, curr),
            cafeName = cafeName,
            onDismiss = { showBulkDeleteDialog = false },
            onConfirmBulkDelete = { code, onError ->
                viewModel.deleteAllAuthorizedPaidOrders(
                    code = code,
                    onSuccess = { count ->
                        showBulkDeleteDialog = false
                        Toast.makeText(context, "تم تصفير الأرشيف وحذف $count فاتورة مسددة بنجاح ✅", Toast.LENGTH_LONG).show()
                    },
                    onError = onError
                )
            }
        )
    }
}

/**
 * Clean uniform setting option card at one hierarchy level.
 * When collapsed, shows the title, subtitle, icon, and chevron.
 * When clicked, expands and displays the settings controls.
 */
@Composable
fun SettingsOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onHeaderClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CafeSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isExpanded) listOf(accentColor.copy(alpha = 0.8f), CafeCardBorderGlow)
                else listOf(CafeCardBorder, CafeCardBorder)
            ),
            width = if (isExpanded) 1.2.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row - Identical level for all options
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = CafeTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onHeaderClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "إغلاق" else "فتح",
                        tint = if (isExpanded) accentColor else CafeTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        color = CafeCardBorder,
                        modifier = Modifier.padding(top = 14.dp, bottom = 14.dp)
                    )
                    content()
                }
            }
        }
    }
}
