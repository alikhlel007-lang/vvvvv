package com.example.ui.developer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CafeCyanPrimary
import com.example.ui.theme.CafeGold
import com.example.ui.theme.CafeTextSecondary
import com.example.util.OrderFeeCalculator

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeveloperCollectFeeDialog(
    cafe: CafeteriaAdminSummary,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf(if (cafe.remainingFee > 0) cafe.remainingFee.toLong().toString() else "") }
    var noteText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun parseAmount(text: String): Double {
        val clean = text
            .replace(",", "")
            .replace("،", "")
            .replace(" ", "")
            .replace("د.ع", "")
            .replace("IQD", "")
            .map { ch ->
                when (ch) {
                    '٠' -> '0'; '١' -> '1'; '٢' -> '2'; '٣' -> '3'; '٤' -> '4'
                    '٥' -> '5'; '٦' -> '6'; '٧' -> '7'; '٨' -> '8'; '٩' -> '9'
                    else -> ch
                }
            }.joinToString("")
        return clean.toDoubleOrNull() ?: 0.0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF071E23),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = CafeGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تحصيل رسوم الخدمة (1%)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "الكافتيريا: ${cafe.cafeName} (معرف: #${cafe.numericId})",
                    color = CafeCyanPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = Color(0xFF031417),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("إجمالي العمولة المستحقة:", color = CafeTextSecondary, fontSize = 11.sp)
                            Text(OrderFeeCalculator.formatPrice(cafe.serviceFeeTotal, cafe.currency), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المحصل سابقاً:", color = CafeTextSecondary, fontSize = 11.sp)
                            Text(OrderFeeCalculator.formatPrice(cafe.collectedFees, cafe.currency), color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المتبقي للتحصيل حالياً:", color = CafeTextSecondary, fontSize = 11.sp)
                            Text(OrderFeeCalculator.formatPrice(cafe.remainingFee, cafe.currency), color = CafeGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        validationError = null
                    },
                    label = { Text("المبلغ المحصل (${cafe.currency})", color = CafeTextSecondary) },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = CafeGold)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = validationError != null,
                    supportingText = {
                        validationError?.let {
                            Text(it, color = Color(0xFFFF5252), fontSize = 11.sp)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CafeGold,
                        unfocusedBorderColor = Color(0xFF0E4750),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Presets
                if (cafe.remainingFee > 0) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = amountText == cafe.remainingFee.toLong().toString(),
                            onClick = {
                                amountText = cafe.remainingFee.toLong().toString()
                                validationError = null
                            },
                            label = { Text("المتبقي بالكامل (${cafe.remainingFee.toLong()})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CafeGold,
                                selectedLabelColor = Color(0xFF021215),
                                containerColor = Color(0xFF0A2B32),
                                labelColor = Color.White
                            )
                        )

                        val half = (cafe.remainingFee / 2.0).toLong()
                        if (half > 0) {
                            FilterChip(
                                selected = amountText == half.toString(),
                                onClick = {
                                    amountText = half.toString()
                                    validationError = null
                                },
                                label = { Text("النصف ($half)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CafeGold,
                                    selectedLabelColor = Color(0xFF021215),
                                    containerColor = Color(0xFF0A2B32),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("ملاحظة أو طريقة التحصيل (مثال: نقداً، زين كاش...)", color = CafeTextSecondary) },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null, tint = CafeCyanPrimary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CafeCyanPrimary,
                        unfocusedBorderColor = Color(0xFF0E4750),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = parseAmount(amountText)
                    if (amt <= 0) {
                        validationError = "يرجى كتابة مبلغ تحصيل أكبر من صفر"
                    } else {
                        onConfirm(amt, noteText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CafeGold)
            ) {
                Text("تأكيد تسجيل الاستلام", color = Color(0xFF021215), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = CafeTextSecondary)
            }
        }
    )
}
