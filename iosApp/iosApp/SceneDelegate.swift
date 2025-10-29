import SwiftUI
import UIKit
import Shared

final class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }

        let backgroundColor = UIColor.systemBackground

        let host = UIHostingController(rootView: LiquidRoot().ignoresSafeArea(.all))
        host.view.isOpaque = true
        host.view.backgroundColor = backgroundColor

        let window = UIWindow(windowScene: windowScene)
        window.isOpaque = true
        window.backgroundColor = backgroundColor
        window.rootViewController = host
        window.makeKeyAndVisible()

        self.window = window
    }
}
