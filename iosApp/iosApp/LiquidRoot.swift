import SwiftUI
import ComposeApp

struct LiquidRoot: View {
    @AppStorage("dark_theme") private var darkThemeEnabled: Bool = true
    @State private var selection: Tab = .notes

    private var themeHostIdentifier: String {
        darkThemeEnabled ? "theme-dark" : "theme-light"
    }

    var body: some View {
        TabView(selection: $selection) {
            ComposeHost { MainViewControllerKt.makeNotesViewController() }
            .id(themeHostIdentifier)
            .ignoresSafeArea(edges: .vertical)
            .tabItem { Label("Notes", systemImage: "note.text") }
            .tag(Tab.notes)

            ComposeHost { MainViewControllerKt.makeFoldersViewController() }
            .id(themeHostIdentifier)
            .ignoresSafeArea(edges: .vertical)
            .tabItem { Label("Folders", systemImage: "folder") }
            .tag(Tab.folders)

            ComposeHost { MainViewControllerKt.makeSettingsViewController() }
            .id(themeHostIdentifier)
            .ignoresSafeArea(edges: .vertical)
            .tabItem { Label("Settings", systemImage: "gearshape") }
            .tag(Tab.settings)
        }
    }

    private enum Tab: Hashable {
        case notes, folders, settings
    }
}

private struct ComposeHost: UIViewControllerRepresentable {
    let controllerFactory: () -> UIViewController

    func makeUIViewController(context: Context) -> UIViewController {
        controllerFactory()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
