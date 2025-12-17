import UIKit
import FirebaseCore
import GoogleSignIn
import FirebaseCrashlytics

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()
        #if DEBUG
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(false)
        #else
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(true)
        #endif
        logBuildInfo()
        return true
    }

    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        let configuration = UISceneConfiguration(
            name: "Default Configuration",
            sessionRole: connectingSceneSession.role
        )
        configuration.delegateClass = SceneDelegate.self
        return configuration
    }

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        GIDSignIn.sharedInstance.handle(url)
    }

    private func logBuildInfo() {
        let info = Bundle.main.infoDictionary
        let sdkName = (info?["DTSDKName"] as? String) ?? "unknown"
        let xcode = (info?["DTXcode"] as? String) ?? "unknown"
        let xcodeBuild = (info?["DTXcodeBuild"] as? String) ?? "unknown"

        NSLog("BuildInfo DTXcode=\(xcode) DTXcodeBuild=\(xcodeBuild) DTSDKName=\(sdkName)")

        #if !DEBUG
        let crashlytics = Crashlytics.crashlytics()
        crashlytics.setCustomValue(sdkName, forKey: "dt_sdk_name")
        crashlytics.setCustomValue(xcode, forKey: "dt_xcode")
        crashlytics.setCustomValue(xcodeBuild, forKey: "dt_xcode_build")
        #endif
    }
}
