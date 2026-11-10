/**
 * 宿主 NAPI 注入点：放大镜像素经 KN MagnifierBlitStore → 宿主 so 的 getMagnifierBlit。
 * HAR 不绑定具体 libentry.so 模块名。
 */
export interface MagnifierBlitNative {
  generation: number;
  width: number;
  height: number;
  hasPixels: boolean;
  pixels: ArrayBuffer | null;
  borderWidth: number;
  borderColor: number;
  showCross: boolean;
  crossColor: number;
  crossLineWidth: number;
  crossLineLength: number;
  showCropLineMirror: boolean;
  cropLineColor: number;
  cropLineWidth: number;
  touchViewX: number;
  touchViewY: number;
  viewPointsXy: number[];
}

export type MagnifierBlitGetter = () => MagnifierBlitNative;

export class PerspectiveCropOhosNative {
  private static blitGetter: MagnifierBlitGetter | null = null;

  static configure(getter: MagnifierBlitGetter): void {
    PerspectiveCropOhosNative.blitGetter = getter;
  }

  static getMagnifierBlitOrNull(): MagnifierBlitNative | null {
    const g = PerspectiveCropOhosNative.blitGetter;
    if (g == null) {
      return null;
    }
    try {
      return g();
    } catch (_e) {
      return null;
    }
  }
}
