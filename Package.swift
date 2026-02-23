// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudSDK' (do not edit)
let remoteEngagementCloudSDKUrl = "https://maven.pkg.github.com/emartech/engagement-cloud-sdk/com/sap/engagement-cloud-sdk-kmmbridge/0.0.50/engagement-cloud-sdk-kmmbridge-0.0.50.zip"
let remoteEngagementCloudSDKChecksum = "6a3a1027a0fddb35fa349ccdc3323cb6f8a4feb544cf121836b0e4d84db3d0d9"
let engagementCloudSDKPackageName = "EngagementCloudSDK"
// END KMMBRIDGE BLOCK FOR 'EngagementCloudSDK'

// BEGIN KMMBRIDGE VARIABLES BLOCK FOR 'EngagementCloudNotificationService' (do not edit)
let remoteEngagementCloudNotificationServiceUrl = "https://maven.pkg.github.com/emartech/engagement-cloud-sdk/com/sap/ios-notification-service-kmmbridge/0.0.50/ios-notification-service-kmmbridge-0.0.50.zip"
let remoteEngagementCloudNotificationServiceChecksum = "248034d4aa263b12c245a54ea782b296ce3615ad92ad9a298c1af6eae9b77492"
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