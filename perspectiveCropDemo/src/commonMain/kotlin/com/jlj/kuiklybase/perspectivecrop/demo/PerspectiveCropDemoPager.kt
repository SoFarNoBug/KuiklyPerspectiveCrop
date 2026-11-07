package com.jlj.kuiklybase.perspectivecrop.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.jlj.kuiklybase.perspectivecrop.CropException
import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropConfig
import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropStage
import com.jlj.kuiklybase.perspectivecrop.rememberPerspectiveCropController
import com.jlj.kuiklyperspectivecropui.DemoTheme
import com.jlj.kuiklyperspectivecropui.base.BasePager
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.platform.LocalActivity
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import kotlinx.coroutines.launch

/** Demo 样例图（commonMain/assets）；Camera/Album 对接见 README（Q12）。 */
private const val SAMPLE_SRC = "assets://crop_sample.webp"

/**
 * M2 演示：框选 + 确认裁切；失败展示 CropException code/message（Q22）。
 */
@Page("perspective_crop_demo", supportInLocal = true)
internal class PerspectiveCropDemoPager : BasePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            PerspectiveCropDemoScreen()
        }
    }
}

@Composable
private fun PerspectiveCropDemoScreen() {
    val statusBarHeight = LocalActivity.current.pageData.statusBarHeight
    val controller = rememberPerspectiveCropController()
    val scope = rememberCoroutineScope()
    var rectMode by remember { mutableStateOf(false) }
    var resultSrc by remember { mutableStateOf<String?>(null) }
    var statusText by remember { mutableStateOf("") }
    var cropping by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DemoTheme.Background)
            .padding(top = statusBarHeight.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = "PerspectiveCrop · M2",
                color = DemoTheme.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
            )
            DemoChip(
                label = if (rectMode) "rectMode:ON" else "rectMode:OFF",
                onClick = {
                    rectMode = !rectMode
                    controller.updateConfig(controller.config.copy(rectMode = rectMode))
                },
            )
            DemoChip(
                label = "rotate",
                onClick = { controller.rotateClockwise() },
            )
            DemoChip(
                label = "reset",
                onClick = { controller.resetFullImage() },
            )
            DemoChip(
                label = if (cropping) "…" else "确认裁切",
                onClick = {
                    if (cropping) return@DemoChip
                    cropping = true
                    statusText = ""
                    scope.launch {
                        try {
                            val path = controller.cropToFile()
                            resultSrc = path
                            statusText = "ok · $path"
                        } catch (e: CropException) {
                            resultSrc = null
                            statusText = "${e.code}: ${e.message}"
                        } catch (e: Exception) {
                            resultSrc = null
                            statusText = "ERROR: ${e.message}"
                        } finally {
                            cropping = false
                        }
                    }
                },
            )
        }
        if (statusText.isNotEmpty()) {
            Text(
                text = statusText,
                color = if (statusText == "ok") Color(0xFF4CAF50) else Color(0xFFFF8A80),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            PerspectiveCropStage(
                modifier = Modifier.fillMaxSize(),
                controller = controller,
                imageSrc = SAMPLE_SRC,
                config = controller.config.copy(rectMode = rectMode),
            )
        }
        val preview = resultSrc
        if (preview != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF111111))
                    .padding(8.dp),
            ) {
                Text(
                    text = "结果预览",
                    color = DemoTheme.TextSecondary,
                    fontSize = 12.sp,
                )
                Image(
                    painter = rememberAsyncImagePainter(model = preview),
                    contentDescription = "crop result",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center,
                )
            }
        }
    }
}

@Composable
private fun DemoChip(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = Color.White,
        fontSize = 12.sp,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .background(Color(0xFF333333))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}
