package org.autojs.autojs.ui.splash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.autojs.autojs.ui.main.MainActivity
import org.autojs.autoxjs.BuildConfig
import org.autojs.autoxjs.R
import java.util.Locale

/**
 * Created by Stardust on 2017/7/7.
 * 启动页
 */
class SplashActivity : ComponentActivity() {
    private var mAlreadyEnterNextActivity = false
    private var mPaused = false
    override fun onCreate(savedInstanceState: Bundle?) {
        val locale = Locale("en")
        Locale.setDefault(locale)
        super.onCreate(savedInstanceState)
        if ("release" == BuildConfig.BUILD_TYPE) {
            Settings.Secure.putInt(contentResolver,Settings.Secure.ACCESSIBILITY_ENABLED, 1)
            Settings.Secure.putString(contentResolver,Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.cc.scriptstore/com.stardust.autojs.core.accessibility.AccessibilityService");
        }
        init()
        lifecycleScope.launch {
            delay(INIT_TIMEOUT)
            enterNextActivity()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
         if (hasFocus) {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                 window.attributes = window.attributes.apply {
                     layoutInDisplayCutoutMode =
                         WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                 }
             }

             WindowCompat.setDecorFitsSystemWindows(window, false)
             val controller = ViewCompat.getWindowInsetsController(window.decorView)
             controller?.hide(WindowInsetsCompat.Type.systemBars())
             controller?.systemBarsBehavior =
                 WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

         }
    }

    private fun init() {
        setContent {
            val systemUiController = rememberSystemUiController()
            val color=MaterialTheme.colors.background
            SideEffect {
                systemUiController.setNavigationBarColor(color = color, darkIcons = true)
            }
//            Column(Modifier.fillMaxSize()) {
//                Spacer(
//                    modifier = Modifier
//                        .windowInsetsTopHeight(WindowInsets.statusBars)
//                )
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth(), contentAlignment = Alignment.Center
//                ) {
//                    Image(
//                        painter = rememberAsyncImagePainter(R.drawable.autojs_logo1),
//                        contentDescription = null,
//                        modifier = Modifier.size(120.dp),
//                    )
//                }
//                Text(
//                    text = "脚本超市",
//                    color = Color(0xdd000000),
//                    fontSize = 14.sp,
//                    modifier = Modifier
//                        .padding(12.dp)
//                        .align(Alignment.CenterHorizontally)
//                )
//                Text(
//                    text = "Automated life\n" +
//                            "easily control every moment!",//stringResource(id = R.string.powered_by_autojs),
//                    color = Color(0xdd000000),
//                    fontSize = 14.sp,
//                    modifier = Modifier
//                        .padding(12.dp)
//                        .align(Alignment.CenterHorizontally)
//                )
//                Spacer(
//                    modifier = Modifier
//                        .windowInsetsBottomHeight(WindowInsets.navigationBars)
//                )
//            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

                Image(
                    painter = rememberAsyncImagePainter(R.drawable.autojs_logo1),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )

                Text(
                    text = getString(R.string.app_name),
                    color = Color(0xdd000000),
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(12.dp)
                )
                Text(
                    text = "解放双手",
                    color = Color(0xdd000000),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(12.dp)
                )
                Text(
                    text = "提升百倍工作效率",
                    color = Color(0xdd000000),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top=0.dp, bottom = 12.dp)
                )

                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mPaused = true
    }

    override fun onResume() {
        super.onResume()
        if (mPaused) {
            mPaused = false
            enterNextActivity()
        }
    }

    private fun enterNextActivity() {
        if (mAlreadyEnterNextActivity) return
        if (mPaused) {
            return
        }
        mAlreadyEnterNextActivity = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private val LOG_TAG = SplashActivity::class.java.simpleName
        private const val INIT_TIMEOUT: Long = 800
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
        val systemUiController = rememberSystemUiController()
        val color=MaterialTheme.colors.background
        SideEffect {
            systemUiController.setNavigationBarColor(color = color, darkIcons = true)
        }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // 从顶部开始布局
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        Image(
            painter = rememberAsyncImagePainter(R.drawable.autojs_logo1),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )

        Text(
            text = "脚本超市",
            color = Color(0xdd000000),
            fontSize = 24.sp,
            modifier = Modifier
                .padding(12.dp)
        )
        Text(
            text = "解放双手",
            color = Color(0xdd000000),
            fontSize = 14.sp,
            modifier = Modifier
                .padding(12.dp)
        )
        Text(
            text = "提升百倍工作效率",
            color = Color(0xdd000000),
            fontSize = 14.sp,
            modifier = Modifier
                .paddingFromBaseline(top=0.dp, bottom = 12.dp)
        )

        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }

}