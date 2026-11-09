import OpenKuiklyIOSRender
import UIKit
import perspectiveCropDemo

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: iOS 放大镜原生圆镜（Q38）；像素来自 PerspectiveCropIosMagnifierBridge / MagnifierBlitStore。
 */
@objc(KRPerspectiveCropMagnifier)
final class KRPerspectiveCropMagnifier: KRView {

    private var active = false
    private var cx: CGFloat = 0
    private var cy: CGFloat = 0
    private var radius: CGFloat = 0

    override init(frame: CGRect) {
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

    @objc var css_magActive: NSNumber? {
        didSet {
            active = css_magActive?.boolValue ?? false
            setNeedsDisplay()
        }
    }

    @objc var css_magCx: NSNumber? {
        didSet {
            cx = CGFloat(truncating: css_magCx ?? 0)
            setNeedsDisplay()
        }
    }

    @objc var css_magCy: NSNumber? {
        didSet {
            cy = CGFloat(truncating: css_magCy ?? 0)
            setNeedsDisplay()
        }
    }

    @objc var css_magRadius: NSNumber? {
        didSet {
            radius = CGFloat(truncating: css_magRadius ?? 0)
            setNeedsDisplay()
        }
    }

    @objc var css_magGeneration: NSNumber? {
        didSet {
            // generation 仅脏标记；像素仍读 BlitStore
            setNeedsDisplay()
        }
    }

    override func draw(_ rect: CGRect) {
        guard active, radius > 0, let ctx = UIGraphicsGetCurrentContext() else { return }
        let bridge = PerspectiveCropIosMagnifierBridge.shared
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
            let pts = bridge.viewPointsXy()
            let count = Int(pts.size)
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
                    ctx.move(to: CGPoint(x: CGFloat(pts.get(index: i0)), y: CGFloat(pts.get(index: i0 + 1))))
                    ctx.addLine(to: CGPoint(x: CGFloat(pts.get(index: i1)), y: CGFloat(pts.get(index: i1 + 1))))
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
}
