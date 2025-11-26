import Foundation
import UIKit
import SwiftUI
import ComposeApp

@MainActor
final class TabBarVisibility: ObservableObject {
    @Published var isVisible: Bool = true
}

@MainActor
struct LiquidRoot: View {
    @AppStorage("dark_theme") private var darkThemeEnabled: Bool = true
    @State private var selection: Tab = .notes
    @StateObject private var tabBar = TabBarVisibility()
    @State private var lastRouteHandled: String?
    @State private var lastShouldShow: Bool?

    private static var lastRouteHandledGlobal: String?
    private static var lastShouldShowGlobal: Bool?

    private var themeHostIdentifier: String {
        darkThemeEnabled ? "theme-dark" : "theme-light"
    }

    var body: some View {
        TabView(selection: $selection) {
            ComposeHost(
                tabBar: tabBar,
                onRouteChanged: { route, isBottomBarVisible in
                    handleRouteChange(route, isBottomBarVisible)
                },
                controllerFactory: { tabBar, routeHandler in
                    MainViewControllerKt.makeNotesViewController(
                        tabBarVisibility: nil, // avoid double-driving visibility; use route-based visibility instead
                        onRouteChanged: routeHandler
                    )
                }
            )
            .id(themeHostIdentifier)
            .ignoresSafeArea(edges: .vertical)
            .tabItem { Label("Notes", systemImage: "note.text") }
            .tag(Tab.notes)

            ComposeHost(
                tabBar: tabBar,
                onRouteChanged: { route, isBottomBarVisible in
                    handleRouteChange(route, isBottomBarVisible)
                },
                controllerFactory: { tabBar, routeHandler in
                    MainViewControllerKt.makeFoldersViewController(
                        tabBarVisibility: nil,
                        onRouteChanged: routeHandler
                    )
                }
            )
            .id(themeHostIdentifier)
            .ignoresSafeArea(edges: .vertical)
            .tabItem { Label("Folders", systemImage: "folder") }
            .tag(Tab.folders)

            ComposeHost(
                tabBar: tabBar,
                onRouteChanged: { route, isBottomBarVisible in
                    handleRouteChange(route, isBottomBarVisible)
                },
                controllerFactory: { tabBar, routeHandler in
                    MainViewControllerKt.makeSettingsViewController(
                        tabBarVisibility: nil,
                        onRouteChanged: routeHandler
                    )
                }
            )
            .id(themeHostIdentifier)
            .ignoresSafeArea(edges: .vertical)
            .tabItem { Label("Settings", systemImage: "gearshape") }
            .tag(Tab.settings)
        }
        .preferredColorScheme(darkThemeEnabled ? .dark : .light)
        .onChange(of: darkThemeEnabled) { _ in
            // Keep the user on Settings after theme-triggered controller rebuilds.
            selection = .settings
        }
        .toolbar(tabBar.isVisible ? .visible : .hidden, for: .tabBar)
        .animation(.easeInOut, value: tabBar.isVisible)
    }

    private func handleRouteChange(_ route: String, _ isBottomBarVisible: Bool) {
        let resolved = Tab(routeID: route)
        let shouldShow = resolved != nil && isBottomBarVisible
        NSLog(
            "RouteChange route=%@ bottomBar=%d resolved=%@ currentVisible=%d shouldShow=%d",
            route,
            isBottomBarVisible ? 1 : 0,
            String(describing: resolved),
            tabBar.isVisible ? 1 : 0,
            shouldShow ? 1 : 0
        )
        if route == lastRouteHandled && shouldShow == lastShouldShow {
            return
        }
        lastRouteHandled = route
        lastShouldShow = shouldShow
        if route == Self.lastRouteHandledGlobal && shouldShow == Self.lastShouldShowGlobal {
            return
        }
        Self.lastRouteHandledGlobal = route
        Self.lastShouldShowGlobal = shouldShow
        if (tabBar.isVisible != shouldShow) {
            Self.updateOnMain {
                tabBar.isVisible = shouldShow
            }
        }
        if let resolved, selection != resolved {
            selection = resolved
        }
    }

    private enum Tab: Hashable {
        case notes, folders, settings

        init?(routeID: String) {
            switch routeID {
            case "notes":
                self = .notes
            case _ where routeID.hasPrefix("note/"):
                self = .notes
            case "folders":
                self = .folders
            case _ where routeID.hasPrefix("folder/"):
                self = .folders
            case "settings":
                self = .settings
            default:
                return nil
            }
        }
    }

