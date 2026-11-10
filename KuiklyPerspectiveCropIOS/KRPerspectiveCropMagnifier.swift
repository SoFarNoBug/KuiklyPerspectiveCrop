import OpenKuiklyIOSRender
import UIKit

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: iOS 放大镜原生圆镜（Q38）；像素来自 PerspectiveCropIosMagnifierBridge / MagnifierBlitStore（Core，经宿主 KN framework 在运行时导出）。
 *
 * 不 import 宿主 KMP 模块名：经 exact ObjC 名在进程内取桥，CocoaPods 包可独立 lint/发布。
 */
@objc protocol PerspectiveCropIosMagnifierBridging {
    func imageOrNull() -> UIImage?
    func borderWidth() -> Float
    func borderColorArgb() -> Int32
    func showCross() -> Bool
    func crossColorArgb() -> Int32
    func crossLineWidth() -> Float
    func crossLineLength() -> Float
    func showCropLineMirror() -> Bool
    func cropLineColorArgb() -> Int32
    func cropLineWidth() -> Float
    func touchViewX() -> Float
    func touchViewY() -> Float
    func viewPointCount() -> Int32
    func viewPointAt(index: Int32) -> Float
}

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
        guard let bridge = Self.resolveBridge() else { return }
        let diam = radius * 2
        let circle = CGRect(x: cx - radius, y: cy - radius, width: diam, height: diam)

        ctx.saveGState()
        ctx.addEllipse(in: circle)
        ctx.clip()
        if let image = bridge.imageOrNull() {
            image.draw(in: circle)
        } else {
            UIColor(red: 0x22 / 255.0, green: 0x22 / 255.0, blue: 0x22 / 255.0, alpha: 1).setFill()
            ctx.fillEllipse(in: circle)
        }

        if bridge.showCropLineMirror() {
            let count = Int(bridge.viewPointCount())
            if count >= 8 {
                ctx.saveGState()
                let ox = cx - CGFloat(bridge.touchViewX())
                let oy = cy - CGFloat(bridge.touchViewY())
                ctx.translateBy(x: ox, y: oy)
                argbColor(bridge.cropLineColorArgb()).setStroke()
                ctx.setLineWidth(CGFloat(bridge.cropLineWidth()))
                ctx.setLineCap(.round)
                for i in 0..<4 {
                    let i0 = Int32(i * 2)
                    let i1 = Int32(((i + 1) % 4) * 2)
                    ctx.move(to: CGPoint(
                        x: CGFloat(bridge.viewPointAt(index: i0)),
                        y: CGFloat(bridge.viewPointAt(index: i0 + 1))
                    ))
                    ctx.addLine(to: CGPoint(
                        x: CGFloat(bridge.viewPointAt(index: i1)),
                        y: CGFloat(bridge.viewPointAt(index: i1 + 1))
                    ))
                }
                ctx.strokePath()
                ctx.restoreGState()
            }
        }
        ctx.restoreGState()

        argbColor(bridge.borderColorArgb()).setStroke()
        ctx.setLineWidth(CGFloat(bridge.borderWidth()))
        ctx.strokeEllipse(in: circle)

        if bridge.showCross() {
            let half = CGFloat(bridge.crossLineLength()) / 2
            argbColor(bridge.crossColorArgb()).setStroke()
            ctx.setLineWidth(CGFloat(bridge.crossLineWidth()))
            ctx.move(to: CGPoint(x: cx - half, y: cy))
            ctx.addLine(to: CGPoint(x: cx + half, y: cy))
            ctx.move(to: CGPoint(x: cx, y: cy - half))
            ctx.addLine(to: CGPoint(x: cx, y: cy + half))
            ctx.strokePath()
        }
    }

    private func argbColor(_ argb: Int32) -> UIColor {
        let a = CGFloat((argb >> 24) & 0xff) / 255
        let r = CGFloat((argb >> 16) & 0xff) / 255
        let g = CGFloat((argb >> 8) & 0xff) / 255
        let b = CGFloat(argb & 0xff) / 255
        return UIColor(red: r, green: g, blue: b, alpha: a)
    }

    /// Core `@ObjCName(exact)` → 进程内类名 `PerspectiveCropIosMagnifierBridge`（宿主须 link 含该桥的 KN framework）。
    private static func resolveBridge() -> PerspectiveCropIosMagnifierBridging? {
        guard let cls = NSClassFromString("PerspectiveCropIosMagnifierBridge") as? NSObject.Type else {
            return nil
        }
        let sharedSel = NSSelectorFromString("shared")
        guard cls.responds(to: sharedSel),
              let obj = cls.perform(sharedSel)?.takeUnretainedValue() else {
            return nil
        }
        return obj as? PerspectiveCropIosMagnifierBridging
    }
}
