// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudSDK' (do not edit)
let remoteEngagementCloudSDKUrl = ""
let remoteEngagementCloudSDKChecksum = ""
let engagementCloudSDKPackageName = "EngagementCloudSDK"
// END KMMBRIDGE BLOCK FOR 'EngagementCloudSDK'

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudNotificationService' (do not edit)
let remoteEngagementCloudNotificationServiceUrl = ""
let remoteEngagementCloudNotificationServiceChecksum = ""
let engagementCloudNotificationServicePackageName = "EngagementCloudNotificationService"
// END KMMBRIDGE BLOCK FOR 'EngagementCloudNotificationService'


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
            url: remoteEngagementCloudSDKUrl,
            checksum: remoteEngagementCloudSDKChecksum
        ),
        .binaryTarget(
            name: engagementCloudNotificationServicePackageName,
            url: remoteEngagementCloudNotificationServiceUrl,
            checksum: remoteEngagementCloudNotificationServiceChecksum
        ),
    ]
)