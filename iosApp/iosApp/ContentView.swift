import UIKit
import SwiftUI
import EmarsysSDK

    
    struct ContentView: View {
        var body: some View {
            Button {
                startSDK()
            } label: {
                Text("enable sdk")
            }
        }
        
        func startSDK() {
            Task {
                do {
                    try await Emarsys.shared.initialize()
                    try await Emarsys.shared.enableTracking(config: EmarsysConfig(applicationCode: "EMS11-C3FD3"))
                    try await Emarsys.shared.linkContact(contactFieldId: 2575, contactFieldValue: "test2@test.com")
                    
                    Emarsys.shared.push.customerUserNotificationCenterDelegate = nil
                    
                    
                    let result = try await Emarsys.shared.push.registerPushToken(pushToken: AppDelegate.pushToken)
                    print("push token track result: \(result)")
                } catch {
                    print(error)
                    print("setup failed")
                }
            }
        }
        
    }
    
    
    
