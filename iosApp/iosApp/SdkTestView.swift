import EngagementCloudSDK
import SwiftUI

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


            if self.showInlineInApp {
                InlineInAppViewWrapper(
                    viewId: "ia",
                    onLoaded: {
                        print("loaded")
                    },
                    onClose: {
                        self.showInlineInApp.toggle()
                    })
            }
        }
    }

    func enableTracking() {
        Task {
            try? await engagementCloud.setup.enable(
                config: IosEngagementCloudSDKConfig(applicationCode: "EMSE3-B4341"),
                onContactLinkingFailed: { onSuccess, onError in
                    Task {
                        // login
                        onSuccess(LinkContactDataContactFieldValueData(contactFieldValue: "test1@test.com"))
                    }
                }
            )
            try? await engagementCloud.contact.link(contactFieldValue: "test1@test.com")
            
            engagementCloud.registerEventListener { event in
                switch onEnum(of: event) {
                    case .appEvent(let appEvent): print("appEvent name \(appEvent.name)")
                    case .badgeCountEvent(let badgeCountEvent): print("badgeCountEvent method \(badgeCountEvent.method) count: \(badgeCountEvent.badgeCount)")
                }

                switch(event) {
                    case let appEvent as AppEvent: print("appEvent received: \(appEvent.name) with payload: \(appEvent.payload ?? [:])")
                    case let badgeCountEvent as BadgeCountEvent: print("badgeCount: \(badgeCountEvent.badgeCount)")
                    default: print("unknown event type")
                }
            }

            for await event in engagementCloud.events {
                switch onEnum(of: event) {
                    case .appEvent(let appEvent): print("AsyncStream appEvent received: \(appEvent.name) with payload: \(appEvent.payload ?? [:])")
                    case .badgeCountEvent(let badgeCountEvent): print("AsyncStream badgeCount: \(badgeCountEvent.badgeCount)")
                }
            }
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
