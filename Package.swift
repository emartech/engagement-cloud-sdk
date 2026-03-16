// swift-tools-version:5.3
import PackageDescription

let engagementCloudSDKPackageName = "EngagementCloudSDK"
let engagementCloudSDKUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.234/EngagementCloudSDK.xcframework.zip"
let engagementCloudSDKChecksum = "dcebfe402cdfc645aaaa17267f1b8957498162d1da4958817e7f3c59d92ef671"

let engagementCloudNotificationServicePackageName = "EngagementCloudSDKNotificationService"
let engagementCloudNotificationServiceUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.234/EngagementCloudSDKNotificationService.xcframework.zip"
let engagementCloudNotificationServiceChecksum = "7f0c7bd5174c3640f7902004a02910f7687fcd2b618cdeda826971d3c299affc"

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