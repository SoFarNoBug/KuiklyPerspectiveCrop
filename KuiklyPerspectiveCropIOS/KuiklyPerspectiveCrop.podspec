Pod::Spec.new do |spec|
  spec.name         = 'KuiklyPerspectiveCrop'
  spec.version      = '1.0.0'
  spec.summary      = 'Kuikly PerspectiveCrop iOS native magnifier view (KRPerspectiveCropMagnifier)'
  spec.description  = '斜拍透视裁剪库的 iOS 原生放大镜 View。须同时接入 Maven Core（KMP）；宿主 KN framework 须 export Core 桥 PerspectiveCropIosMagnifierBridge。'
  spec.homepage     = 'https://github.com/SoFarNoBug/KuiklyPerspectiveCrop'
  spec.license      = { :type => 'MIT', :file => '../LICENSE' }
  spec.author       = { 'jlj' => '1396275290@qq.com' }
  spec.source       = { :git => 'https://github.com/SoFarNoBug/KuiklyPerspectiveCrop.git', :tag => spec.version.to_s }
  spec.source_files = 'KRPerspectiveCropMagnifier.swift'
  spec.requires_arc = true
  spec.platform     = :ios, '15.0'
  spec.frameworks   = 'UIKit'
  spec.dependency 'OpenKuiklyIOSRender'
  spec.swift_version = '5.0'
end
