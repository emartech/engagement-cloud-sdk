// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudSDK' (do not edit)
let remoteEngagementCloudSDKUrl = "https://maven.pkg.github.com/emartech/engagement-cloud-sdk/com/sap/engagement-cloud-sdk-kmmbridge/0.0.56/engagement-cloud-sdk-kmmbridge-0.0.56.zip"
let remoteEngagementCloudSDKChecksum = "7dc2d5395c59ed3d88e7371842ab479769121e62faf13f2b5d4d030d73de6afb"
let engagementCloudSDKPackageName = "EngagementCloudSDK"
// END KMMBRIDGE BLOCK FOR 'EngagementCloudSDK'

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudNotificationService' (do not edit)
let remoteEngagementCloudNotificationServiceUrl = "https://maven.pkg.github.com/emartech/engagement-cloud-sdk/com/sap/ios-notification-service-kmmbridge/0.0.56/ios-notification-service-kmmbridge-0.0.56.zip"
let remoteEngagementCloudNotificationServiceChecksum = "bfce8cf70c2bae8a8bd9f056ba5c9fd8f426f21ba8dc893e899f82dd2be39527"
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