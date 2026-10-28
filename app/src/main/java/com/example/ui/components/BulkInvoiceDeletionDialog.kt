package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CafeCardBorder
import com.example.ui.theme.CafeCyanPrimary
import com.example.ui.theme.CafeGold
import com.example.ui.theme.CafeInputBg
import com.example.ui.theme.CafeInputBorder
import com.example.ui.theme.CafeSurface
import com.example.ui.theme.CafeSurfaceVariant
import com.example.ui.theme.CafeTextPrimary
import com.example.ui.theme.CafeTextSecondary
import com.example.util.InvoiceSecurity

@Composable
fun BulkInvoiceDeletionDialog(
    invoiceCount: Int,
    totalSalesText: String,
    totalFeeText: String,
    cafeName: String,
    onDismiss: () -> Unit,
    onConfirmBulkDelete: (code: String, onError: (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var enteredCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CafeSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFF5252).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "تصفير أرشيف الفواتير ⚠️",
                        color = CafeTextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Warning Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF260808))
                        .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تحذير: سيتم حذف كافة الفواتير المسددة نهائياً من قاعدة البيانات السحابية والتطبيق! يتطلب هذا الإجراء موافقة المطور الرسمية.",
                            fontSize = 11.sp,
                            color = Color(0xFFFF8A80),
                            lineHeight = 16.sp
                        )
                    }
                }

                // Summary Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CafeSurfaceVariant)
                        .border(1.dp, CafeCardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("عدد الفواتير المراد حذفها:", fontSize = 12.sp, color = CafeTextSecondary)
                            Text("$invoiceCount فاتورة", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = CafeTextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("إجمالي المبيعات المسددة:", fontSize = 12.sp, color = CafeTextSecondary)
                            Text(totalSalesText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = CafeCyanPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("رسوم الخدمة للمطور (1%):", fontSize = 12.sp, color = CafeTextSecondary)
                            Text(totalFeeText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CafeGold)
                        }
                    }
                }

                // WhatsApp Request Button
                Button(
                    onClick = {
                        InvoiceSecurity.openWhatsAppBulkDeleteRequest(
                            context = context,
                            cafeName = cafeName,
                            invoiceCount = invoiceCount,
                            totalSalesText = totalSalesText
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        text = "طلب كود الموافقة الشامل عبر واتساب 💬",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Text(
                    text = "أدخل كود الموافقة الشامل المكون من 6 أرقام المستلم من المطور (أو كود الطوارئ الرئيسي):",
                    fontSize = 11.sp,
                    color = CafeTextSecondary,
                    lineHeight = 16.sp
                )

                // PIN Input
                OutlinedTextField(
                    value = enteredCode,
                    onValueChange = {
                        if (it.length <= 10) {
                            enteredCode = it.filter { ch -> ch.isDigit() }
                            errorMessage = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("أدخل الكود الشامل (مثلاً 132132)", fontSize = 12.sp, color = CafeTextSecondary.copy(alpha = 0.5f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = CafeGold, modifier = Modifier.size(18.dp))
                    },
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = Color(0xFFFF5252), fontSize = 11.sp)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CafeInputBg,
                        unfocusedContainerColor = CafeInputBg,
                        focusedBorderColor = CafeCyanPrimary,
                        unfocusedBorderColor = CafeInputBorder,
                        focusedTextColor = CafeTextPrimary,
                        unfocusedTextColor = CafeTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredCode.isBlank()) {
                        errorMessage = "يرجى إدخال كود الموافقة أولاً"
                        return@Button
                    }
                    isSubmitting = true
                    onConfirmBulkDelete(enteredCode) { err ->
                        isSubmitting = false
                        errorMessage = err
                    }
                },
                enabled = !isSubmitting && enteredCode.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC62828),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isSubmitting) "جاري التحقق والتصفير..." else "تأكيد تصفير الأرشيف نهائياً 🗑️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, CafeCardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إلغاء", color = CafeTextSecondary, fontSize = 12.sp)
            }
        }
    )
}
