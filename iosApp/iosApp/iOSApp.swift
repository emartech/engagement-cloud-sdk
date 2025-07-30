import SwiftUI
import EmarsysSDK

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor private var appDelegate: EmarsysAppDelegate
    
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