    @MainActor
    private static func updateTabBar(_ tabBar: TabBarVisibility?, value: Any?) {
        guard let tabBar else { return }
        let resolved: Bool
        if let bool = value as? Bool {
            resolved = bool
        } else if let number = value as? NSNumber {
            resolved = number.boolValue
        } else {
            return
        }
        tabBar.isVisible = resolved
    }

    @MainActor
    private static func updateOnMain(_ action: () -> Void) {
        NSLog("TabBarVisibility::updateOnMain dispatch")
        action()
    }
}

private struct ComposeHost: UIViewControllerRepresentable {
    @ObservedObject var tabBar: TabBarVisibility
    let onRouteChanged: (String, Bool) -> Void
    let controllerFactory: (TabBarVisibility, @escaping (String, KotlinBoolean) -> Void) -> UIViewController

    func makeCoordinator() -> Coordinator {
        Coordinator(onRouteChanged: onRouteChanged)
    }

    func makeUIViewController(context: Context) -> UIViewController {
        context.coordinator.onRouteChanged = onRouteChanged
        let controller = controllerFactory(tabBar, context.coordinator.routeHandler)
        applyTabBarVisibility(for: controller, coordinator: context.coordinator, visible: tabBar.isVisible)
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        context.coordinator.onRouteChanged = onRouteChanged
        applyTabBarVisibility(for: uiViewController, coordinator: context.coordinator, visible: tabBar.isVisible)
    }

    final class Coordinator {
        var onRouteChanged: (String, Bool) -> Void
        var cachedTabBarHeight: CGFloat?
        private var lastRoute: String?
        private var lastVisible: Bool?

        init(onRouteChanged: @escaping (String, Bool) -> Void) {
            self.onRouteChanged = onRouteChanged
        }

        lazy var routeHandler: (String, KotlinBoolean) -> Void = { [weak self] route, isBottomBarVisible in
            guard let self else { return }
            let visible = isBottomBarVisible.boolValue
            if route == lastRoute && visible == lastVisible {
                return
            }
            lastRoute = route
            lastVisible = visible
            self.onRouteChanged(route, visible)
        }
    }

    private func applyTabBarVisibility(for controller: UIViewController, coordinator: Coordinator, visible: Bool) {
        guard let tabController = findTabBarController(startingFrom: controller) else { return }
        let targetHidden = !visible

        if #available(iOS 18.0, *) {
            guard tabController.tabBar.isHidden != targetHidden else { return }
            tabController.setTabBarHidden(targetHidden, animated: true)
            return
        }

        if tabController.tabBar.isHidden == targetHidden && tabController.tabBar.alpha == (visible ? 1 : 0) {
            return
        }

        if coordinator.cachedTabBarHeight == nil {
            let height = tabController.tabBar.bounds.height
            if height > 0 {
                coordinator.cachedTabBarHeight = height
            }
        }
        let measuredHeight = coordinator.cachedTabBarHeight ?? tabController.tabBar.bounds.height
        let baseHeight = measuredHeight > 0 ? measuredHeight : 49
        let additionalInset = targetHidden ? -baseHeight : 0
        if tabController.additionalSafeAreaInsets.bottom != additionalInset {
            tabController.additionalSafeAreaInsets.bottom = additionalInset
            var frame = tabController.view.frame
            frame.size.height += 0.1
            tabController.view.frame = frame
            frame.size.height -= 0.1
            tabController.view.frame = frame
            tabController.view.setNeedsLayout()
            tabController.view.layoutIfNeeded()
        }

        if targetHidden {
            UIView.animate(withDuration: 0.2) {
                tabController.tabBar.alpha = 0
            } completion: { _ in
                tabController.tabBar.isHidden = true
            }
        } else {
            tabController.tabBar.isHidden = false
            tabController.tabBar.alpha = 0
            UIView.animate(withDuration: 0.25) {
                tabController.tabBar.alpha = 1
            }
        }
    }

    private func findTabBarController(startingFrom controller: UIViewController) -> UITabBarController? {
        var current: UIViewController? = controller
        while let candidate = current {
            if let tabController = candidate as? UITabBarController {
                return tabController
            }
            current = candidate.parent
        }
        return nil
    }
}
