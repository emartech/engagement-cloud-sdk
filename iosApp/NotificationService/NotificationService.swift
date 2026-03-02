import UserNotifications
import EngagementCloudSDKNotificationService


class NotificationService: UNNotificationServiceExtension {
    let engagementCloudNotificationService = EngagementCloudNotificationService()

    override func didReceive(_ request: UNNotificationRequest, withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
        engagementCloudNotificationService.didReceiveNotificationRequest(request: request, withContentHandler: contentHandler)
    }
    
    override func serviceExtensionTimeWillExpire() {
        engagementCloudNotificationService.serviceExtensionTimeWillExpire()
    }
}
