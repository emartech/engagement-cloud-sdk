import SwiftUI
import EmarsysSDK

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor private var appDelegate: AppDelegate
    
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
