package com.xubobo.hyperscreen

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.xubobo.hyperscreen.ui.theme.HyperScreenTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

const val TAG = "xu"

class MainActivity : FragmentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HyperScreenTheme {

                val hazeState = remember {
                    HazeState()
                }

                Scaffold(modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        GlassMorphicBottomNavigation(hazeState)
                    }
                ) { innerPadding ->

                    LazyColumn(
                        Modifier
                            .haze(
                                hazeState,
                                style = HazeStyle(
                                    tint = Color.Black.copy(alpha = 0.2f),
                                    blurRadius = 30.dp
                                )
                            )
                            .fillMaxSize(),
                        contentPadding = innerPadding
                    ) {

//                        item {
//                            LinePath(Modifier.fillMaxWidth().height(50.dp))
//                        }

                        items(30) {
                            Image(
                                painter = painterResource(id = R.drawable.ava),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun takePhoto() {
        val intent = Intent()
        intent.apply {
            action = MediaStore.ACTION_IMAGE_CAPTURE
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        startActivity(intent)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun takePhotoV2() {
        PictureSelector.create(this)
            .openCamera(SelectMimeType.ofImage())
            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: ArrayList<LocalMedia?>) {
                    result[0]?.cutPath?.let { path ->
                    }
                }

                override fun onCancel() {}
            })
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HyperScreenTheme {
        Greeting("Android")
    }
}

sealed class BottomBarTab(val title: String, val icon: ImageVector, val color: Color) {
    data object Profile : BottomBarTab(
        title = "我的",
        icon = Icons.Rounded.Person,
        color = Color(0xFFFFA574)
    )

    data object Home : BottomBarTab(
        title = "首页",
        icon = Icons.Rounded.Home,
        color = Color(0xFFFA6FFF)
    )

    data object Settings : BottomBarTab(
        title = "设置",
        icon = Icons.Rounded.Settings,
        color = Color(0xFFADFF64)
    )
}

val tabs = listOf(
    BottomBarTab.Profile,
    BottomBarTab.Home,
    BottomBarTab.Settings,
)

@Composable
fun BottomBarTabs(
    tabs: List<BottomBarTab>,
    selectedTab: Int,
    onTabSelected: (BottomBarTab) -> Unit,
) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        ),
        LocalContentColor provides Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            for (tab in tabs) {
                val alpha by animateFloatAsState(
                    targetValue = if (selectedTab == tabs.indexOf(tab)) 1f else .35f,
                    label = "alpha"
                )
                val scale by animateFloatAsState(
                    targetValue = if (selectedTab == tabs.indexOf(tab)) 1f else .97f,
                    visibilityThreshold = .000001f,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessLow,
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                    ),
                    label = "scale"
                )
                Column(
                    modifier = Modifier
                        .scale(scale)
                        .alpha(alpha)
                        .fillMaxHeight()
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onTabSelected(tab)
                            }
                        },
//                        .background(tab.color)
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(imageVector = tab.icon, contentDescription = "tab ${tab.title}")
                    Text(text = tab.title)
                }
            }
        }
    }
}

@Composable
fun GlassMorphicBottomNavigation(hazeState: HazeState, modifier: Modifier = Modifier) {
    var selectedTabIndex by remember { mutableIntStateOf(1) }

    val animatedSelectedTabIndex by animateFloatAsState(
        targetValue = selectedTabIndex.toFloat(),
        label = "animatedSelectedTabIndex",
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioLowBouncy,
        )
    )

    val animatedColor by animateColorAsState(
        targetValue = tabs[selectedTabIndex].color,
        label = "animatedColor",
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
        )
    )

    Box(
        modifier = Modifier
            .padding(vertical = 24.dp, horizontal = 64.dp)
            .fillMaxWidth()
            .height(64.dp)
            .hazeChild(hazeState, shape = CircleShape)
            .border(
                width = Dp.Hairline,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = .8f),
                        Color.White.copy(alpha = .2f),
                    ),
                ),
                shape = CircleShape
            )
    ) {
        BottomBarTabs(
            tabs,
            selectedTab = selectedTabIndex,
            onTabSelected = {
                selectedTabIndex = tabs.indexOf(it)
            }
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .blur(
                    50.dp,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded
                )
        ) {
            val tabWidth = size.width / tabs.size
            drawCircle(
                color = animatedColor.copy(alpha = .6f),
                radius = size.height / 2,
                center = Offset(
                    (tabWidth * animatedSelectedTabIndex) + tabWidth / 2,
                    size.height / 2
                )
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        ) {
            val path = Path().apply {
                addRoundRect(RoundRect(size.toRect(), CornerRadius(size.height)))
            }
            val length = PathMeasure().apply { setPath(path, false) }.length

            Log.d(TAG, "pathLength:  $length ")

            val tabWidth = size.width / tabs.size
            Log.d(TAG, "tabWidth:  $tabWidth ")

            drawPath(
                path,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        animatedColor.copy(alpha = 0f),
                        animatedColor.copy(alpha = 1f),
                        animatedColor.copy(alpha = 1f),
                        animatedColor.copy(alpha = 0f),
                    ),
                    startX = tabWidth * animatedSelectedTabIndex,
                    endX = tabWidth * (animatedSelectedTabIndex + 1),
                ),
                style = Stroke(
                    width = 6f,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(length / 2, length)
//                        intervals = floatArrayOf(100f, 5f)
                    )
                )
            )
        }
    }
}

@Composable
fun LinePath(modifier: Modifier = Modifier) {

    Canvas(
        modifier = modifier
//            .clip(CircleShape)
    ) {
        val path = Path().apply {
            addRoundRect(RoundRect(size.toRect(), CornerRadius(size.height)))
        }
        val length = PathMeasure().apply { setPath(path, false) }.length

        Log.d(TAG, "pathLength:  $length ")

        val tabWidth = size.width / tabs.size
        Log.d(TAG, "tabWidth:  $tabWidth ")


        drawPath(
            path,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Yellow.copy(alpha = 0f),
                    Color.Yellow.copy(alpha = 1f),
                    Color.Yellow.copy(alpha = 1f),
                    Color.Yellow.copy(alpha = 0f),
                ),
//                colors = listOf(
//                    Color.Red.copy(alpha = 1f),
//                    Color.Yellow.copy(alpha = 1f),
//                    Color.Green.copy(alpha = 1f),
//                    Color.Blue.copy(alpha = 1f),
//                ),
                startX = tabWidth * 1,
                endX = tabWidth * (1 + 1),
            ),
            style = Stroke(
                width = 3f,
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(length / 2, length / 2)
//                    intervals = floatArrayOf(tabWidth, length)
//                        intervals = floatArrayOf(100f, 10f)
                )
            )
        )
    }


}