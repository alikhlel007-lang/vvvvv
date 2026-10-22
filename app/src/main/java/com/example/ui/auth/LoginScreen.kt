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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
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

import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }

    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccessMessage.collectAsState()
    val authLoading by viewModel.authLoading.collectAsState()
    val unverifiedEmail by viewModel.unverifiedEmail.collectAsState()

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
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo Header
            CafeLogoHeader(
                size = 110,
                showText = true
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Login Container Card
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
                        text = "Login Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextPrimary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Username / Email input
                    CafeTextField(
                        value = emailOrUsername,
                        onValueChange = { emailOrUsername = it },
                        placeholder = "Email or Username",
                        labelEn = "Username",
                        leadingIcon = Icons.Default.Person,
                        testTag = "login_username_input"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password input
                    CafeTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "••••••••",
                        labelEn = "Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        testTag = "login_password_input"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Remember Me & Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { rememberMe = !rememberMe }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (rememberMe) CafeCyanPrimary else Color(0xFF0F3E44)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (rememberMe) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF032125),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Remember Me",
                                fontSize = 12.sp,
                                color = CafeTextSecondary
                            )
                        }

                        Text(
                            text = "Forgot Password?",
                            fontSize = 12.sp,
                            color = CafeCyanPrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(AppScreen.FORGOT_PASSWORD) }
                                .padding(vertical = 4.dp)
                        )
                    }

                    if (authSuccess != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3E44))
                        ) {
                            Text(
                                text = authSuccess ?: "",
                                color = CafeCyanPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3B151E))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = authError ?: "",
                                    color = Color(0xFFFF8B8B),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )

                                if (unverifiedEmail != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .clickable {
                                                viewModel.resendVerificationEmail(
                                                    email = unverifiedEmail ?: emailOrUsername,
                                                    password = password
                                                )
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                            .background(CafeCyanPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = CafeCyanPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "إعادة إرسال رابط التفعيل الآن",
                                            color = CafeCyanPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Action
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
                            text = "Login / تسجيل الدخول",
                            onClick = { viewModel.login(emailOrUsername, password) },
                            testTag = "login_submit_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Don't have an account
                    Text(
                        text = "Don't have an account?",
                        fontSize = 12.sp,
                        color = CafeTextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Create Account Button
                    CafeSecondaryButton(
                        text = "Create Account",
                        onClick = { viewModel.navigateTo(AppScreen.REGISTER) },
                        testTag = "login_goto_register_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick bypass to customer view or direct dashboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.CUSTOMER_MENU) }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "عرض وتجربة منيو الزبائن كطاولة ->",
                    fontSize = 13.sp,
                    color = CafeCyanPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
