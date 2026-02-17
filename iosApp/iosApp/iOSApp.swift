import SwiftUI
import SAPEngagementCloudSDK

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor private var appDelegate: EngagementCloudSDKAppDelegate
    
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
