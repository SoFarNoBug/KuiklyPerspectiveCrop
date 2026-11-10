import OpenKuiklyIOSRender
import UIKit

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: iOS 放大镜原生圆镜（Q38）；像素来自 PerspectiveCropIosMagnifierBridge / MagnifierBlitStore（Core，经宿主 KN framework 在运行时导出）。
 *
 * 不 import 宿主 KMP 模块名：经 exact ObjC 名在进程内取桥，CocoaPods 包可独立 lint/发布。
 * 不用 `as? Protocol`：KN 对象不登记 Swift 协议，强转失败会整路空桥（灰底）。改用 method(for:)。
 */
@objc(KRPerspectiveCropMagnifier)
public final class KRPerspectiveCropMagnifier: KRView {

    private var active = false
    private var cx: CGFloat = 0
    private var cy: CGFloat = 0
    private var radius: CGFloat = 0

    public override init(frame: CGRect) {
        super.init(frame: frame)
        isOpaque = false
        backgroundColor = .clear
        isUserInteractionEnabled = false
        contentMode = .redraw
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    @objc public var css_magActive: NSNumber? {
        didSet {
            active = css_magActive?.boolValue ?? false
            setNeedsDisplay()
        }
    }

    @objc public var css_magCx: NSNumber? {
        didSet {
            cx = CGFloat(truncating: css_magCx ?? 0)
            setNeedsDisplay()
        }
    }

    @objc public var css_magCy: NSNumber? {
        didSet {
            cy = CGFloat(truncating: css_magCy ?? 0)
            setNeedsDisplay()
        }
    }

    @objc public var css_magRadius: NSNumber? {
        didSet {
            radius = CGFloat(truncating: css_magRadius ?? 0)
            setNeedsDisplay()
        }
    }

    @objc public var css_magGeneration: NSNumber? {
        didSet {
            setNeedsDisplay()
        }
    }

    public override func draw(_ rect: CGRect) {
        guard active, radius > 0, let ctx = UIGraphicsGetCurrentContext() else { return }
        let dens = max(UIScreen.main.scale, 1)
        let drawCx = pxToPt(cx, density: dens)
        let drawCy = pxToPt(cy, density: dens)
        let drawRadius = pxToPt(radius, density: dens)
        guard drawRadius > 0 else { return }
        let diam = drawRadius * 2
        let circle = CGRect(
            x: drawCx - drawRadius,
            y: drawCy - drawRadius,
            width: diam,
            height: diam
        )
        let bridge = MagBridge.resolve()
        let blitImage = Self.resolveBlitCGImage(bridge: bridge)

        ctx.saveGState()
        ctx.addEllipse(in: circle)
        ctx.clip()
        UIColor(red: 0x22 / 255.0, green: 0x22 / 255.0, blue: 0x22 / 255.0, alpha: 1).setFill()
        ctx.fillEllipse(in: circle)

        if let cgImage = blitImage {
            // 顶左 RGBA；UIImage.draw（勿再对 CGContext 做 Y 翻转）。
            UIImage(cgImage: cgImage).draw(in: circle)
        }

        if let bridge, bridge.showCropLineMirror(), bridge.viewPointCount() >= 8 {
            ctx.saveGState()
            let ox = drawCx - pxToPt(CGFloat(bridge.touchViewX()), density: dens)
            let oy = drawCy - pxToPt(CGFloat(bridge.touchViewY()), density: dens)
            ctx.translateBy(x: ox, y: oy)
            argbColor(bridge.cropLineColorArgb()).setStroke()
            ctx.setLineWidth(pxToPt(CGFloat(bridge.cropLineWidth()), density: dens))
            ctx.setLineCap(.round)
            for i in 0..<4 {
                let i0 = Int32(i * 2)
                let i1 = Int32(((i + 1) % 4) * 2)
                ctx.move(to: CGPoint(
                    x: pxToPt(CGFloat(bridge.viewPointAt(index: i0)), density: dens),
                    y: pxToPt(CGFloat(bridge.viewPointAt(index: i0 + 1)), density: dens)
                ))
                ctx.addLine(to: CGPoint(
                    x: pxToPt(CGFloat(bridge.viewPointAt(index: i1)), density: dens),
                    y: pxToPt(CGFloat(bridge.viewPointAt(index: i1 + 1)), density: dens)
                ))
            }
            ctx.strokePath()
            ctx.restoreGState()
        }
        ctx.restoreGState()

        let borderArgb = bridge?.borderColorArgb() ?? Int32(bitPattern: 0xFFFFFFFF)
        let borderW = bridge.map { pxToPt(CGFloat($0.borderWidth()), density: dens) } ?? 2
        argbColor(borderArgb).setStroke()
        ctx.setLineWidth(borderW)
        ctx.strokeEllipse(in: circle)

        if let bridge, bridge.showCross() {
            let half = pxToPt(CGFloat(bridge.crossLineLength()), density: dens) / 2
            argbColor(bridge.crossColorArgb()).setStroke()
            ctx.setLineWidth(pxToPt(CGFloat(bridge.crossLineWidth()), density: dens))
            ctx.move(to: CGPoint(x: drawCx - half, y: drawCy))
            ctx.addLine(to: CGPoint(x: drawCx + half, y: drawCy))
            ctx.move(to: CGPoint(x: drawCx, y: drawCy - half))
            ctx.addLine(to: CGPoint(x: drawCx, y: drawCy + half))
            ctx.strokePath()
        }
    }

    private struct BlitResolve {
        let image: CGImage?
        let path: String
    }

    private static func resolveBlitCGImage(bridge: MagBridge?) -> BlitResolve {
        guard let bridge else {
            return BlitResolve(image: nil, path: "no-bridge")
        }
        let w = bridge.blitWidth()
        let h = bridge.blitHeight()
        let rgbaNS = bridge.rgbaDataOrNull()
        let pngNS = bridge.pngDataOrNull()

        if w > 0, h > 0, let ns = rgbaNS {
            let data = Data(referencing: ns)
            if data.count >= w * h * 4, let img = cgImageFromRGBA(data: data, width: w, height: h) {
                return BlitResolve(image: img, path: "rgba")
            }
        }
        if let ns = pngNS, let ui = UIImage(data: Data(referencing: ns)), let cg = ui.cgImage {
            return BlitResolve(image: cg, path: "png")
        }
        return BlitResolve(image: nil, path: "empty")
    }

    private static func cgImageFromRGBA(data: Data, width: Int, height: Int) -> CGImage? {
        let colorSpace = CGColorSpaceCreateDeviceRGB()
        let bitmapInfo = CGImageAlphaInfo.premultipliedLast.rawValue | CGBitmapInfo.byteOrder32Big.rawValue
        guard let provider = CGDataProvider(data: data as CFData) else { return nil }
        return CGImage(
            width: width,
            height: height,
            bitsPerComponent: 8,
            bitsPerPixel: 32,
            bytesPerRow: width * 4,
            space: colorSpace,
            bitmapInfo: CGBitmapInfo(rawValue: bitmapInfo),
            provider: provider,
            decode: nil,
            shouldInterpolate: false,
            intent: .defaultIntent
        )
    }

    private func pxToPt(_ px: CGFloat, density: CGFloat) -> CGFloat {
        px / density
    }

    private func argbColor(_ argb: Int32) -> UIColor {
        let a = CGFloat((argb >> 24) & 0xff) / 255
        let r = CGFloat((argb >> 16) & 0xff) / 255
        let g = CGFloat((argb >> 8) & 0xff) / 255
        let b = CGFloat(argb & 0xff) / 255
        return UIColor(red: r, green: g, blue: b, alpha: a)
    }
}

/// KN `PerspectiveCropIosMagnifierBridge` 运行时调用封装（避开 Swift 协议强转）。
private final class MagBridge {
    private let obj: NSObject

