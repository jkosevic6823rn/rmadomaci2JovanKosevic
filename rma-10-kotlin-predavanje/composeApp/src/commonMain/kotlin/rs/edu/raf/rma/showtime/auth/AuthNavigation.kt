package rs.edu.raf.rma.showtime.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import rs.edu.raf.rma.showtime.auth.landing.AuthLandingScreen
import rs.edu.raf.rma.showtime.auth.login.LoginScreen
import rs.edu.raf.rma.showtime.auth.register.RegisterScreen

private object AuthRoutes {
    const val LANDING = "auth/landing"
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
}

@Composable
fun AuthNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthRoutes.LANDING,
    ) {
        composable(AuthRoutes.LANDING) {
            AuthLandingScreen(
                onLogin = { navController.navigate(AuthRoutes.LOGIN) },
                onRegister = { navController.navigate(AuthRoutes.REGISTER) },
            )
        }

        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                onLoggedIn = {},
                onNavigateToRegister = {
                    navController.navigate(AuthRoutes.REGISTER) {
                        popUpTo(AuthRoutes.LANDING)
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(AuthRoutes.REGISTER) {
            RegisterScreen(
                onRegistered = {},
                onBack = { navController.popBackStack() },
            )
        }
    }
}
