package com.jlj.kuiklybase.perspectivecrop

/**
 * 出图引擎（双策略可切换）。
 *
 * - [Auto]：有原生则原生，否则 common CPU
 * - [Native]：端上原生（Android `setPolyToPoly`；鸿蒙本期未提供则明确失败）
 * - [Common]：common CPU 单应 + 双线性（与 H5 同算法）
 */
enum class CropEngine {
    Auto,
    Native,
    Common,
}
