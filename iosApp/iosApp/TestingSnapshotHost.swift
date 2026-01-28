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
        route = AppRoutesDetailDestination.Login.shared
    case .notes:
        route = AppRoutesTabDestination.Notes.shared
    case .noteDetail:
        route = AppRoutesDetailDestination.NoteDetail(id: nil, folderId: nil)
    case .folders:
        route = AppRoutesTabDestination.Folders.shared
    case .settings:
        route = AppRoutesTabDestination.Settings.shared
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
