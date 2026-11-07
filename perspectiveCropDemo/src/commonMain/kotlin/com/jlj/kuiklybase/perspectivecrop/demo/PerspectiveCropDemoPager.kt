package com.jlj.kuiklybase.perspectivecrop.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.jlj.kuiklybase.perspectivecrop.CropException
import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropStage
import com.jlj.kuiklybase.perspectivecrop.rememberPerspectiveCropController
import com.jlj.kuiklyperspectivecropui.DemoTheme
import com.jlj.kuiklyperspectivecropui.base.BasePager
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.background
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

/**
 * 最小实现演示（Q30）：assets + Stage + rect/rotate/reset + 确认裁切。
 * Camera/Album → imageSrc 对接见 README（Q12）。
 */
@Page("perspective_crop_demo", supportInLocal = true)
internal class PerspectiveCropDemoPager : BasePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            PerspectiveCropMinimalScreen()
        }
    }
}

@Composable
private fun PerspectiveCropMinimalScreen() {
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
                text = "最小实现",
                color = DemoTheme.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
            )
            DemoChip(
                label = if (rectMode) "rect:ON" else "rect:OFF",
                onClick = {
                    rectMode = !rectMode
                    controller.updateConfig(controller.config.copy(rectMode = rectMode))
                },
            )
            DemoChip(label = "rotate", onClick = { controller.rotateClockwise() })
            DemoChip(label = "reset", onClick = { controller.resetFullImage() })
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
        DemoStatusLine(statusText)
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
                    .height(160.dp)
                    .background(Color(0xFF111111))
                    .padding(8.dp),
            ) {
                Text(text = "结果预览", color = DemoTheme.TextSecondary, fontSize = 12.sp)
                Image(
                    painter = rememberAsyncImagePainter(model = preview),
                    contentDescription = "crop result",
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center,
                )
            }
        }
    }
}
