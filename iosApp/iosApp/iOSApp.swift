import SwiftUI
import perspectiveCropDemo

@main
struct iOSApp: App {
    init() {
        let caches = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first
            ?? FileManager.default.temporaryDirectory
        PerspectiveCropIosBridge.shared.configure(cacheDir: caches.path)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