    private init(obj: NSObject) {
        self.obj = obj
    }

    static func resolve() -> MagBridge? {
        guard let cls = NSClassFromString("PerspectiveCropIosMagnifierBridge") as? NSObject.Type else {
            return nil
        }
        let sharedSel = NSSelectorFromString("shared")
        guard cls.responds(to: sharedSel),
              let raw = cls.perform(sharedSel)?.takeUnretainedValue() as? NSObject else {
            return nil
        }
        return MagBridge(obj: raw)
    }

    func rgbaDataOrNull() -> NSData? { object("rgbaDataOrNull") as? NSData }
    func pngDataOrNull() -> NSData? { object("pngDataOrNull") as? NSData }

    func blitWidth() -> Int { Int(int32("blitWidth")) }
    func blitHeight() -> Int { Int(int32("blitHeight")) }
    func borderWidth() -> Float { float("borderWidth") }
    func borderColorArgb() -> Int32 { int32("borderColorArgb") }
    func showCross() -> Bool { bool("showCross") }
    func crossColorArgb() -> Int32 { int32("crossColorArgb") }
    func crossLineWidth() -> Float { float("crossLineWidth") }
    func crossLineLength() -> Float { float("crossLineLength") }
    func showCropLineMirror() -> Bool { bool("showCropLineMirror") }
    func cropLineColorArgb() -> Int32 { int32("cropLineColorArgb") }
    func cropLineWidth() -> Float { float("cropLineWidth") }
    func touchViewX() -> Float { float("touchViewX") }
    func touchViewY() -> Float { float("touchViewY") }
    func viewPointCount() -> Int { Int(int32("viewPointCount")) }

    func viewPointAt(index: Int32) -> Float {
        let sel = NSSelectorFromString("viewPointAtIndex:")
        guard obj.responds(to: sel), let m = obj.method(for: sel) else { return 0 }
        typealias Fn = @convention(c) (AnyObject, Selector, Int32) -> Float
        return unsafeBitCast(m, to: Fn.self)(obj, sel, index)
    }

    private func object(_ name: String) -> AnyObject? {
        let sel = NSSelectorFromString(name)
        guard obj.responds(to: sel) else { return nil }
        return obj.perform(sel)?.takeUnretainedValue()
    }

    private func int32(_ name: String) -> Int32 {
        let sel = NSSelectorFromString(name)
        guard obj.responds(to: sel), let m = obj.method(for: sel) else { return 0 }
        typealias Fn = @convention(c) (AnyObject, Selector) -> Int32
        return unsafeBitCast(m, to: Fn.self)(obj, sel)
    }

    private func float(_ name: String) -> Float {
        let sel = NSSelectorFromString(name)
        guard obj.responds(to: sel), let m = obj.method(for: sel) else { return 0 }
        typealias Fn = @convention(c) (AnyObject, Selector) -> Float
        return unsafeBitCast(m, to: Fn.self)(obj, sel)
    }

    private func bool(_ name: String) -> Bool {
        let sel = NSSelectorFromString(name)
        guard obj.responds(to: sel), let m = obj.method(for: sel) else { return false }
        typealias Fn = @convention(c) (AnyObject, Selector) -> Bool
        return unsafeBitCast(m, to: Fn.self)(obj, sel)
    }
}
