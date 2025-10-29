import SwiftUI
import UIKit
import Shared

private final class TransparentHostingController<Content: View>: UIHostingController<Content> {
    override init(rootView: Content) {
        super.init(rootView: rootView)
        view.isOpaque = false
        view.backgroundColor = .clear
    }

    @MainActor required dynamic init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        view.isOpaque = false
        view.backgroundColor = .clear
    }
}

final class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }

        let window = UIWindow(windowScene: windowScene)
        window.backgroundColor = .clear
        window.rootViewController = TransparentHostingController(rootView: LiquidRoot().ignoresSafeArea(.all))
        window.makeKeyAndVisible()

        self.window = window
    }
}
