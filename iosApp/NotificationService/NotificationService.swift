import UserNotifications
import EngagementCloudNotificationService


class NotificationService: UNNotificationServiceExtension {
    let engagementCloudNotificationService = EngagementCloudNotificationService_()

    var contentHandler: ((UNNotificationContent) -> Void)?
    var bestAttemptContent: UNMutableNotificationContent?

    override func didReceive(_ request: UNNotificationRequest, withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
        engagementCloudNotificationService.didReceiveNotificationRequest(request: request, withContentHandler: contentHandler)
    }
    
    override func serviceExtensionTimeWillExpire() {
        engagementCloudNotificationService.serviceExtensionTimeWillExpire()
    }

}
