import Foundation
import SwiftUI
import EngagementCloudSDK


struct EmbeddedMessagingListView: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        EngagementCloud.shared.embeddedMessage()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
