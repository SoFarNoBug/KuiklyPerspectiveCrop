import {
  KRAny,
  KuiklyRenderBaseView,
  KuiklyRenderCallback
} from '@kuikly-open/render';
import { ComponentContent } from '@kit.ArkUI';
import { image } from '@kit.ImageKit';
import { PerspectiveCropOhosNative } from './PerspectiveCropOhosNative';

const PROP_ACTIVE = 'magActive';
const PROP_CX = 'magCx';
const PROP_CY = 'magCy';
const PROP_RADIUS = 'magRadius';
const PROP_GENERATION = 'magGeneration';

function toNumber(v: KRAny): number {
  if (typeof v === 'number') {
    return v;
  }
  if (typeof v === 'string') {
    const n = Number(v);
    return Number.isFinite(n) ? n : 0;
  }
  if (typeof v === 'boolean') {
    return v ? 1 : 0;
  }
  return 0;
}

function argbToCss(c: number): string {
  const a = (c >>> 24) & 0xff;
  const r = (c >>> 16) & 0xff;
  const g = (c >>> 8) & 0xff;
  const b = c & 0xff;
  return `rgba(${r},${g},${b},${a / 255})`;
}

@Observed
export class KRPerspectiveCropMagnifierView extends KuiklyRenderBaseView {
  static readonly VIEW_NAME = 'KRPerspectiveCropMagnifier';

  magActive: boolean = false;
  magCx: number = 0;
  magCy: number = 0;
  magRadius: number = 0;
  magGeneration: number = -1;

  context: CanvasRenderingContext2D | null = null;
  private pixelMap: image.PixelMap | null = null;
  private lastPixelGen: number = -1;

  borderWidth: number = 2;
  borderColor: number = 0xFFFFFFFF;
  showCross: boolean = false;
  crossColor: number = 0xFFFFFFFF;
  crossLineWidth: number = 3;
  crossLineLength: number = 15;
  showCropLineMirror: boolean = true;
  cropLineColor: number = 0xFFFF3B30;
  cropLineWidth: number = 2;
  touchViewX: number = 0;
  touchViewY: number = 0;
  viewPointsXy: number[] = [];
  hasPixels: boolean = false;

  createArkUIView(): ComponentContent<KuiklyRenderBaseView> {
    const uiContext = this.getUIContext() as UIContext;
    return new ComponentContent<KuiklyRenderBaseView>(
      uiContext,
      wrapBuilder<[KuiklyRenderBaseView]>(createPerspectiveCropMagnifier),
      this
    );
  }

  setProp(propKey: string, propValue: KRAny | KuiklyRenderCallback): boolean {
    switch (propKey) {
      case PROP_ACTIVE:
        this.magActive = toNumber(propValue as KRAny) !== 0;
        this.paint();
        return true;
      case PROP_CX:
        this.magCx = toNumber(propValue as KRAny);
        this.paint();
        return true;
      case PROP_CY:
        this.magCy = toNumber(propValue as KRAny);
        this.paint();
        return true;
      case PROP_RADIUS:
        this.magRadius = toNumber(propValue as KRAny);
        this.paint();
        return true;
      case PROP_GENERATION: {
        this.magGeneration = toNumber(propValue as KRAny);
        this.pullBlit();
        this.paint();
        return true;
      }
      default:
        return super.setProp(propKey, propValue);
    }
  }

  call(method: string, params: KRAny, callback: KuiklyRenderCallback | null): KRAny {
    return null;
  }

  private pullBlit(): void {
    try {
      const blit = PerspectiveCropOhosNative.getMagnifierBlitOrNull();
      if (blit == null) {
        this.hasPixels = false;
        this.releasePixelMap();
        return;
      }
      this.borderWidth = blit.borderWidth;
      this.borderColor = blit.borderColor;
      this.showCross = blit.showCross;
      this.crossColor = blit.crossColor;
      this.crossLineWidth = blit.crossLineWidth;
      this.crossLineLength = blit.crossLineLength;
      this.showCropLineMirror = blit.showCropLineMirror;
      this.cropLineColor = blit.cropLineColor;
      this.cropLineWidth = blit.cropLineWidth;
      this.touchViewX = blit.touchViewX;
      this.touchViewY = blit.touchViewY;
      this.viewPointsXy = blit.viewPointsXy ?? [];
      this.hasPixels = blit.hasPixels && blit.pixels != null && blit.width > 0 && blit.height > 0;

      if (!this.hasPixels || blit.pixels == null) {
        this.releasePixelMap();
        this.lastPixelGen = blit.generation;
        return;
      }
      if (this.lastPixelGen === blit.generation && this.pixelMap != null) {
        return;
      }
      this.releasePixelMap();
      const opts: image.InitializationOptions = {
        size: { width: blit.width, height: blit.height },
        pixelFormat: image.PixelMapFormat.RGBA_8888,
        editable: false
      };
      // sync：避免拖动帧与 PixelMap 错位
      this.pixelMap = image.createPixelMapSync(blit.pixels, opts);
      this.lastPixelGen = blit.generation;
    } catch (_e) {
      this.hasPixels = false;
      this.releasePixelMap();
    }
  }

