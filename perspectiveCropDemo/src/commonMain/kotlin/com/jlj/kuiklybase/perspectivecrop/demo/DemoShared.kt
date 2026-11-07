package com.jlj.kuiklybase.perspectivecrop.demo

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: Demo 共用芯片与样例图常量（Camera/Album 对接见 README）。
 */

/** Demo 样例图（commonMain/assets）；Camera/Album 对接见 README（Q12）。 */
internal const val SAMPLE_SRC = "assets://crop_sample.webp"

@Composable
internal fun DemoChip(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = Color.White,
        fontSize = 11.sp,
        modifier = Modifier
            .padding(horizontal = 3.dp, vertical = 2.dp)
            .background(Color(0xFF333333))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
    )
}

@Composable
internal fun DemoStatusLine(text: String) {
    if (text.isEmpty()) return
    Text(
        text = text,
        color = if (text.startsWith("ok")) Color(0xFF4CAF50) else Color(0xFFFF8A80),
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

internal object DemoSkinColors {
    val accent = 0xFF00C853
    val borderAlt = 0xFF2979FF
}
