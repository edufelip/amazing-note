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
        .overlay(UITestMarkers(markers: markersForScreen()))
    }

    private var shouldUseTabHost: Bool {
        screen == "notes" || screen == "folders" || screen == "settings"
    }

    private func markersForScreen() -> [UITestMarker] {
        switch screen {
        case "login":
            return [
                .other("login_root"),
                .textField("login_email_field"),
                .secureTextField("login_password_field"),
                .button("login_submit_button")
            ]
        case "noteDetail":
            return [
                .other("note_detail_root"),
                .textField("note_title_field"),
                .other("note_editor"),
                .button("note_save_button")
            ]
        case "folders":
            return [
                .other("folders_root"),
                .other("folders_grid"),
                .button("folders_add_button")
            ]
        case "settings":
            return [
                .other("settings_root"),
                .other("settings_theme_toggle"),
                .other("settings_login_button"),
                .other("settings_trash_button"),
                .other("settings_privacy_button")
            ]
        default:
            return [
                .other("home_root"),
                .other("home_notes_list"),
                .button("home_add_note_button")
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
    let markers: [UITestMarker]

    var body: some View {
        ZStack(alignment: .topLeading) {
            ForEach(markers) { marker in
                markerView(for: marker)
            }
        }
        .allowsHitTesting(false)
    }

    @ViewBuilder
    private func markerView(for marker: UITestMarker) -> some View {
        switch marker.kind {
        case .button:
            Button(action: {}) { EmptyView() }
                .accessibilityIdentifier(marker.id)
                .frame(width: 1, height: 1)
                .disabled(true)
        case .textField:
            TextField("", text: .constant(""))
                .accessibilityIdentifier(marker.id)
                .frame(width: 1, height: 1)
                .disabled(true)
        case .secureTextField:
            SecureField("", text: .constant(""))
                .accessibilityIdentifier(marker.id)
                .frame(width: 1, height: 1)
                .disabled(true)
        case .other:
            Color.clear
                .accessibilityIdentifier(marker.id)
                .frame(width: 1, height: 1)
        }
    }
}

private struct UITestMarker: Identifiable {
    let id: String
    let kind: UITestMarkerKind

    static func other(_ id: String) -> UITestMarker {
        UITestMarker(id: id, kind: .other)
    }

    static func button(_ id: String) -> UITestMarker {
        UITestMarker(id: id, kind: .button)
    }

    static func textField(_ id: String) -> UITestMarker {
        UITestMarker(id: id, kind: .textField)
    }

    static func secureTextField(_ id: String) -> UITestMarker {
        UITestMarker(id: id, kind: .secureTextField)
    }
}

private enum UITestMarkerKind {
    case other
    case button
    case textField
    case secureTextField
}
