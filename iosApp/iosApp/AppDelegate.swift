import UIKit
import UserNotifications
import Shared

/**
 * Registers for APNs and forwards the device token (hex) to Kotlin [IosPushRegistration].
 */
final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    /// Last APNs token (hex); used after login if token arrived before session started.
    static var pendingApnsTokenHex: String?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { _, _ in
            DispatchQueue.main.async {
                application.registerForRemoteNotifications()
            }
        }
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let hex = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        AppDelegate.pendingApnsTokenHex = hex
        IosPushRegistration.shared.syncApnsTokenIfLoggedIn(tokenHex: hex)
    }

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        // Simulator or missing capability; no-op for production builds with Push enabled in Xcode.
    }
}
