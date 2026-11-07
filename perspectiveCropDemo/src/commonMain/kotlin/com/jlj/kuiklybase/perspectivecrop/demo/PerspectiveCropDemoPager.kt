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
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.platform.LocalActivity
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.annotations.Page
import kotlinx.coroutines.launch

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: 产品演示页（Q30 最小实现）：顶栏 + Stage + 底栏 + 裁切结果/失败浮窗。
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
    var errorText by remember { mutableStateOf<String?>(null) }
    var cropping by remember { mutableStateOf(false) }

    fun runCrop() {
        if (cropping) return
        cropping = true
        scope.launch {
            try {
                resultSrc = controller.cropToFile()
                errorText = null
            } catch (e: CropException) {
                resultSrc = null
                errorText = "${e.code}: ${e.message}"
            } catch (e: Exception) {
                resultSrc = null
                errorText = "ERROR: ${e.message}"
            } finally {
                cropping = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DemoTheme.Background)
            .padding(top = statusBarHeight.dp),
    ) {
        DemoTopBar(
            title = DemoCopy.TITLE_DEMO,
            trailing = {
                DemoToggleChip(
                    label = if (rectMode) DemoCopy.RECT_ON else DemoCopy.RECT_OFF,
                    active = rectMode,
                    onClick = {
                        rectMode = !rectMode
                        controller.updateConfig(controller.config.copy(rectMode = rectMode))
                    },
                )
            },
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            PerspectiveCropStage(
                modifier = Modifier.fillMaxSize(),
                controller = controller,
                imageSrc = SAMPLE_SRC,
                config = controller.config.copy(rectMode = rectMode),
            )
        }
        DemoBottomBar {
            DemoBarButton(label = DemoCopy.RESET, onClick = { controller.resetFullImage() })
            DemoBarButton(label = DemoCopy.ROTATE, onClick = { controller.rotateClockwise() })
            DemoBarButton(
                label = if (cropping) DemoCopy.CROP_BUSY else DemoCopy.CROP,
                primary = true,
                enabled = !cropping,
                onClick = { runCrop() },
            )
        }
    }

    val preview = resultSrc
    if (preview != null) {
        DemoResultModal(
            imageSrc = preview,
            onAgain = { resultSrc = null },
            onDone = { resultSrc = null },
        )
    }
    val err = errorText
    if (err != null) {
        DemoErrorModal(
            message = err,
            onDismiss = { errorText = null },
        )
    }
}
