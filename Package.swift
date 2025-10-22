// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EmarsysSDK' (do not edit)
let EmarsysSdkPackageName = "EmarsysSDK"
// END KMMBRIDGE BLOCK FOR 'EmarsysSDK'

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EmarsysNotificationService' (do not edit)
let emarsysNotificationServicePackageName = "EmarsysNotificationService"
// END KMMBRIDGE BLOCK FOR 'EmarsysNotificationService'


let package = Package(
    name: EmarsysSdkPackageName,
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: EmarsysSdkPackageName,
            targets: [EmarsysSdkPackageName]
        ),
        .library(
            name: emarsysNotificationServicePackageName,
            targets: [emarsysNotificationServicePackageName]
        ),
    ],
    targets: [
        .binaryTarget(
            name: EmarsysSdkPackageName,
            path: "./emarsys-sdk/build/XCFrameworks/debug/\(EmarsysSdkPackageName).xcframework"
        ),
        .binaryTarget(
            name: emarsysNotificationServicePackageName,
            path: "./ios-notification-service/build/XCFrameworks/debug/\(emarsysNotificationServicePackageName).xcframework"
        ),
    ]
)