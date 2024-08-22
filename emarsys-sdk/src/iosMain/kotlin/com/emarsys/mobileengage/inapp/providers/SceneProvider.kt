package com.emarsys.mobileengage.inapp.providers

import com.emarsys.core.providers.Provider
import platform.UIKit.UIApplication
import platform.UIKit.UIScene
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UISceneActivationStateForegroundInactive

class SceneProvider(private val application: UIApplication) : Provider<UIScene> {
    override fun provide(): UIScene {
        val scene = (application.connectedScenes() as Set<UIScene>).first { scene: UIScene ->
            scene.activationState == UISceneActivationStateForegroundActive || scene.activationState == UISceneActivationStateForegroundInactive
        }
        return scene
    }
}