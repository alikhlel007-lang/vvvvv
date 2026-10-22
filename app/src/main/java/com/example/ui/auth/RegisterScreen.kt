package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.CafeLogoHeader
import com.example.ui.components.CafePrimaryButton
import com.example.ui.components.CafeSecondaryButton
import com.example.ui.components.CafeTextField
import com.example.ui.theme.CafeCyanPrimary
import com.example.ui.theme.CafeDarkBg
import com.example.ui.theme.CafeDarkBgGradientEnd
import com.example.ui.theme.CafeSurface
import com.example.ui.theme.CafeTextPrimary
import com.example.ui.theme.CafeTextSecondary

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon

@Composable
fun RegisterScreen(viewModel: MainViewModel) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var cafeName by remember { mutableStateOf("كافيه النخبة") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val authError by viewModel.authError.collectAsState()
    val authLoading by viewModel.authLoading.collectAsState()
    val regSuccessMessage by viewModel.registrationSuccessMessage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        CafeDarkBg,
                        CafeDarkBgGradientEnd,
                        Color(0xFF021214)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CafeLogoHeader(
                size = 90,
                showText = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CafeSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Text(
                        text = "Create Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextPrimary
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Full Name
                    CafeTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "e.g., John Doe",
                        labelEn = "Full Name",
                        labelAr = "الإسم الكامل",
                        leadingIcon = Icons.Default.Person,
                        testTag = "reg_name_input"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Email
                    CafeTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "e.g., john.doe@mail.com",
                        labelEn = "Email",
                        labelAr = "البريد الإلكتروني",
                        leadingIcon = Icons.Default.Email,
                        testTag = "reg_email_input"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password
                    CafeTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "At least 8 characters",
                        labelEn = "Password",
                        labelAr = "كلمة السر",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        testTag = "reg_pass_input"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Confirm Password
                    CafeTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "••••••••",
                        labelEn = "Confirm Password",
                        labelAr = "تأكيد كلمة السر",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        testTag = "reg_confirm_pass_input"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Terms Note
                    Text(
                        text = "By continuing, you agree to our Terms & Privacy\nباستمرارك، أنت توافق على الشروط والأحكام والخصوصية",
                        fontSize = 11.sp,
                        color = CafeTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 16.sp
                    )

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3B151E))
                        ) {
                            Text(
                                text = authError ?: "",
                                color = Color(0xFFFF8B8B),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Submit Register
                    if (authLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = CafeCyanPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        CafePrimaryButton(
                            text = "إنشاء حساب وإرسال رابط التفعيل",
                            showArrow = false,
                            onClick = {
                                viewModel.register(
                                    fullName = fullName,
                                    email = email,
                                    pass = password,
                                    confirmPass = confirmPassword,
                                    cafeName = cafeName
                                )
                            },
                            testTag = "reg_submit_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Already have an account? لديك حساب بالفعل؟",
                        fontSize = 12.sp,
                        color = CafeTextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    CafeSecondaryButton(
                        text = "تسجيل الدخول",
                        onClick = { viewModel.navigateTo(AppScreen.LOGIN) },
                        testTag = "reg_goto_login_button"
                    )
                }
            }
        }

        if (regSuccessMessage != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissRegistrationSuccess() },
                containerColor = CafeSurface,
                shape = RoundedCornerShape(20.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = null,
                        tint = CafeCyanPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "تأكيد البريد الإلكتروني",
                        color = CafeTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = regSuccessMessage ?: "",
                        color = CafeTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    CafePrimaryButton(
                        text = "الانتقال لتسجيل الدخول",
                        onClick = { viewModel.dismissRegistrationSuccess() }
                    )
                }
            )
        }
    }
}
