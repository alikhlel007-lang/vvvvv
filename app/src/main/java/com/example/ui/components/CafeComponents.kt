package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CafeCardBorder
import com.example.ui.theme.CafeCyanPrimary
import com.example.ui.theme.CafeGold
import com.example.ui.theme.CafeInputBg
import com.example.ui.theme.CafeInputBorder
import com.example.ui.theme.CafeTextPrimary
import com.example.ui.theme.CafeTextSecondary

@Composable
fun CafeLogoHeader(
    modifier: Modifier = Modifier,
    size: Int = 120,
    showText: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .border(2.dp, Brush.radialGradient(listOf(CafeCyanPrimary, CafeGold, CafeCardBorder)), CircleShape)
                .background(Color(0xFF071F22)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.cafe_logo),
                contentDescription = "Cafe Logo",
                modifier = Modifier
                    .size((size * 0.88).dp)
                    .clip(CircleShape)
            )
        }

        if (showText) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "CAFE",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CafeTextPrimary,
                letterSpacing = 3.sp
            )
        }
    }
}

@Composable
fun CafeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    labelEn: String? = null,
    labelAr: String? = null,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier,
    testTag: String = "input_field",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var passwordVisible by remember { mutableStateOf(!isPassword) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (labelEn != null || labelAr != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (labelEn != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (leadingIcon != null) {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = null,
                                tint = CafeCyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = labelEn,
                            color = CafeTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (labelAr != null) {
                    Text(
                        text = labelAr,
                        color = CafeTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(CafeInputBg)
                .border(1.dp, CafeInputBorder, RoundedCornerShape(26.dp))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(testTag),
                    textStyle = TextStyle(
                        color = CafeTextPrimary,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(CafeCyanPrimary),
                    visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = Color(0xFF4C7075),
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                )

                if (isPassword) {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                            tint = CafeCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CafePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showArrow: Boolean = true,
    testTag: String = "primary_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CafeCyanPrimary,
            contentColor = Color(0xFF032125),
            disabledContainerColor = Color(0xFF0A3C42),
            disabledContentColor = Color(0xFF3B686E)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (showArrow) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CafeSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "secondary_button"
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(26.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = SolidColor(CafeCyanPrimary),
            width = 1.2.dp
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = CafeCyanPrimary
        )
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
