//
//
// Copyright © 2024 Emarsys-Technologies Kft. All rights reserved.
//

import Foundation
import SwiftUI
import EmarsysSDK


class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    let center = UNUserNotificationCenter.current()
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {

        Task {
            try? await Emarsys.shared.initialize()
            
            center.delegate = Emarsys.shared.push.emarsysUserNotificationCenterDelegate
            do {
                try await Emarsys.shared.enableTracking(config: EmarsysConfig(applicationCode: "EMS11-C3FD3"))
                try await Emarsys.shared.contact.link(contactFieldId: 2575, contactFieldValue: "test2@test.com")
            } catch {
                print(error)
            }
        }
        
        Task { @MainActor in
            do {
                let permissionGranted = try await center.requestAuthorization(options: [.alert, .sound, .badge, .provisional])
                if permissionGranted {
                    DispatchQueue.main.async {
                        UIApplication.shared.registerForRemoteNotifications()
                    }
                    print("Push permission granted!")
                } else {
                    print("Push not allowed!")
                }
            } catch {
                print("No permissons!")
            }
        }
        
        return true
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let tokenString = deviceToken.reduce("", {$0 + String(format: "%02x", $1)})
        print("Push token - \(tokenString)")
    
        Task {
            do {
                let result = try await Emarsys.shared.push.registerToken(token: tokenString)
            } catch {
                print("push token track failed: \(error)")
            }
        }
    }
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("Failed to register for remote notification. Error \(error)")
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter, 
                                willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.badge, .banner, .list, .sound])
    }
}
