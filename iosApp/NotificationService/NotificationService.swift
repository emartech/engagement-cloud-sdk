//
//
// Copyright © 2024 Emarsys-Technologies Kft. All rights reserved.
//

import UserNotifications
import EmarsysNotificationService


class NotificationService: UNNotificationServiceExtension {
    let emarsysNotificationService = EmarsysNotificationService_()

    var contentHandler: ((UNNotificationContent) -> Void)?
    var bestAttemptContent: UNMutableNotificationContent?

    override func didReceive(_ request: UNNotificationRequest, withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
        emarsysNotificationService.didReceiveNotificationRequest(request: request, withContentHandler: contentHandler)
    }
    
    override func serviceExtensionTimeWillExpire() {
        emarsysNotificationService.serviceExtensionTimeWillExpire()
    }

}
