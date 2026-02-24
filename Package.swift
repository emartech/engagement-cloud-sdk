// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudSDK' (do not edit)
let SdkPackageName = "EngagementCloudSDK"
// END KMMBRIDGE BLOCK FOR 'EngagementCloudSDK'

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudNotificationService' (do not edit)
let NotificationServicePackageName = "EngagementCloudNotificationService"
// END KMMBRIDGE BLOCK FOR 'EngagementCloudNotificationService'


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