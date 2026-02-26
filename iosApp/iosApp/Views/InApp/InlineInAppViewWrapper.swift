import Foundation
import SwiftUI
import UIKit
import EngagementCloudSDK


struct InlineInAppViewWrapper: UIViewControllerRepresentable {
    let viewId: String
    let onLoaded: (() -> Void)? = nil
    let onDismiss: (() -> Void)? = nil
    
    func makeUIViewController(context: Context) -> UIViewController {
        EngagementCloud.shared.inApp.InlineInAppView(viewId: viewId, onLoaded: onLoaded, onDismiss: onDismiss)
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
