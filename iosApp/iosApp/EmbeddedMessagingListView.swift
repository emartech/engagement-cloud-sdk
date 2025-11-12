//
//
// Copyright © 2025 Emarsys-Technologies Kft. All rights reserved.
//

import Foundation
import SwiftUI
import EmarsysSDK


struct EmbeddedMessagingListView: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        Emarsys.shared.embeddedMessage()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
