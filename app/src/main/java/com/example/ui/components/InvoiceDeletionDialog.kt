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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
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
import com.example.data.model.OrderRecord
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
import com.example.util.OrderFeeCalculator

@Composable
fun InvoiceDeletionDialog(
    order: OrderRecord,
    cafeName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: (code: String, onError: (String) -> Unit) -> Unit
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
                            .background(CafeGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CafeGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "كود موافقة الإدارة للحذف",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CafeTextPrimary
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // ملخص الفاتورة المراد حذفها
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CafeSurfaceVariant)
                        .border(1.dp, CafeCardBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "طاولة ${order.tableNumber} • ${order.customerName.ifBlank { "زبون" }}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )
                        Text(
                            text = "رقم الفاتورة: ${order.id}",
                            fontSize = 10.sp,
                            color = CafeTextSecondary
                        )
                    }
                    Text(
                        text = OrderFeeCalculator.formatPrice(order.totalPrice, order.currency),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4CAF50)
                    )
                }

                // تنبيه الأمان
                Text(
                    text = "لحفظ حقوق تدقيق الحسابات ونسبة الخدمة (1%)، لا يمكن حذف الفاتورة إلا بعد إدخال كود الموافقة المعتمد من المطور/الإدارة.",
                    fontSize = 11.sp,
                    color = CafeTextSecondary,
                    lineHeight = 16.sp
                )

                // زر التواصل المباشر عبر واتساب المطور
                Button(
                    onClick = {
                        InvoiceSecurity.openWhatsAppRequest(
                            context = context,
                            cafeName = cafeName,
                            tableNumber = order.tableNumber,
                            orderId = order.id,
                            amount = order.totalPrice,
                            currency = order.currency
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        text = "💬 طلب الكود عبر واتساب الإدارة (${InvoiceSecurity.DEVELOPER_PHONE})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // حقل إدخال كود الموافقة
                OutlinedTextField(
                    value = enteredCode,
                    onValueChange = {
                        enteredCode = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("أدخل كود الموافقة المكون من 6 أرقام...", fontSize = 12.sp, color = CafeTextSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = CafeGold,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                // رسالة الخطأ إن وجدت
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 11.sp,
                        color = Color(0xFFFF5252),
                        lineHeight = 15.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredCode.isBlank()) {
                        errorMessage = "يرجى إدخال كود الموافقة"
                        return@Button
                    }
                    isSubmitting = true
                    onConfirmDelete(enteredCode) { err ->
                        isSubmitting = false
                        errorMessage = err
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5252),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSubmitting) "جاري التحقق..." else "تأكيد الحذف بالكود",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CafeInputBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CafeTextSecondary)
            ) {
                Text("إلغاء", fontSize = 12.sp)
            }
        }
    )
}
