// swift-tools-version:5.3
import PackageDescription

let engagementCloudSDKPackageName = "EngagementCloudSDK"
let engagementCloudSDKUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.124/EngagementCloudSDK.xcframework.zip"
let engagementCloudSDKChecksum = "dd3ccd25b62c77c01c6c0e231ca964f2f9751496fcac026031083ef552aecf2d"

let engagementCloudNotificationServicePackageName = "EngagementCloudSDKNotificationService"
let engagementCloudNotificationServiceUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.124/EngagementCloudSDKNotificationService.xcframework.zip"
let engagementCloudNotificationServiceChecksum = "4200b44e03e1e3ec5eebc3a5d1775a5a7a59e4e0e1667c7c2715856d23008691"

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