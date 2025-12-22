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
    private let forcedColorScheme: ColorScheme?

    init(
        forcedColorScheme: ColorScheme? = nil,
        initialTabId: String? = nil
    ) {
        self.forcedColorScheme = forcedColorScheme
        if let initialTabId, let resolvedTab = Tab(routeID: initialTabId) {
            _selection = State(initialValue: resolvedTab)
        }
    }

    private var themeHostIdentifier: String {
        darkThemeEnabled ? "theme-dark" : "theme-light"
    }

    var body: some View {
        TabView(selection: $selection) {
            ComposeHost(
                tabBar: tabBar,
                isActive: selection == .notes,
                onRouteChanged: { route in
                    handleRouteChange(route)
                },
                onTabBarVisibilityChanged: { isVisible in
                    handleTabBarVisibilityChange(isVisible)
                },
                controllerFactory: { tabBarVisibility, routeHandler in
                    MainViewControllerKt.makeNotesViewController(
                        tabBarVisibility: tabBarVisibility,
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
                isActive: selection == .folders,
                onRouteChanged: { route in
                    handleRouteChange(route)
                },
                onTabBarVisibilityChanged: { isVisible in
                    handleTabBarVisibilityChange(isVisible)
                },
                controllerFactory: { tabBarVisibility, routeHandler in
                    MainViewControllerKt.makeFoldersViewController(
                        tabBarVisibility: tabBarVisibility,
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
                isActive: selection == .settings,
                onRouteChanged: { route in
                    handleRouteChange(route)
                },
                onTabBarVisibilityChanged: { isVisible in
                    handleTabBarVisibilityChange(isVisible)
                },
                controllerFactory: { tabBarVisibility, routeHandler in
                    MainViewControllerKt.makeSettingsViewController(
                        tabBarVisibility: tabBarVisibility,
                        onRouteChanged: routeHandler
                    )
                }
            )
            .id(themeHostIdentifier)
            .ignoresSafeArea(edges: .vertical)
            .tabItem { Label("Settings", systemImage: "gearshape") }
            .tag(Tab.settings)
        }
        .preferredColorScheme(forcedColorScheme ?? (darkThemeEnabled ? .dark : .light))
        .onChange(of: darkThemeEnabled) { _ in
            // Keep the user on Settings after theme-triggered controller rebuilds.
            selection = .settings
        }
        .toolbar(tabBar.isVisible ? .visible : .hidden, for: .tabBar)
        .animation(.easeInOut, value: tabBar.isVisible)
    }

    private func handleRouteChange(_ route: String) {
        let resolved = Tab(routeID: route)
        NSLog(
            "RouteChange route=%@ resolved=%@ currentTab=%@",
            route,
            String(describing: resolved),
            String(describing: selection)
        )
        if route == lastRouteHandled {
            return
        }
        lastRouteHandled = route
        if let resolved, selection != resolved {
            selection = resolved
        }
    }

    private func handleTabBarVisibilityChange(_ visible: Bool) {
        if tabBar.isVisible == visible {
            return
        }
        tabBar.isVisible = visible
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

    // Visibility is driven by the shared tab bar policy and only from the active tab host.
}

private struct ComposeHost: UIViewControllerRepresentable {
    @ObservedObject var tabBar: TabBarVisibility
    let isActive: Bool
    let onRouteChanged: (String) -> Void
    let onTabBarVisibilityChanged: (Bool) -> Void
    let controllerFactory: (@escaping (KotlinBoolean) -> Void, @escaping (String, KotlinBoolean) -> Void) -> UIViewController

    func makeCoordinator() -> Coordinator {
        Coordinator(onRouteChanged: onRouteChanged, onTabBarVisibilityChanged: onTabBarVisibilityChanged, isActive: isActive)
    }

    func makeUIViewController(context: Context) -> UIViewController {
        context.coordinator.onRouteChanged = onRouteChanged
        context.coordinator.onTabBarVisibilityChanged = onTabBarVisibilityChanged
        context.coordinator.isActive = isActive
        let controller = controllerFactory(context.coordinator.tabBarVisibilityHandler, context.coordinator.routeHandler)
        applyTabBarVisibility(for: controller, coordinator: context.coordinator, visible: tabBar.isVisible)
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        context.coordinator.onRouteChanged = onRouteChanged
        context.coordinator.onTabBarVisibilityChanged = onTabBarVisibilityChanged
        context.coordinator.isActive = isActive
        applyTabBarVisibility(for: uiViewController, coordinator: context.coordinator, visible: tabBar.isVisible)
    }

    final class Coordinator {
        var onRouteChanged: (String) -> Void
        var onTabBarVisibilityChanged: (Bool) -> Void
        var isActive: Bool
        var cachedTabBarHeight: CGFloat?
        private var lastRoute: String?
        private var lastVisible: Bool?

        init(
            onRouteChanged: @escaping (String) -> Void,
            onTabBarVisibilityChanged: @escaping (Bool) -> Void,
            isActive: Bool
        ) {
            self.onRouteChanged = onRouteChanged
            self.onTabBarVisibilityChanged = onTabBarVisibilityChanged
            self.isActive = isActive
        }

        lazy var routeHandler: (String, KotlinBoolean) -> Void = { [weak self] route, _ in
            guard let self else { return }
            guard isActive else { return }
            if route == lastRoute {
                return
            }
            lastRoute = route
            self.onRouteChanged(route)
        }

        lazy var tabBarVisibilityHandler: (KotlinBoolean) -> Void = { [weak self] isVisible in
            guard let self else { return }
            guard isActive else { return }
            let visible = isVisible.boolValue
            if visible == lastVisible {
                return
            }
            lastVisible = visible
            self.onTabBarVisibilityChanged(visible)
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
