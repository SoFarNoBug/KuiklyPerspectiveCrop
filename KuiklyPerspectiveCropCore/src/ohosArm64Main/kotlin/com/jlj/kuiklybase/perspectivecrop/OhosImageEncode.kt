package com.jlj.kuiklybase.perspectivecrop

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import ohos.image.compress2
import ohos.image.compressBound
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

/**
 * ARGB → PNG 落盘（不走 ImagePacker Native）。
 * zlib level=1 + 预分配 ByteArray，避免逐字节 ArrayList（大图可从十几秒降到亚秒级）。
 *
 * @return 实际写入的绝对路径
 */
@OptIn(ExperimentalForeignApi::class)
internal fun encodeArgbToFile(
    argb: IntArray,
    width: Int,
    height: Int,
    absolutePath: String,
    format: CropOutputFormat,
    jpegQuality: Int,
): String {
    if (width <= 0 || height <= 0 || argb.size < width * height) {
        throw CropException(CropException.WRITE_FAILED, "invalid pixel buffer ${width}x$height")
    }
    @Suppress("UNUSED_VARIABLE")
    val ignoredQuality = jpegQuality

    val pngPath = pngOutputPath(absolutePath, format)
    val bytes = encodeArgbToPng(argb, width, height)
    writeBytesToFile(pngPath, bytes)
    return pngPath
}

private fun pngOutputPath(absolutePath: String, format: CropOutputFormat): String {
    if (absolutePath.endsWith(".png", ignoreCase = true)) return absolutePath
    val stripped = absolutePath.replace(Regex("\\.(jpg|jpeg)$", RegexOption.IGNORE_CASE), "")
    return when (format) {
        CropOutputFormat.Png, CropOutputFormat.Jpeg -> "$stripped.png"
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun writeBytesToFile(absolutePath: String, bytes: ByteArray) {
    val file = fopen(absolutePath, "wb")
        ?: throw CropException(CropException.WRITE_FAILED, "fopen failed path=$absolutePath")
    try {
        bytes.usePinned { pinned ->
            val written = fwrite(pinned.addressOf(0), 1u, bytes.size.toULong(), file)
            if (written.toInt() != bytes.size) {
                throw CropException(
                    CropException.WRITE_FAILED,
                    "fwrite short write ${written.toInt()}/${bytes.size}",
                )
            }
        }
    } finally {
        fclose(file)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun encodeArgbToPng(argb: IntArray, width: Int, height: Int): ByteArray {
    val stride = width * 4 + 1
    val raw = ByteArray(stride * height)
    var o = 0
    var i = 0
    for (y in 0 until height) {
        raw[o++] = 0
        for (x in 0 until width) {
            val c = argb[i++]
            raw[o++] = ((c ushr 16) and 0xFF).toByte()
            raw[o++] = ((c ushr 8) and 0xFF).toByte()
            raw[o++] = (c and 0xFF).toByte()
            raw[o++] = ((c ushr 24) and 0xFF).toByte()
        }
    }
    val zlibData = zlibCompressFast(raw)
    val ihdr = ByteArray(13)
    putInt32Be(ihdr, 0, width)
    putInt32Be(ihdr, 4, height)
    ihdr[8] = 8
    ihdr[9] = 6

    // sig(8) + IHDR(12+13) + IDAT(12+zlib) + IEND(12)
    val out = ByteArray(8 + 25 + 12 + zlibData.size + 12)
    var p = 0
    val sig = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
    )
    sig.copyInto(out, p); p += 8
    p = writeChunk(out, p, "IHDR", ihdr)
    p = writeChunk(out, p, "IDAT", zlibData)
    p = writeChunk(out, p, "IEND", ByteArray(0))
    return if (p == out.size) out else out.copyOf(p)
}

@OptIn(ExperimentalForeignApi::class)
private fun zlibCompressFast(raw: ByteArray): ByteArray = memScoped {
    val bound = compressBound(raw.size.toULong()).toInt().coerceAtLeast(64)
    val dest = ByteArray(bound)
    val destLen = alloc<ULongVar>()
    destLen.value = bound.toULong()
    val rc = raw.usePinned { srcPinned ->
        dest.usePinned { dstPinned ->
            compress2(
                dstPinned.addressOf(0).reinterpret(),
                destLen.ptr,
                srcPinned.addressOf(0).reinterpret(),
                raw.size.toULong(),
                1, // Z_BEST_SPEED
            )
        }
    }
    if (rc != 0) {
        throw CropException(CropException.WRITE_FAILED, "zlib compress2 failed rc=$rc")
    }
    dest.copyOf(destLen.value.toInt())
}

private fun writeChunk(out: ByteArray, at: Int, type: String, data: ByteArray): Int {
    var p = at
    putInt32Be(out, p, data.size); p += 4
    val typeBytes = type.encodeToByteArray()
    typeBytes.copyInto(out, p); p += 4
    data.copyInto(out, p); p += data.size
    val crcBuf = ByteArray(4 + data.size)
    typeBytes.copyInto(crcBuf, 0)
    data.copyInto(crcBuf, 4)
    putInt32Be(out, p, pngCrc32(crcBuf)); p += 4
    return p
}

private fun putInt32Be(buf: ByteArray, at: Int, v: Int) {
    buf[at] = ((v ushr 24) and 0xFF).toByte()
    buf[at + 1] = ((v ushr 16) and 0xFF).toByte()
    buf[at + 2] = ((v ushr 8) and 0xFF).toByte()
    buf[at + 3] = (v and 0xFF).toByte()
}

private fun pngCrc32(data: ByteArray): Int {
    var c = 0xFFFFFFFF.toInt()
    for (byte in data) {
        c = pngCrcTable[(c xor (byte.toInt() and 0xFF)) and 0xFF] xor (c ushr 8)
    }
    return c.inv()
}

private val pngCrcTable: IntArray = IntArray(256) { n ->
    var c = n
    repeat(8) {
        c = if ((c and 1) != 0) {
            0xEDB88320.toInt() xor (c ushr 1)
        } else {
            c ushr 1
        }
    }
    c
}
