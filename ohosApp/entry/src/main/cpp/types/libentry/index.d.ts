export const initKuikly: () => number;
export const setPerspectiveCropResfileDir: (dir: string) => void;
export const setPerspectiveCropOhosDirs: (resfileDir: string, cacheDir: string) => void;

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

export const getMagnifierBlit: () => MagnifierBlitNative;
