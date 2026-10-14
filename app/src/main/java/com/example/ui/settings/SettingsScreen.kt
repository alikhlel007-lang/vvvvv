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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Save
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

    var showProfileDialog by remember { mutableStateOf(false) }

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
                            tint = CafeCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "إعدادات الكافتيريا",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )
                        Text(
                            text = cafeName,
                            fontSize = 12.sp,
                            color = CafeCyanPrimary
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION 1: CAFE PROFILE & LOGO (مخفية وتظهر عند الضغط عليها)
                item {
                    SettingsCard(title = "بيانات وشعار الكافتيريا", icon = Icons.Default.Business) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SettingActionRow(
                                title = "تعديل بيانات وشعار الكافتيريا",
                                subtitle = "اسم الكافتيريا، الشعار المرفوع، اسم المالك، الهاتف والبريد",
                                icon = Icons.Default.Business,
                                onClick = { showProfileDialog = true }
                            )

                            // Quick summary pill showing active name & logo
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, CafeCyanPrimary, CircleShape)
                                            .background(Color(0xFF071F22)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!customLogoUri.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = customLogoUri,
                                                contentDescription = "شعار الكافتيريا",
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.cafe_logo),
                                                contentDescription = "الشعار الافتراضي",
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = cafeName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CafeTextPrimary
                                        )
                                        Text(
                                            text = "المالك: $cafeOwnerName • $cafePhone",
                                            fontSize = 11.sp,
                                            color = CafeTextSecondary
                                        )
                                    }
                                }

                                Text(
                                    text = "تعديل",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CafeCyanPrimary
                                )
                            }
                        }
                    }
                }

                // SECTION 2: OPERATIONS & BARCODES (إدارة القوائم والباركودات)
                item {
                    SettingsCard(title = "إدارة العمليات والقوائم", icon = Icons.Default.RestaurantMenu) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Menu and items
                            SettingActionRow(
                                title = "إدارة القوائم والأصناف",
                                subtitle = "إضافة وتعديل وحذف الأقسام والوجبات والأسعار",
                                icon = Icons.Default.RestaurantMenu,
                                onClick = { viewModel.navigateTo(AppScreen.MENU_MANAGEMENT) }
                            )

                            HorizontalDivider(color = CafeCardBorder)

                            // Tables barcode
                            SettingActionRow(
                                title = "باركودات الطاولات وتصدير PDF",
                                subtitle = "توليد كودات الطاولات وتحميل ملف PDF جاهز للطباعة",
                                icon = Icons.Default.QrCode,
                                onClick = { viewModel.navigateTo(AppScreen.TABLES_BARCODE) }
                            )
                        }
                    }
                }

                // SECTION 3: ACCOUNT & LOGOUT (قسم تسجيل الخروج)
                item {
                    SettingsCard(title = "الحساب وتسجيل الخروج", icon = Icons.Default.Logout) {
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
                                        .background(CafeCyanPrimary.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "صاحب الكافتيريا",
                                        fontSize = 11.sp,
                                        color = CafeCyanPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.logout() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("settings_logout_btn"),
                                shape = RoundedCornerShape(24.dp),
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

    // DIALOG: Cafe Profile & Logo (يظهر عند الضغط فقط)
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
                                    .border(2.dp, Brush.radialGradient(listOf(CafeCyanPrimary, CafeGold)), CircleShape)
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
                                colors = ButtonDefaults.outlinedButtonBorder.let {
                                    ButtonDefaults.outlinedButtonColors(
                                        containerColor = CafeSurfaceVariant,
                                        contentColor = CafeCyanPrimary
                                    )
                                }
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
                                text = "يظهر هذا الشعار في أعلى المينيو عند مسح باركود الطاولة",
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
                                    tint = CafeCyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_cafe_name_input"),
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
                                    tint = CafeCyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_owner_name_input"),
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
                                    tint = CafeCyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_phone_input"),
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
                                    tint = CafeCyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_email_input"),
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
                    colors = ButtonDefaults.buttonColors(containerColor = CafeCyanPrimary, contentColor = Color(0xFF032125)),
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
}

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CafeCyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
            }
            content()
        }
    }
}

@Composable
fun SettingActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CafeSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CafeCyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
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
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = CafeCyanPrimary,
            modifier = Modifier.size(16.dp)
        )
    }
}
