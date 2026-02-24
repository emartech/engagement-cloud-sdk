import Foundation
import SwiftUI
import UIKit
import EngagementCloudSDK


struct InlineInAppViewWrapper: UIViewControllerRepresentable {
    let viewId: String
    
    func makeUIViewController(context: Context) -> UIViewController {
        EngagementCloud.shared.inApp.InlineInAppView(viewId: viewId)
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
