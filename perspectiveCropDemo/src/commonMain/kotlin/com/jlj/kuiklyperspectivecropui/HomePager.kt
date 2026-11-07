package com.jlj.kuiklyperspectivecropui

import androidx.compose.runtime.Composable
import com.jlj.kuiklyperspectivecropui.base.BasePager
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.platform.LocalActivity
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.pager.Pager

/** 应用入口主页。三端宿主按 pageName "home" 启动。 */
@Page("home", supportInLocal = true)
internal class HomePager : BasePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            HomeScreen()
        }
    }
}

@Composable
private fun HomeScreen() {
    val localPager = LocalActivity.current.getPager() as Pager
    val statusBarHeight = LocalActivity.current.pageData.statusBarHeight

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DemoTheme.Background)
            .padding(top = (statusBarHeight + 24).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "透视裁剪",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DemoTheme.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "KuiklyPerspectiveCrop Demo",
            fontSize = 15.sp,
            color = DemoTheme.TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(40.dp))

        HomeButton(
            label = "最小实现演示",
            onClick = { openPage(localPager, "perspective_crop_demo") },
        )
        Spacer(modifier = Modifier.height(16.dp))
        HomeButton(
            label = "全能全配置 Lab",
            onClick = { openPage(localPager, "perspective_crop_lab") },
        )
    }
}

@Composable
private fun HomeButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DemoTheme.Primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 40.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DemoTheme.TextPrimary,
        )
    }
}

private fun openPage(pager: Pager, pageName: String) {
    pager.acquireModule<RouterModule>(RouterModule.MODULE_NAME)
        .openPage(pageName, JSONObject())
}
