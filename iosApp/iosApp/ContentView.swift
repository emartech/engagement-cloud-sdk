import SwiftUI
import SAPEngagementCloudSDK


struct ContentView: View {
    @State private var eventName = ""
    
    var body: some View {
        TabView {
            SdkTestView()
                .tabItem {
                    Label {
                        Text("SdkTest")
                    } icon: {
                        Image(systemName: "testtube.2")
                    }
                }
            EmbeddedMessagingView()
                .tabItem {
                    Label {
                        Text("EmbeddedMessaging")
                    } icon: {
                        Image(systemName: "message")
                    }
                }
        }
    }
}

#Preview {
    ContentView()
}
