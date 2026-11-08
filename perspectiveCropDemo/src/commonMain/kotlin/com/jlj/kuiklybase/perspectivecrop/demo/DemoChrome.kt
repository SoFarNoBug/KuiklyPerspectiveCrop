package com.jlj.kuiklybase.perspectivecrop.demo

import androidx.compose.runtime.Composable
import com.jlj.kuiklyperspectivecropui.DemoTheme
import com.jlj.kuiklyperspectivecropui.Modal
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Slider
import com.tencent.kuikly.compose.material3.SliderDefaults
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.platform.LocalActivity
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import kotlin.math.roundToInt

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: Demo 共用顶栏 / 底栏 / Toggle / 裁切结果与失败浮窗。
 */

internal object DemoCopy {
    const val BACK = "←"
    const val TITLE_DEMO = "透视裁剪"
    const val TITLE_LAB = "全能配置"
    const val RESET = "重置"
    const val ROTATE = "旋转"
    const val CROP = "确认裁切"
    const val CROP_SHORT = "裁切"
    const val CROP_BUSY = "…"
    const val INJECT_POINTS = "注入点"
    const val RECT_ON = "矩形模式"
    const val RECT_OFF = "自由四边形模式"
    const val RESULT_TITLE = "裁切成功"
    const val ERROR_TITLE = "裁切失败"
    const val AGAIN = "再裁一张"
    const val DONE = "完成"
    const val DISMISS = "知道了"
    const val CLOSE = "✕"
    const val TAB_APPEARANCE = "外观"
    const val TAB_GESTURE = "手势"
    const val TAB_MAGNIFIER = "放大镜"
}

@Composable
internal fun DemoTopBar(
    title: String,
    trailing: @Composable (() -> Unit)? = null,
) {
    val pager = LocalActivity.current.getPager() as Pager
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = DemoCopy.BACK,
            color = DemoTheme.TextPrimary,
            fontSize = 20.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable {
                    pager.acquireModule<RouterModule>(RouterModule.MODULE_NAME).closePage()
                }
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
        Text(
            text = title,
            color = DemoTheme.TextPrimary,
            fontSize = 17.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp),
            textAlign = TextAlign.Start,
        )
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
internal fun DemoBarButton(
    label: String,
    primary: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    // H5：次要按钮勿用 Surface（与底栏同色会像「字在漂」）；
    // 竖向 padding 放在 Box 上，勿打在 Text（H5 会把字画在整块高度顶部）
    val bg = when {
        !enabled -> Color(0xFF2A2F38)
        primary -> DemoTheme.Primary
        else -> Color(0xFF2A2F38)
    }
    val fg = if (enabled) DemoTheme.TextPrimary else DemoTheme.TextSecondary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = { if (enabled) onClick() })
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 14.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun DemoBottomBar(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DemoTheme.Surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

/**
 * Lab 用 dp 滑杆（整数档）；[valueRange] 默认 0..100。
 * 样式对齐框架 Compose `MaterialDemo`：压矮轨道 + [SliderDefaults.colors]。
 * 0 = 关闭对应命中（catch 语义）。
 */
@Composable
internal fun DemoDpSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    onValueChange: (Float) -> Unit,
) {
    val lo = valueRange.start
    val hi = valueRange.endInclusive
    val span = (hi - lo).coerceAtLeast(1f)
    val steps = (span.roundToInt() - 1).coerceAtLeast(0)
    val trackInactive = Color(0xFF3A4150)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = DemoTheme.TextSecondary,
                fontSize = 11.sp,
                lineHeight = 11.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${value.roundToInt()} dp",
                color = DemoTheme.TextPrimary,
                fontSize = 11.sp,
                lineHeight = 11.sp,
            )
        }
        // MaterialDemo：Modifier.height(12.dp)；CanvasDemo：colors 换主题色
        Slider(
            value = value.coerceIn(lo, hi),
            onValueChange = { onValueChange(it.roundToInt().toFloat()) },
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = DemoTheme.Primary,
                activeTrackColor = DemoTheme.Primary,
                inactiveTrackColor = trackInactive,
                activeTickColor = DemoTheme.Primary,
                inactiveTickColor = trackInactive,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
        )
    }
}

@Composable
internal fun DemoToggleChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (active) DemoTheme.Primary else Color(0xFF2A2F38)
    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = DemoTheme.TextPrimary,
            fontSize = 12.sp,
            lineHeight = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun DemoTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DemoToggleChip(label = label, active = selected, onClick = onClick)
}

@Composable
internal fun DemoResultModal(
    imageSrc: String,
    onAgain: () -> Unit,
    onDone: () -> Unit,
) {
    DemoModalCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = DemoCopy.RESULT_TITLE,
                color = DemoTheme.TextPrimary,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = DemoCopy.CLOSE,
                color = DemoTheme.TextSecondary,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                modifier = Modifier
                    .clickable(onClick = onDone)
                    .padding(6.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            painter = rememberAsyncImagePainter(model = imageSrc),
            contentDescription = DemoCopy.RESULT_TITLE,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0B0E14)),
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DemoBarButton(
                label = DemoCopy.AGAIN,
                modifier = Modifier.weight(1f),
                onClick = onAgain,
            )
            DemoBarButton(
                label = DemoCopy.DONE,
                primary = true,
                modifier = Modifier.weight(1f),
                onClick = onDone,
            )
        }
    }
}

@Composable
internal fun DemoErrorModal(
    message: String,
    onDismiss: () -> Unit,
) {
    DemoModalCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = DemoCopy.ERROR_TITLE,
                color = DemoTheme.TextPrimary,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = DemoCopy.CLOSE,
                color = DemoTheme.TextSecondary,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                modifier = Modifier
                    .clickable(onClick = onDismiss)
                    .padding(6.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            color = Color(0xFFFF8A80),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        DemoBarButton(
            label = DemoCopy.DISMISS,
            primary = true,
            modifier = Modifier.fillMaxWidth(),
            onClick = onDismiss,
        )
    }
}

@Composable
private fun DemoModalCard(content: @Composable () -> Unit) {
    Modal {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .clickable(onClick = {}),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DemoTheme.Surface)
                    .padding(16.dp),
            ) {
                content()
            }
        }
    }
}
