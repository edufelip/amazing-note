import SwiftUI
import Shared

struct LiquidRoot: View {
    @State private var selection: Tab = .notes

    var body: some View {
        ZStack {
            Color(uiColor: .systemBackground)
                .ignoresSafeArea()

            TabView(selection: $selection) {
                ComposeHost { MainViewControllerKt.makeNotesViewController() }
                    .tabItem { Label("Notes", systemImage: "note.text") }
                    .tag(Tab.notes)

                ComposeHost { MainViewControllerKt.makeFoldersViewController() }
                    .tabItem { Label("Folders", systemImage: "folder") }
                    .tag(Tab.folders)

                ComposeHost { MainViewControllerKt.makeSettingsViewController() }
                    .tabItem { Label("Settings", systemImage: "gearshape") }
                    .tag(Tab.settings)
            }
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
