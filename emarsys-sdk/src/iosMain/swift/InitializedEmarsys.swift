import Foundation
import UserNotifications

@propertyWrapper
public class InitializedEmarsys {
    private let emarsys: Emarsys

    public var wrappedValue: Emarsys! {
        get {
            return emarsys
        }
    }

    public init(_ wrappedValue: Emarsys? = nil) {
        self.emarsys = Emarsys.shared
        Task {
            try? await emarsys.initialize()
            let center = UNUserNotificationCenter.current()
            center.delegate = emarsys.push.emarsysUserNotificationCenterDelegate
        }
    }
}