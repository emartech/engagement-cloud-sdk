// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudSDK' (do not edit)
let EngagementCloudSDKPackageName = "EngagementCloudSDK"
let EngagementCloudSDKUrl = ""
let EngagementCloudSDKChecksum = ""
// END KMMBRIDGE BLOCK FOR 'EngagementCloudSDK'

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudNotificationService' (do not edit)
let EngagementCloudNotificationServicePackageName = "EngagementCloudNotificationService"
let EngagementCloudNotificationServiceUrl = ""
let EngagementCloudNotificationServiceChecksum = ""
// END KMMBRIDGE BLOCK FOR 'EngagementCloudNotificationService'


let package = Package(
    name: EngagementCloudSDKPackageName,
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: EngagementCloudSDKPackageName,
            targets: [EngagementCloudSDKPackageName]
        ),
        .library(
            name: EngagementCloudNotificationServicePackageName,
            targets: [EngagementCloudNotificationServicePackageName]
        ),
    ],
    targets: [
        .binaryTarget(
            name: EngagementCloudSDKPackageName,
            url: EngagementCloudSDKUrl,
            checksum: EngagementCloudSDKChecksum
        ),
        .binaryTarget(
            name: EngagementCloudNotificationServicePackageName,
            url: EngagementCloudNotificationServiceUrl,
            checksum: EngagementCloudNotificationServiceChecksum
        ),
    ]
)