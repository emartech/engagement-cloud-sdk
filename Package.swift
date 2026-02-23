// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudSDK' (do not edit)
let remoteEngagementCloudSDKUrl = "https://maven.pkg.github.com/emartech/engagement-cloud-sdk/com/sap/engagement-cloud-sdk-kmmbridge/0.0.51/engagement-cloud-sdk-kmmbridge-0.0.51.zip"
let remoteEngagementCloudSDKChecksum = "a6a9d91d9be52f81ec073a96c7e5d9b0a54fc91f04b1a780d77e4d429fbac7ab"
let engagementCloudSDKPackageName = "EngagementCloudSDK"
// END KMMBRIDGE BLOCK FOR 'EngagementCloudSDK'

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudNotificationService' (do not edit)
let remoteEngagementCloudNotificationServiceUrl = "https://maven.pkg.github.com/emartech/engagement-cloud-sdk/com/sap/ios-notification-service-kmmbridge/0.0.51/ios-notification-service-kmmbridge-0.0.51.zip"
let remoteEngagementCloudNotificationServiceChecksum = "c5adf99be34fa464ad07fdb1652d759bf842aedc9bdde712b400f9b29e371a1d"
let engagementCloudNotificationServicePackageName = "EngagementCloudNotificationService"
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