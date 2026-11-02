package com.smartoptimize.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smartoptimize.app.navigation.AppNavHost
import com.smartoptimize.app.ui.theme.SmartOptimizeTheme

/**
 * Single-activity host. All screens live in Compose Navigation; all logic
 * lives in ViewModels / repositories — this class stays thin on purpose.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as SmartOptimizeApplication).container

        setContent {
            SmartOptimizeTheme {
                AppNavHost(
                    container = container,
                    ownPackageName = packageName
                )
            }
        }
    }
}
