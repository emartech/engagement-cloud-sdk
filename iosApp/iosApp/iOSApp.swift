import SwiftUI
import EngagementCloudSDK

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor private var appDelegate: EngagementCloudSDKAppDelegate
    
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
