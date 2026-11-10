import { KuiklyNativeManager } from '@kuikly-open/render';
import { PerspectiveCropOhosNative } from '@jlj/kuikly-perspective-crop-ohos';
import Napi from 'libentry.so';
import { ResfileHolder } from './ResfileHolder';

class MyNativeManager extends KuiklyNativeManager {
  protected loadNative(): number {
    // so 已加载：注入 resfile + cache 供解码 / cropToFile；放大镜 blit 给 HAR 壳
    if (ResfileHolder.dir.length > 0 || ResfileHolder.cacheDir.length > 0) {
      Napi.setPerspectiveCropOhosDirs(ResfileHolder.dir, ResfileHolder.cacheDir);
    }
    PerspectiveCropOhosNative.configure(() => Napi.getMagnifierBlit());
    return Napi.initKuikly();
  }
}

// 导出一个全局的 KuiklyNativeManager 实例给 Kuikly 页面共用
const globalNativeManager = new MyNativeManager();

export default globalNativeManager;
