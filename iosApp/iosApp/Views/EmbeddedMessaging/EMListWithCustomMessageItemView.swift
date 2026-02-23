import UIKit
import SwiftUI
import EngagementCloudSDK

struct EMListWithCustomMessageItemView: UIViewControllerRepresentable {
    
    func makeUIViewController(context: Context) -> UIViewController {
        EngagementCloud.shared.embeddedMessaging.compactListView {
            print("HELLO: onNavigate")
        } customMessageItem: { viewModel, isSelected in
            let vc = UIViewController()
                    
            let label = UILabel()
            label.text = "Message title: \(viewModel.title)"
            label.numberOfLines = 0
            label.textAlignment = .center
            label.translatesAutoresizingMaskIntoConstraints = false
            
            vc.view.addSubview(label)
            
            vc.view.frame.size.width = 300
            vc.view.frame.size.height = 300

            return vc
        }
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
