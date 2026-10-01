package com.simats.selfora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.navigation.SelforaNavHost
import com.simats.selfora.ui.theme.SELFORATheme
import com.simats.selfora.ui.theme.SelforaBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(applicationContext)
        setContent {
            SELFORATheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SelforaBackground
                ) {
                    SelforaNavHost()
                }
            }
        }
    }
}