package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.developer.DeveloperDashboardScreen
import com.example.ui.developer.DeveloperInvoicesScreen
import com.example.ui.developer.DeveloperLoginScreen
import com.example.ui.developer.DeveloperScreen
import com.example.ui.developer.DeveloperViewModel
import com.example.ui.theme.CafeCyanPrimary
import com.example.ui.theme.CafeDarkBg
import com.example.ui.theme.CafeTheme

class DeveloperAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val devViewModel: DeveloperViewModel = viewModel()
            val context = LocalContext.current

            CafeTheme(accentColor = CafeCyanPrimary) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding(),
                        color = Color(0xFF020C0E)
                    ) {
                        DeveloperAppNavHost(
                            viewModel = devViewModel,
                            onOpenCashierApp = {
                                val intent = Intent(context, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeveloperAppNavHost(
    viewModel: DeveloperViewModel,
    onOpenCashierApp: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    when (currentScreen) {
        DeveloperScreen.LOGIN -> {
            DeveloperLoginScreen(
                viewModel = viewModel,
                onOpenCashierApp = onOpenCashierApp
            )
        }

        DeveloperScreen.DASHBOARD -> {
            BackHandler {
                onOpenCashierApp()
            }
            DeveloperDashboardScreen(
                viewModel = viewModel,
                onOpenCashierApp = onOpenCashierApp
            )
        }

        DeveloperScreen.INVOICES -> {
            BackHandler {
                viewModel.navigateTo(DeveloperScreen.DASHBOARD)
            }
            DeveloperInvoicesScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(DeveloperScreen.DASHBOARD) }
            )
        }
    }
}
