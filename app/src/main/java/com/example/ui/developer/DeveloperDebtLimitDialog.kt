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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Speed
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
fun DeveloperDebtLimitDialog(
    cafe: CafeteriaAdminSummary,
    onDismiss: () -> Unit,
    onConfirm: (limit: Double) -> Unit
) {
    var limitText by remember { mutableStateOf(cafe.maxDebtLimit.toLong().toString()) }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun parseAmount(text: String): Double {
        val clean = text
            .replace(",", "")
            .replace("،", "")
            .replace(" ", "")
            .replace("د.ع", "")
            .map { ch ->
                when (ch) {
                    '٠' -> '0'; '١' -> '1'; '٢' -> '2'; '٣' -> '3'; '٤' -> '4'
                    '٥' -> '5'; '٦' -> '6'; '٧' -> '7'; '٨' -> '8'; '٩' -> '9'
                    else -> ch
                }
            }.joinToString("")
        return clean.toDoubleOrNull() ?: -1.0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF071E23),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = CafeCyanPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تعيين حد الديون المسموح به",
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
                            Text("الديون الحالية غير المحصلة:", color = CafeTextSecondary, fontSize = 11.sp)
                            Text(
                                text = OrderFeeCalculator.formatPrice(cafe.remainingFee, cafe.currency),
                                color = if (cafe.remainingFee >= cafe.maxDebtLimit) Color(0xFFFF5252) else CafeGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("حد الديون الحالي:", color = CafeTextSecondary, fontSize = 11.sp)
                            Text(
                                text = OrderFeeCalculator.formatPrice(cafe.maxDebtLimit, cafe.currency),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "💡 إذا تجاوزت الديون غير المسددة هذا الحد، سيتم إيقاف عمل الكافتيريا ومنيو الزبائن تلقائياً حتى يتم السداد.",
                    color = CafeTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = limitText,
                    onValueChange = {
                        limitText = it
                        validationError = null
                    },
                    label = { Text("سقف الديون (${cafe.currency})", color = CafeTextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = validationError != null,
                    supportingText = {
                        validationError?.let {
                            Text(it, color = Color(0xFFFF5252), fontSize = 11.sp)
                        }
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

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "خيارات سريعة:", color = CafeTextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(25000L, 50000L, 100000L, 200000L, 500000L)
                    presets.forEach { preset ->
                        FilterChip(
                            selected = limitText == preset.toString(),
                            onClick = {
                                limitText = preset.toString()
                                validationError = null
                            },
                            label = { Text("${preset / 1000} ألف", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CafeCyanPrimary,
                                selectedLabelColor = Color(0xFF021215),
                                containerColor = Color(0xFF0A2B32),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = parseAmount(limitText)
                    if (parsed < 0) {
                        validationError = "يرجى كتابة رقم صحيح أكبر أو يساوي صفر"
                    } else {
                        onConfirm(parsed)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CafeCyanPrimary)
            ) {
                Text("حفظ السقف الجديد", color = Color(0xFF021215), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = CafeTextSecondary)
            }
        }
    )
}
