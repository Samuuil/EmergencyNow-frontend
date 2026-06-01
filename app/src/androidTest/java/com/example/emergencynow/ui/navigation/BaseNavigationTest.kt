package com.example.emergencynow.ui.navigation

import android.Manifest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.example.emergencynow.di.appModule
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import io.mockk.clearAllMocks
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.unloadKoinModules
import org.koin.core.logger.Level

abstract class BaseNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_CONTACTS,
    )

    protected lateinit var env: NavTestEnvironment
    protected lateinit var navController: TestNavHostController

    @Before
    fun setUpKoin() {
        env = NavTestEnvironment()
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidLogger(Level.ERROR)
                androidContext(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext)
                modules(appModule)
            }
        }
        loadKoinModules(env.module)
    }

    @After
    fun tearDownKoin() {
        runCatching { unloadKoinModules(env.module) }
        clearAllMocks()
    }

    protected fun setContent(startDestination: Any) {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
                navigatorProvider.addNavigator(DialogNavigator())
            }
            EmergencyNowTheme {
                AppNavGraph(navController = navController, startDestination = startDestination)
            }
        }
    }

    protected fun currentRoute(): String? =
        navController.currentBackStackEntry?.destination?.route

    protected fun assertOnRoute(routeSimpleName: String) {
        composeTestRule.waitForIdle()
        val route = currentRoute()
        assertTrue(
            "Expected to be on a route containing \"$routeSimpleName\" but was \"$route\"",
            route?.contains(routeSimpleName) == true,
        )
    }
}
