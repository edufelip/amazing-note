import Foundation
import UIKit
import SwiftUI
import ComposeApp

final class TabBarVisibility: ObservableObject {
    @Published var isVisible: Bool = true
}

struct LiquidRoot: View {
    @AppStorage("dark_theme") private var darkThemeEnabled: Bool = true
    @State private var selection: Tab = .notes
    @StateObject private var tabBar = TabBarVisibility()

    private var themeHostIdentifier: String {
        darkThemeEnabled ? "theme-dark" : "theme-light"
    }

    var body: some View {
        TabView(selection: $selection) {
            ComposeHost(
                tabBar: tabBar,
                onRouteChanged: handleRouteChange,
                controllerFactory: { tabBar, routeHandler in
                    MainViewControllerKt.makeNotesViewController(
                        tabBarVisibility: { [weak tabBar] isVisible in
                            Self.updateTabBar(tabBar, value: isVisible)
                        },
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
                onRouteChanged: handleRouteChange,
                controllerFactory: { tabBar, routeHandler in
                    MainViewControllerKt.makeFoldersViewController(
                        tabBarVisibility: { [weak tabBar] isVisible in
                            Self.updateTabBar(tabBar, value: isVisible)
                        },
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
                onRouteChanged: handleRouteChange,
                controllerFactory: { tabBar, routeHandler in
                    MainViewControllerKt.makeSettingsViewController(
                        tabBarVisibility: { [weak tabBar] isVisible in
                            Self.updateTabBar(tabBar, value: isVisible)
                        },
                        onRouteChanged: routeHandler
                    )
                }
            )
            .id(themeHostIdentifier)
            .ignoresSafeArea(edges: .vertical)
            .tabItem { Label("Settings", systemImage: "gearshape") }
            .tag(Tab.settings)
        }
        .toolbar(tabBar.isVisible ? .visible : .hidden, for: .tabBar)
        .animation(.easeInOut, value: tabBar.isVisible)
    }

    private func handleRouteChange(_ route: String) {
        let resolved = Tab(routeID: route)
        let shouldShow = resolved != nil
        Self.updateOnMain {
            tabBar.isVisible = shouldShow
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
        updateOnMain {
            tabBar.isVisible = resolved
        }
    }

    private static func updateOnMain(_ action: @escaping () -> Void) {
        if Thread.isMainThread {
            action()
            return
        }
        DispatchQueue.main.async(execute: action)
    }
}

private struct ComposeHost: UIViewControllerRepresentable {
    @ObservedObject var tabBar: TabBarVisibility
    let onRouteChanged: (String) -> Void
    let controllerFactory: (TabBarVisibility, @escaping (String) -> Void) -> UIViewController

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
        var onRouteChanged: (String) -> Void
        var cachedTabBarHeight: CGFloat?

        init(onRouteChanged: @escaping (String) -> Void) {
            self.onRouteChanged = onRouteChanged
        }

        lazy var routeHandler: (String) -> Void = { [weak self] route in
            self?.onRouteChanged(route)
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
