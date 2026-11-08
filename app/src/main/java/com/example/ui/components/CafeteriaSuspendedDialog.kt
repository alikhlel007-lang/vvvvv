package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CafeGold
import com.example.ui.theme.CafeTextSecondary

@Composable
fun CafeteriaSuspendedDialog(
    reason: String,
    onLogout: () -> Unit
) {
    // Prevent dismissing by back button
    BackHandler(enabled = true) {
        // Do not allow back navigation
    }

    Dialog(
        onDismissRequest = { /* Non-dismissible */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6050303)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D0B)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFEF4444)),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ban / Lock Icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFEF4444).copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Suspended",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "⛔ تم إيقاف عمل الكافتيريا فوراً",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "تم إيقاف هذه الكافتيريا من قِبل إدارة النظام / المطور. تم تعليق استقبال الطلبات وإدارة الطاولات حتى يتم تسوية الحساب وإعادة التفعيل.",
                        fontSize = 13.sp,
                        color = CafeTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    if (reason.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = Color(0xFF261210),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8A2323)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFFF8A80),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "السبب: $reason",
                                    color = Color(0xFFFF8A80),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = Color(0xFF1B1614),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 بمجرد قيام المطور بإلغاء الإيقاف من لوحته، ستُفتح هذه الشاشة تلقائياً في نفس اللحظة وتستأنف عملك دون الحاجة لإعادة التشغيل.",
                            color = CafeGold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF382320)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFFF8A80), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تسجيل الخروج من الحساب",
                            color = Color(0xFFFF8A80),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
