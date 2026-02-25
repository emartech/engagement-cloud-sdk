// swift-tools-version:5.3
import PackageDescription

let engagementCloudSDKPackageName = "EngagementCloudSDK"
let engagementCloudSDKUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.98/EngagementCloudSDK.xcframework.zip"
let engagementCloudSDKChecksum = "f990b3a9498704cf27b39fc4ac1b4441798f686e2803fa5f6738c740fed69863"

let engagementCloudNotificationServicePackageName = "EngagementCloudNotificationService"
let engagementCloudNotificationServiceUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.98/EngagementCloudNotificationService.xcframework.zip"
let engagementCloudNotificationServiceChecksum = "6256f339303db519e847462e5954a1a48fb749738df0e1e8b5a6da0e411d27be"

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