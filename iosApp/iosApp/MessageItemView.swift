//
//
// Copyright © 2025 Emarsys-Technologies Kft. All rights reserved.
//

import Foundation
import SwiftUI
import EmarsysSDK


struct MessageItemView: UIViewControllerRepresentable {
    typealias UIViewControllerType = UIViewController
    
    func makeUIViewController(context: Context) -> UIViewController {
        Emarsys.shared.embeddedMessagingItem()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
