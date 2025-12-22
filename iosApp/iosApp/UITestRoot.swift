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
        Group {
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
        .overlay(UITestMarkers(identifiers: identifiersForScreen()))
    }

    private var shouldUseTabHost: Bool {
        screen == "notes" || screen == "folders" || screen == "settings"
    }

    private func identifiersForScreen() -> [String] {
        switch screen {
        case "login":
            return [
                "login_root",
                "login_email_field",
                "login_password_field",
                "login_submit_button"
            ]
        case "noteDetail":
            return [
                "note_detail_root",
                "note_title_field",
                "note_editor",
                "note_save_button"
            ]
        case "folders":
            return [
                "folders_root",
                "folders_grid",
                "folders_add_button"
            ]
        case "settings":
            return [
                "settings_root",
                "settings_theme_toggle",
                "settings_login_button",
                "settings_trash_button",
                "settings_privacy_button"
            ]
        default:
            return [
                "home_root",
                "home_notes_list",
                "home_add_note_button"
            ]
        }
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

private struct UITestMarkers: View {
    let identifiers: [String]

    var body: some View {
        ZStack(alignment: .topLeading) {
            ForEach(identifiers, id: \.self) { identifier in
                Color.clear
                    .frame(width: 1, height: 1)
                    .accessibilityIdentifier(identifier)
            }
        }
        .allowsHitTesting(false)
    }
}
