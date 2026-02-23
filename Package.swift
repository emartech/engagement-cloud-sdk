// swift-tools-version:5.3
import PackageDescription

let engagementCloudSDKPackageName = "EngagementCloudSDK"
let engagementCloudSDKUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/spm-0.0.69/EngagementCloudSDK.xcframework.zip"
let engagementCloudSDKChecksum = "7a447ac229aeb657853ac1bc3f969f2688f4d7a536784f8bd92e84fdc92769d1"

let engagementCloudNotificationServicePackageName = "EngagementCloudNotificationService"
let engagementCloudNotificationServiceUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/spm-0.0.69/EngagementCloudNotificationService.xcframework.zip"
let engagementCloudNotificationServiceChecksum = "7fa1a4b3177a884b12275120053771e334c9c6f38906de81c2e66a639ddaa461"

let package = Package(
    name: engagementCloudSDKPackageName,
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: engagementCloudSDKPackageName,
            targets: [engagementCloudSDKPackageName]
        ),
        .library(
            name: engagementCloudNotificationServicePackageName,
            targets: [engagementCloudNotificationServicePackageName]
        ),
    ],
    targets: [
        .binaryTarget(
            name: engagementCloudSDKPackageName,
            url: engagementCloudSDKUrl,
            checksum: engagementCloudSDKChecksum
        ),
        .binaryTarget(
            name: engagementCloudNotificationServicePackageName,
            url: engagementCloudNotificationServiceUrl,
            checksum: engagementCloudNotificationServiceChecksum
        ),
    ]
)