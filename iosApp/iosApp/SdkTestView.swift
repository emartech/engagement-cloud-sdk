import SwiftUI
import EngagementCloudSDK

struct SdkTestView: View {
    @State private var eventName = ""
    private let engagementCloud = EngagementCloud.shared
    @State private var showInlineInApp = false

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
                let deeplinkHandled = engagementCloud.deepLink.track(userActivity: userActivity)
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
            
            Button {
                self.showInlineInApp.toggle()
            } label: {
                Text("Show InlineInApp")
            }

            
            if (self.showInlineInApp) {
                InlineInAppViewWrapper(viewId: "ia")
            }
        }
    }
    
    func enableTracking() {
        Task {
        
            try? await engagementCloud.setup.enable(config: IosEngagementCloudSDKConfig(applicationCode: "EMSE3-B4341"))
            try? await engagementCloud.contact.link(contactFieldValue: "test@test.com")
        }
    }
    
    func trackEvent(eventName: String) {
        Task {
            try await engagementCloud.event.track(event: CustomEvent(name: eventName, attributes: nil))
        }
    }
}

#Preview {
    SdkTestView()
}
