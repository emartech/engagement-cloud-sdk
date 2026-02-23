// swift-tools-version:5.3
import PackageDescription

let engagementCloudSDKPackageName = "EngagementCloudSDK"
let engagementCloudSDKUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.73/EngagementCloudSDK.xcframework.zip"
let engagementCloudSDKChecksum = "550ce73294d24ed28aa7556216507401a7349f3fa6e31b683339840736d0e7f8"

let engagementCloudNotificationServicePackageName = "EngagementCloudNotificationService"
let engagementCloudNotificationServiceUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.73/EngagementCloudNotificationService.xcframework.zip"
let engagementCloudNotificationServiceChecksum = "30fa37e11f20af01fc113973f69f9f7d9e9d5d59ac71331f3e0517506fe49ec5"

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