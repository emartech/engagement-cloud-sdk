import SwiftUI
import EmarsysSDK


struct ContentView: View {
    @State private var eventName = ""
    
    var body: some View {
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
    
    func trackEvent(eventName: String) {
        Task {
            try await Emarsys.shared.tracking.track(event: CustomEvent(name: eventName, attributes: nil))
        }
    }
}

#Preview {
    ContentView()
}
