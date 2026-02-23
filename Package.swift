// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudSDK' (do not edit)
let remoteEngagementCloudSDKUrl = "https://maven.pkg.github.com/emartech/engagement-cloud-sdk/com/sap/engagement-cloud-sdk-kmmbridge/0.0.59/engagement-cloud-sdk-kmmbridge-0.0.59.zip"
let remoteEngagementCloudSDKChecksum = "93e97d1dfde53b07284badb713b2babaab2032bfd3c43fc9f2aae6f1a0e97e97"
let engagementCloudSDKPackageName = "EngagementCloudSDK"
// END KMMBRIDGE BLOCK FOR 'EngagementCloudSDK'

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudNotificationService' (do not edit)
let remoteEngagementCloudNotificationServiceUrl = "https://maven.pkg.github.com/emartech/engagement-cloud-sdk/com/sap/ios-notification-service-kmmbridge/0.0.59/ios-notification-service-kmmbridge-0.0.59.zip"
let remoteEngagementCloudNotificationServiceChecksum = "6e677eedf405844b6ce6fb89caad2598143f22d58f979f9b27f7179f4d1c1786"
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