// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'SAPEngagementCloudSDK' (do not edit)
let SdkPackageName = "SAPEngagementCloudSDK"
// END KMMBRIDGE BLOCK FOR 'SAPEngagementCloudSDK'

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'SAPEngagementCloudNotificationService' (do not edit)
let NotificationServicePackageName = "SAPEngagementCloudNotificationService"
// END KMMBRIDGE BLOCK FOR 'SAPEngagementCloudNotificationService'


let package = Package(
    name: SdkPackageName,
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: SdkPackageName,
            targets: [SdkPackageName]
        ),
        .library(
            name: NotificationServicePackageName,
            targets: [NotificationServicePackageName]
        ),
    ],
    targets: [
        .binaryTarget(
            name: SdkPackageName,
            path: "./engagement-cloud-sdk/build/XCFrameworks/debug/\(SdkPackageName).xcframework"
        ),
        .binaryTarget(
            name: NotificationServicePackageName,
            path: "./ios-notification-service/build/XCFrameworks/debug/\(NotificationServicePackageName).xcframework"
        ),
    ]
)