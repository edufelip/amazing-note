import SwiftUI
import UIKit
import ComposeApp

final class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo sessimion: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }
        let bg = UIColor.systemBackground
        let host = UIHostingController(rootView: LiquidRoot())
        host.view.isOpaque = true
        host.view.backgroundColor = UIColor(named: "AppBackground")
        let window = UIWindow(windowScene: windowScene)
        window.isOpaque = true
        window.backgroundColor = bg
        window.rootViewController = host
        self.window = window
        window.makeKeyAndVisible()
    }
}
