import Foundation
import UIKit

@objc public class EngagementCloudSDKAppDelegate: UIResponder, UIApplicationDelegate {

    private let ec = EngagementCloud.shared

    @objc public func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        Task {
            let center = UNUserNotificationCenter.current()
            center.delegate = self.ec.push.userNotificationCenterDelegate

            UIApplication.shared.registerForRemoteNotifications()
            let notificationCenter = UNUserNotificationCenter.current()
            var authorizationOptions: UNAuthorizationOptions = [.alert, .sound, .badge]
            if #available(iOS 12.0, *) {
                authorizationOptions.insert(.provisional)
            }
            do {
                let _ = try await notificationCenter.requestAuthorization(options: authorizationOptions)
            } catch {
                print("Error requesting notification authorization: \(error.localizedDescription)")
            }

        }
        return true
    }

    @objc public func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        return self.ec.deepLink.track(userActivity: userActivity)
    }

    @objc public func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let token = deviceToken.map { data in String(format: "%02.2hhx", data) }.joined()
        Task {
            try? await self.ec.push.registerToken(token: token)
        }
    }

    @objc public func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        Task {
            try? await self.ec.push.handleSilentMessageWithUserInfo(rawUserInfo: userInfo as! [String: Any])
        }
        completionHandler(.newData)
    }
}
