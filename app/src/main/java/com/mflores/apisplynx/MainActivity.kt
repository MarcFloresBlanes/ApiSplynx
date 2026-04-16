package com.mflores.apisplynx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mflores.apisplynx.ui.theme.ApiSplynxTheme
import com.mflores.apisplynx.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApiSplynxTheme {
                AppNavigation()
            }
        }
    }
}
