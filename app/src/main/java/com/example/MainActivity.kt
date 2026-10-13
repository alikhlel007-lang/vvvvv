package com.example

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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.auth.ForgotPasswordScreen
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.RegisterScreen
import com.example.ui.cashier.CashierOrdersScreen
import com.example.ui.customer.CustomerMenuScreen
import com.example.ui.menu_management.MenuManagementScreen
import com.example.ui.tables.TablesBarcodeScreen
import com.example.ui.theme.CafeDarkBg
import com.example.ui.theme.CafeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CafeTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding(),
                        color = CafeDarkBg
                    ) {
                        CafeAppNavHost()
                    }
                }
            }
        }
    }
}

@Composable
fun CafeAppNavHost(viewModel: MainViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    when (currentScreen) {
        AppScreen.LOGIN -> {
            LoginScreen(viewModel = viewModel)
        }

        AppScreen.REGISTER -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.LOGIN)
            }
            RegisterScreen(viewModel = viewModel)
        }

        AppScreen.FORGOT_PASSWORD -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.LOGIN)
            }
            ForgotPasswordScreen(viewModel = viewModel)
        }

        AppScreen.CASHIER_ORDERS -> {
            CashierOrdersScreen(viewModel = viewModel)
        }

        AppScreen.MENU_MANAGEMENT -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.CASHIER_ORDERS)
            }
            MenuManagementScreen(viewModel = viewModel)
        }

        AppScreen.TABLES_BARCODE -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.CASHIER_ORDERS)
            }
            TablesBarcodeScreen(viewModel = viewModel)
        }

        AppScreen.CUSTOMER_MENU -> {
            BackHandler {
                if (currentUser != null) {
                    viewModel.navigateTo(AppScreen.CASHIER_ORDERS)
                } else {
                    viewModel.navigateTo(AppScreen.LOGIN)
                }
            }
            CustomerMenuScreen(viewModel = viewModel)
        }
    }
}
