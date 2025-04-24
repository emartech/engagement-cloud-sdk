import SwiftUI
import EmarsysSDK

    
    struct ContentView: View {
        @State private var eventName = ""
        
        var body: some View {
            Button {
                startSDK()
            } label: {
                Text("enable sdk")
            }
            HStack {
                TextField(
                        "Event name",
                        text: $eventName
                )
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .textInputAutocapitalization(TextInputAutocapitalization.never)
                Button {
                    trackEvent(eventName: eventName)
                } label: {
                    Text("track event")
                }
            }.padding(8)
            
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
        
        func trackEvent(eventName: String) {
            Task {
                try await Emarsys.shared.trackCustomEvent(event: eventName, attributes: nil)
            }
        }
        
    }
    
    
    
