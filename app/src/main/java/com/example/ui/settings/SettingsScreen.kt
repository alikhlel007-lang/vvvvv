package com.example.ui.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
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

    var showProfileDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

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
                        onClick = { viewModel.navigateTo(AppScreen.CASHIER_ORDERS) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CafeSurfaceVariant)
                            .testTag("settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "الرجوع للكاشير",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
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
                contentPadding = PaddingValues(top = 6.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==========================================
                // 1. أولاً: بيانات الكافتيريا والشعار
                // ==========================================
                item {
                    SettingsSectionCard(
                        stepNumber = "1",
                        title = "أولاً: بيانات الكافتيريا والشعار",
                        subtitle = "اسم الكافتيريا، الشعار، اسم المالك، الهاتف والبريد",
                        icon = Icons.Default.Business,
                        accentColor = accentColor
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Current Active Summary Card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CafeInputBg)
                                    .border(1.dp, CafeInputBorder, RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                                    .clickable { showProfileDialog = true },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, accentColor, CircleShape)
                                            .background(Color(0xFF071F22)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!customLogoUri.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = customLogoUri,
                                                contentDescription = "شعار الكافتيريا",
                                                modifier = Modifier
                                                    .size(52.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.cafe_logo),
                                                contentDescription = "الشعار الافتراضي",
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = cafeName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CafeTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "المالك: $cafeOwnerName",
                                            fontSize = 12.sp,
                                            color = CafeTextSecondary
                                        )
                                        Text(
                                            text = "$cafePhone • $cafeEmail",
                                            fontSize = 11.sp,
                                            color = CafeTextSecondary
                                        )
                                    }
                                }

                                Button(
                                    onClick = { showProfileDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        contentColor = Color(0xFF032125)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("تعديل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 2. ثانياً: الطاولات والباركود
                // ==========================================
                item {
                    SettingsSectionCard(
                        stepNumber = "2",
                        title = "ثانياً: الطاولات والباركود",
                        subtitle = "توليد كودات QR لجميع الطاولات وتصدير ملف PDF جاهز للطباعة",
                        icon = Icons.Default.QrCode,
                        accentColor = accentColor
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
                                modifier = Modifier.fillMaxWidth(),
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
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 3. ثالثاً: القوائم والأصناف
                // ==========================================
                item {
                    SettingsSectionCard(
                        stepNumber = "3",
                        title = "ثالثاً: القوائم والأصناف",
                        subtitle = "إضافة وتعديل وحذف الأقسام، تعديل الأسعار، العروض والصور",
                        icon = Icons.Default.RestaurantMenu,
                        accentColor = accentColor
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
                                modifier = Modifier.fillMaxWidth(),
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
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. رابعاً: تعديل التصميم العام (التلاعب بالألوان وغيرها)
                // ==========================================
                item {
                    SettingsSectionCard(
                        stepNumber = "4",
                        title = "رابعاً: تعديل التصميم العام",
                        subtitle = "التلاعب بالألوان الرئيسية، استدارة البطاقات وحجم الخطوط",
                        icon = Icons.Default.Palette,
                        accentColor = accentColor
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
                // 5. خامساً: تسجيل الخروج
                // ==========================================
                item {
                    SettingsSectionCard(
                        stepNumber = "5",
                        title = "خامساً: تسجيل الخروج",
                        subtitle = "تسجيل الخروج من الحساب الحالي وقفل لوحة التحكم",
                        icon = Icons.Default.Logout,
                        accentColor = Color(0xFFFF5252)
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
                                    .height(48.dp)
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
                                        fontSize = 14.sp,
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

    // ==========================================
    // DIALOG: Cafe Profile & Logo (أولاً: بيانات الكافتيريا والشعار)
    // ==========================================
    if (showProfileDialog) {
        var tempName by remember { mutableStateOf(cafeName) }
        var tempOwnerName by remember { mutableStateOf(cafeOwnerName) }
        var tempPhone by remember { mutableStateOf(cafePhone) }
        var tempEmail by remember { mutableStateOf(cafeEmail) }
        var tempLogoUri by remember { mutableStateOf(customLogoUri) }

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                tempLogoUri = uri.toString()
            }
        }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            containerColor = CafeSurface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "بيانات وشعار الكافتيريا",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextPrimary
                    )
                    IconButton(
                        onClick = { showProfileDialog = false },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = CafeTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Logo Picker Header
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Brush.radialGradient(listOf(accentColor, CafeGold)), CircleShape)
                                    .background(Color(0xFF071F22)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!tempLogoUri.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = tempLogoUri,
                                        contentDescription = "شعار الكافتيريا",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.cafe_logo),
                                        contentDescription = "الشعار الافتراضي",
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CafeSurfaceVariant,
                                    contentColor = accentColor
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "رفع صورة للشعار من الهاتف",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "يظهر هذا الشعار في أعلى المينيو عند فتح الزبون للباركود",
                                fontSize = 10.sp,
                                color = CafeTextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    // 1. Cafe Name
                    item {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("اسم الكافتيريا", color = CafeTextSecondary, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_cafe_name_input"),
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
                    }

                    // 2. Owner Name
                    item {
                        OutlinedTextField(
                            value = tempOwnerName,
                            onValueChange = { tempOwnerName = it },
                            label = { Text("اسم صاحب الكافتيريا", color = CafeTextSecondary, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_owner_name_input"),
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
                    }

                    // 3. Phone Number
                    item {
                        OutlinedTextField(
                            value = tempPhone,
                            onValueChange = { tempPhone = it },
                            label = { Text("رقم الهاتف للتواصل", color = CafeTextSecondary, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_phone_input"),
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
                    }

                    // 4. Email
                    item {
                        OutlinedTextField(
                            value = tempEmail,
                            onValueChange = { tempEmail = it },
                            label = { Text("البريد الإلكتروني", color = CafeTextSecondary, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_email_input"),
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
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCafeProfile(
                            name = tempName,
                            phone = tempPhone,
                            ownerName = tempOwnerName,
                            email = tempEmail,
                            customLogoUri = tempLogoUri
                        )
                        Toast.makeText(context, "تم حفظ بيانات وشعار الكافتيريا بنجاح!", Toast.LENGTH_SHORT).show()
                        showProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color(0xFF032125)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("dialog_save_profile_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "حفظ البيانات", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showProfileDialog = false }
                ) {
                    Text(text = "إلغاء", color = CafeTextSecondary, fontSize = 13.sp)
                }
            }
        )
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
}

@Composable
fun SettingsSectionCard(
    stepNumber: String,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = CafeTextSecondary
                    )
                }
            }

            content()
        }
    }
}
