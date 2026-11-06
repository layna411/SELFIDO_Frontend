package com.simats.selfora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.navigation.SelforaNavHost
import com.simats.selfora.ui.theme.SELFORATheme
import com.simats.selfora.ui.theme.SelforaBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        SessionManager.init(applicationContext)
        com.simats.selfora.utils.LanguageManager.init(applicationContext)
        com.simats.selfora.data.repository.PracticeSessionStore.init(applicationContext)
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
