import Foundation
import SwiftUI
import SAPEngagementCloudSDK


struct EmbeddedMessagingListView: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        EngagementCloud.shared.embeddedMessage()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
