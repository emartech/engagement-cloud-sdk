import Foundation
import SwiftUI
import UIKit
import EngagementCloudSDK


struct InlineInAppViewWrapper: UIViewControllerRepresentable {
    let viewId: String
    let onLoaded: (() -> Void)?
    let onClose: (() -> Void)?
    
    func makeUIViewController(context: Context) -> UIViewController {
        EngagementCloud.shared.inApp.InlineInAppView(viewId: viewId, onLoaded: onLoaded, onClose: onClose)
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
