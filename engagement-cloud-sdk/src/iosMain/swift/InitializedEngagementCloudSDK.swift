import Foundation
import UserNotifications

@propertyWrapper
public class InitializedEngagementCloudSDK {
    private let engagementCloud: EngagementCloud

    public var wrappedValue: EngagementCloud! {
        get {
            return engagementCloud
        }
    }

    public init(_ wrappedValue: EngagementCloud? = nil) {
        self.engagementCloud = EngagementCloud.shared
        Task {
            try? await engagementCloud.initialize()
            let center = UNUserNotificationCenter.current()
            center.delegate = engagementCloud.push.userNotificationCenterDelegate
        }
    }
}