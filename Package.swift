// swift-tools-version:5.3
import PackageDescription

let engagementCloudSDKPackageName = "EngagementCloudSDK"
let engagementCloudSDKUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.196/EngagementCloudSDK.xcframework.zip"
let engagementCloudSDKChecksum = "87288385d1ee3c58dddfa8c2d10a86066941aff7ea8b442be6174d72c5cf34bb"

let engagementCloudNotificationServicePackageName = "EngagementCloudSDKNotificationService"
let engagementCloudNotificationServiceUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.196/EngagementCloudSDKNotificationService.xcframework.zip"
let engagementCloudNotificationServiceChecksum = "9913a96463be0f50ef110c8b1f9bcfc06a6fa1f99200581f27c67903bd2a22df"

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