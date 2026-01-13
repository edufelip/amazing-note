import SwiftUI
import FirebaseCore
import GoogleSignIn

@main
struct AmazingNoteiOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    init() {
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }
    }

    var body: some Scene {
        WindowGroup {
            let env = ProcessInfo.processInfo.environment
            let isUiTestMode = env["UITEST_MODE"] == "1"
            let isRunningTests =
                env["XCTestConfigurationFilePath"] != nil || NSClassFromString("XCTestCase") != nil
            if isUiTestMode {
                UITestRootView()
            } else if isRunningTests {
                Color.clear
            } else {
                LiquidRoot()
                    .onOpenURL { url in
                        _ = GIDSignIn.sharedInstance.handle(url)
                    }
            }
        }
    }
}
