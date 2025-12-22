import SwiftUI
import ComposeApp

struct UITestRootView: View {
    private let screen: String
    private let darkMode: Bool

    init() {
        let env = ProcessInfo.processInfo.environment
        self.screen = env["UITEST_SCREEN"] ?? "notes"
        self.darkMode = env["UITEST_DARK"] == "1"
    }

    var body: some View {
        if shouldUseTabHost {
            LiquidRoot(
                forcedColorScheme: darkMode ? .dark : .light,
                initialTabId: screen
            )
        } else {
            ComposeRouteHost(route: resolveRoute())
                .preferredColorScheme(darkMode ? .dark : .light)
        }
    }

    private var shouldUseTabHost: Bool {
        screen == "notes" || screen == "folders" || screen == "settings"
    }

    private func resolveRoute() -> AppRoutes {
        switch screen {
        case "login":
            return AppRoutesLogin()
        case "noteDetail":
            return AppRoutesNoteDetail(id: nil, folderId: nil)
        case "folders":
            return AppRoutesFolders()
        case "settings":
            return AppRoutesSettings()
        default:
            return AppRoutesNotes()
        }
    }
}

private struct ComposeRouteHost: UIViewControllerRepresentable {
    let route: AppRoutes

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.createAmazingNoteViewController(
            initialRoute: route,
            showBottomBar: false,
            tabBarVisibility: nil,
            onRouteChanged: nil
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No-op. We recreate controllers per launch to ensure deterministic tests.
    }
}
