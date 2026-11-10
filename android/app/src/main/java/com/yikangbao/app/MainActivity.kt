package com.yikangbao.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.yikangbao.app.data.repository.AuthRepositoryImpl
import com.yikangbao.app.ui.navigation.YiKangBaoNavHost
import com.yikangbao.app.ui.theme.YiKangBaoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // M1：冷启动恢复登录态（DataStore → AuthSession 进程内共享）
        lifecycleScope.launch { authRepository.restoreSession() }
        setContent {
            YiKangBaoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    YiKangBaoNavHost()
                }
            }
        }
    }
}
