package com.jlj.kuiklybase.perspectivecrop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jlj.kuiklybase.perspectivecrop.overlay.CropOverlayPainters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 透视裁剪控制器：设图、选区、旋转、[crop] / [cropToFile]。
 */
class PerspectiveCropController(
    initialConfig: PerspectiveCropConfig = PerspectiveCropConfig(),
    initialPainters: CropOverlayPainters = CropOverlayPainters(),
) {
    private val _session = CropSession(initialConfig)
    private val _processor = CropProcessor()

    var config: PerspectiveCropConfig by mutableStateOf(initialConfig)
        private set

    var painters: CropOverlayPainters by mutableStateOf(initialPainters)
        private set

    var imageSrc: String by mutableStateOf("")
        private set

    /** 供 Compose 订阅的选区快照。 */
    var points: List<CropPoint> by mutableStateOf(emptyList())
        private set

    /** 当前显示坐标系下的图宽高（UI 旋转后会与 intrinsic 对调）。 */
    var imageWidth: Float by mutableStateOf(0f)
        private set
    var imageHeight: Float by mutableStateOf(0f)
        private set

    /** 解码固有尺寸（不随 UI 旋转改变；底图 graphicsLayer 用）。 */
    var naturalWidth: Float by mutableStateOf(0f)
        private set
    var naturalHeight: Float by mutableStateOf(0f)
        private set

    var uiRotationSteps: Int by mutableStateOf(0)
        private set

    internal fun sessionOrNull(): CropSession = _session

    fun updateConfig(value: PerspectiveCropConfig) {
        config = value
        _session.updateConfig(value)
        publish()
    }

    fun updatePainters(value: CropOverlayPainters) {
        painters = value
    }

    fun applyImageSrc(src: String) {
        if (src == imageSrc) return
        imageSrc = src
        naturalWidth = 0f
        naturalHeight = 0f
        _session.clearImage()
        publish()
    }

    /**
     * 图片固有尺寸就绪时调用（Fit 映射与初始选区依赖此值）。
     * 仅在 intrinsic 变化时写入；避免 UI 旋转后被 painter 回调用原始宽高冲掉。
     */
    fun onImageSizeReady(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        if (naturalWidth == width && naturalHeight == height && points.size == 4) return
        naturalWidth = width
        naturalHeight = height
        _session.setImageSize(width, height, resetSelection = true)
        publish()
    }

    fun readPoints(): List<CropPoint> = points

    fun writePoints(newPoints: List<CropPoint>) {
        _session.setPoints(newPoints)
        publish()
    }

    fun resetFullImage() {
        _session.resetFullImage()
        publish()
    }

    /** UI 旋转 90°：只更新预览与选区状态（像素变换在出图时按 Q17）。 */
    fun rotateClockwise() {
        _session.rotateClockwiseUi()
        publish()
    }

    fun canCrop(): Boolean = _session.canCrop()

    /**
     * 透视出图。非法四点 / 平台 stub / 解码失败等抛 [CropException]（Q16）。
     * 解码与变换在后台执行（Q6 / L2）。
     */
    suspend fun crop(): PlatformImage {
        val src = imageSrc
        if (src.isEmpty()) {
            throw CropException(CropException.EMPTY_IMAGE_SRC, "imageSrc is empty")
        }
        if (!canCrop()) {
            throw CropException(CropException.ILLEGAL_POINTS, "crop points are invalid; check canCrop()")
        }
        val pts = points.toList()
        val steps = uiRotationSteps
        val cfg = config
        return withContext(Dispatchers.IO) {
            _processor.process(src, pts, steps, cfg)
        }
    }

    /** 出图并写入默认缓存路径，返回可再作 [imageSrc] 的路径（Q18）。 */
    suspend fun cropToFile(): String {
        val image = crop()
        return withContext(Dispatchers.IO) {
            image.saveToFile()
        }
    }

    /** 出图并写入 [path]（Q18）。 */
    suspend fun cropToFile(path: String): String {
        if (path.isEmpty()) {
            throw CropException(CropException.WRITE_FAILED, "output path is empty")
        }
        val image = crop()
        return withContext(Dispatchers.IO) {
            image.saveToFile(path)
        }
    }

    internal fun publish() {
        points = _session.points.toList()
        imageWidth = _session.imageWidth
        imageHeight = _session.imageHeight
        uiRotationSteps = _session.uiRotationSteps
        config = _session.config
    }
}

@Composable
fun rememberPerspectiveCropController(
    config: PerspectiveCropConfig = PerspectiveCropConfig(),
    painters: CropOverlayPainters = CropOverlayPainters(),
): PerspectiveCropController {
    return remember { PerspectiveCropController(config, painters) }
}