  private releasePixelMap(): void {
    if (this.pixelMap != null) {
      try {
        this.pixelMap.release();
      } catch (_e) {
        // ignore
      }
      this.pixelMap = null;
    }
  }

  /** API 18+：用 UIContext 绑定的 px2vp，避免全局弃用 API。 */
  private pxToVp(px: number): number {
    const ui = this.getUIContext();
    if (ui == null) {
      return 0;
    }
    return ui.px2vp(px);
  }

  paint(): void {
    const ctx = this.context;
    if (ctx == null) {
      return;
    }
    const w = ctx.width;
    const h = ctx.height;
    ctx.clearRect(0, 0, w, h);
    if (!this.magActive || this.magRadius <= 0) {
      return;
    }
    // Kuikly Compose：坐标 = vp × density（≈物理 px）；Canvas 默认单位 vp
    const cx = this.pxToVp(this.magCx);
    const cy = this.pxToVp(this.magCy);
    const radius = this.pxToVp(this.magRadius);
    const diam = radius * 2;
    const borderW = this.pxToVp(this.borderWidth);
    const cropLineW = this.pxToVp(this.cropLineWidth);
    const crossLW = this.pxToVp(this.crossLineWidth);
    const crossLen = this.pxToVp(this.crossLineLength);
    const touchX = this.pxToVp(this.touchViewX);
    const touchY = this.pxToVp(this.touchViewY);

    ctx.save();
    ctx.beginPath();
    ctx.arc(cx, cy, radius, 0, Math.PI * 2);
    ctx.closePath();
    ctx.clip();

    if (this.hasPixels && this.pixelMap != null) {
      ctx.drawImage(this.pixelMap, cx - radius, cy - radius, diam, diam);
    } else {
      ctx.fillStyle = '#222222';
      ctx.fillRect(cx - radius, cy - radius, diam, diam);
    }

    if (this.showCropLineMirror && this.viewPointsXy.length >= 8) {
      const dx = cx - touchX;
      const dy = cy - touchY;
      ctx.strokeStyle = argbToCss(this.cropLineColor);
      ctx.lineWidth = cropLineW;
      ctx.beginPath();
      for (let i = 0; i < 4; i++) {
        const i0 = i * 2;
        const i1 = ((i + 1) % 4) * 2;
        const x0 = this.pxToVp(this.viewPointsXy[i0]) + dx;
        const y0 = this.pxToVp(this.viewPointsXy[i0 + 1]) + dy;
        const x1 = this.pxToVp(this.viewPointsXy[i1]) + dx;
        const y1 = this.pxToVp(this.viewPointsXy[i1 + 1]) + dy;
        if (i === 0) {
          ctx.moveTo(x0, y0);
        }
        ctx.lineTo(x1, y1);
      }
      ctx.closePath();
      ctx.stroke();
    }
    ctx.restore();

    ctx.beginPath();
    ctx.arc(cx, cy, radius, 0, Math.PI * 2);
    ctx.strokeStyle = argbToCss(this.borderColor);
    ctx.lineWidth = borderW;
    ctx.stroke();

    if (this.showCross) {
      const half = crossLen / 2;
      ctx.strokeStyle = argbToCss(this.crossColor);
      ctx.lineWidth = crossLW;
      ctx.beginPath();
      ctx.moveTo(cx - half, cy);
      ctx.lineTo(cx + half, cy);
      ctx.moveTo(cx, cy - half);
      ctx.lineTo(cx, cy + half);
      ctx.stroke();
    }
  }
}

@Component
struct KRPerspectiveCropMagnifier {
  @ObjectLink renderView: KRPerspectiveCropMagnifierView
  private settings: RenderingContextSettings = new RenderingContextSettings(true)
  private context: CanvasRenderingContext2D = new CanvasRenderingContext2D(this.settings)

  build() {
    Column() {
      Canvas(this.context)
        .width('100%')
        .height('100%')
        .hitTestBehavior(HitTestMode.None)
        .backgroundColor(Color.Transparent)
        .onReady(() => {
          this.renderView.context = this.context
          this.renderView.paint()
        })
    }
    .width('100%')
    .height('100%')
  }
}

@Builder
function createPerspectiveCropMagnifier(view: KuiklyRenderBaseView) {
  KRPerspectiveCropMagnifier({ renderView: view as KRPerspectiveCropMagnifierView })
}
