package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.example.ui.components.CafeTextField
import com.example.ui.theme.CafeCyanPrimary
import com.example.ui.theme.CafeDarkBg
import com.example.ui.theme.CafeDarkBgGradientEnd
import com.example.ui.theme.CafeSurface
import com.example.ui.theme.CafeTextPrimary
import com.example.ui.theme.CafeTextSecondary

@Composable
fun ForgotPasswordScreen(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccessMessage.collectAsState()

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
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CafeLogoHeader(
                size = 100,
                showText = true
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CafeSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Forgot Password",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Enter your email address to receive a password reset link.\nأدخل بريدك الإلكتروني لتلقي رابط إعادة تعيين كلمة السر",
                        fontSize = 12.sp,
                        color = CafeTextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    CafeTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "e.g., john.doe@mail.com",
                        labelEn = "Email Address",
                        labelAr = "عنوان البريد الإلكتروني",
                        leadingIcon = Icons.Default.Email,
                        testTag = "forgot_email_input"
                    )

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = authError ?: "",
                            color = Color(0xFFFF6B6B),
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (authSuccess != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = authSuccess ?: "",
                            color = CafeCyanPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    CafePrimaryButton(
                        text = "إرسال رابط إعادة تعيين",
                        onClick = { viewModel.forgotPassword(email) },
                        testTag = "forgot_submit_button"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "العودة لتسجيل الدخول",
                        fontSize = 14.sp,
                        color = CafeCyanPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(AppScreen.LOGIN) }
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
