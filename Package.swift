// swift-tools-version:5.3
import PackageDescription

let engagementCloudSDKPackageName = "EngagementCloudSDK"
let engagementCloudSDKUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.99/EngagementCloudSDK.xcframework.zip"
let engagementCloudSDKChecksum = "9766a5a9753a1d82e8fac2d53d8116adc2805554c6c67556153f864f0cbc5393"

let engagementCloudNotificationServicePackageName = "EngagementCloudNotificationService"
let engagementCloudNotificationServiceUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.99/EngagementCloudNotificationService.xcframework.zip"
let engagementCloudNotificationServiceChecksum = "248da84a5e4850fbbb67b55abf4fe43f1ab8a467ac80a8e027707e5688bfcc64"

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