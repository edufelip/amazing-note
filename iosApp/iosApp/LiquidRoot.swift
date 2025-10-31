import SwiftUI
import Shared

struct LiquidRoot: View {
    @State private var selection: Tab = .notes

    var body: some View {
        TabView(selection: $selection) {
            ComposeHost { MainViewControllerKt.makeNotesViewController() }
            .ignoresSafeArea(edges: .vertical)
            .tabItem { Label("Notes", systemImage: "note.text") }
            .tag(Tab.notes)

            ComposeHost { MainViewControllerKt.makeFoldersViewController() }
            .ignoresSafeArea(edges: .vertical)
            .tabItem { Label("Folders", systemImage: "folder") }
            .tag(Tab.folders)

            ComposeHost { MainViewControllerKt.makeSettingsViewController() }
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
