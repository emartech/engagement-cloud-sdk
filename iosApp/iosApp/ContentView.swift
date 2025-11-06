import SwiftUI
import EmarsysSDK


struct ContentView: View {
    @State private var eventName = ""
    
    var body: some View {
        VStack {
            Button {
                enableTracking()
            } label: {
                Text("enableTracking")
            }            
            Button {
                enableTracking()
                let userActivity = NSUserActivity(activityType: NSUserActivityTypeBrowsingWeb)
                userActivity.webpageURL = URL(string: "http://www.google.com/something?fancy_url=1&ems_dl=1_2_3_4_5")
                let deeplinkHandled = Emarsys.shared.deepLink.track(userActivity: userActivity)
                print("Deeplink handled: \(deeplinkHandled)")
            } label: {
                Text("testDeeplinkWithDemoData")
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
            
            MessageItemView()
        }
    }
    
    func enableTracking() {
        Task {
            try? await Emarsys.shared.setup.enableTracking(config: EmarsysConfig(applicationCode: "EMS11-C3FD3"))
            try? await Emarsys.shared.contact.link(contactFieldValue: "test2@test.com")
        }
    }
    
    func trackEvent(eventName: String) {
        Task {
            try await Emarsys.shared.event.track(event: CustomEvent(name: eventName, attributes: nil))
        }
    }
}

#Preview {
    ContentView()
}
