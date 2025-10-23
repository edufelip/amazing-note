import SwiftUI
import Shared

struct LiquidRoot: View {
    @State private var selection: Tab = .notes

    var body: some View {
        TabView(selection: $selection) {
            ComposeHost { MainViewControllerKt.makeNotesViewController() }
                .ignoresSafeArea(.all)
                .ignoresSafeArea(.keyboard)
                .tabItem { Label("Notes", systemImage: "note.text") }
                .tag(Tab.notes)

            ComposeHost { MainViewControllerKt.makeFoldersViewController() }
                .ignoresSafeArea(.all)
                .ignoresSafeArea(.keyboard)
                .tabItem { Label("Folders", systemImage: "folder") }
                .tag(Tab.folders)

            ComposeHost { MainViewControllerKt.makeSettingsViewController() }
                .ignoresSafeArea(.all)
                .ignoresSafeArea(.keyboard)
                .tabItem { Label("Settings", systemImage: "gearshape") }
                .tag(Tab.settings)
        }
    }

    private enum Tab: Hashable {
        case notes
        case folders
        case settings
    }
}

private struct ComposeHost: UIViewControllerRepresentable {
    let controllerFactory: () -> UIViewController

    func makeUIViewController(context: Context) -> UIViewController {
        controllerFactory()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
