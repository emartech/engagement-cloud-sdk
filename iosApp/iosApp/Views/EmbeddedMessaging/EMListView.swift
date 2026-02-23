import Foundation
import SwiftUI
import UIKit
import EngagementCloudSDK


struct EmbeddedMessagingListView: UIViewControllerRepresentable {
    
    func makeUIViewController(context: Context) -> UIViewController {
        EngagementCloud.shared.embeddedMessaging.listPageView(showFilters: true)
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
