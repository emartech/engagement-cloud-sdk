// swift-tools-version:5.3
import PackageDescription

let engagementCloudSDKPackageName = "EngagementCloudSDK"
let engagementCloudSDKUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.179/EngagementCloudSDK.xcframework.zip"
let engagementCloudSDKChecksum = "8d24ac17a90b697a0ab25c7f48267001672b6327d4c2d9357e3fe2dab318d124"

let engagementCloudNotificationServicePackageName = "EngagementCloudSDKNotificationService"
let engagementCloudNotificationServiceUrl = "https://github.com/emartech/engagement-cloud-sdk/releases/download/0.0.179/EngagementCloudSDKNotificationService.xcframework.zip"
let engagementCloudNotificationServiceChecksum = "7f5cdef03036382a1243190ba89017f478c422a5409049a01c4194147c9adde9"

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