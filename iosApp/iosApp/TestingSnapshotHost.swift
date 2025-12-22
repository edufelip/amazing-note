import UIKit
import ComposeApp

enum SnapshotScreen: String {
    case login
    case notes
    case noteDetail
    case folders
    case settings
}

func makeSnapshotController(screen: SnapshotScreen, style: UIUserInterfaceStyle) -> UIViewController {
    let route: AppRoutes
    switch screen {
    case .login:
        route = AppRoutesLogin()
    case .notes:
        route = AppRoutesNotes()
    case .noteDetail:
        route = AppRoutesNoteDetail(id: nil, folderId: nil)
    case .folders:
        route = AppRoutesFolders()
    case .settings:
        route = AppRoutesSettings()
    }

    let controller = MainViewControllerKt.createAmazingNoteViewController(
        initialRoute: route,
        showBottomBar: false,
        tabBarVisibility: nil,
        onRouteChanged: nil
    )
    controller.overrideUserInterfaceStyle = style
    return controller
}
