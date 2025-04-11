# KMP Emarsys SDK

The **KMP Emarsys SDK** is a Kotlin Multiplatform SDK designed to integrate with the Emarsys platform. It provides a unified interface for managing events, push notifications, and other functionalities across Android, iOS, and Web platforms.

## Features

- **Event Management**: Track and manage various SDK events.
- **Push Notifications**: Handle push notifications, including silent pushes and user interactions.
- **Cross-Platform Support**: Built using Kotlin Multiplatform, enabling seamless integration across Android, iOS, and Web platforms.

---

## Installation

### Android

The SDK is available on Maven Central. Add the following dependency to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("com.emarsys:emarsys-sdk-android:4.0.0")
}
```

Ensure you have the following permissions in your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

### iOS

The SDK is distributed via Swift Package Manager (SPM). Add the following to your `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/emartech/kmp-emarsys-sdk.git", from: "4.0.0")
]
```

Then, import the SDK in your Swift code:

```swift
import EmarsysSDK
```

### Web

The SDK is available on NPM. Install it using the following command:

```bash
npm install @emarsys/kmp-emarsys-sdk
```

Import and initialize the SDK in your JavaScript or TypeScript code:

```javascript
import { EmarsysJs } from "@emarsys/kmp-emarsys-sdk";

const emarsys = new EmarsysJs();
await emarsys.enableTracking({
    account: "YOUR_ACCOUNT_ID",
});
```

### Kotlin Multiplatform (KMP)

For KMP projects, the SDK is available on Maven Central. Add the following to your `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.emarsys:emarsys-sdk:4.0.0")
            }
        }
    }
}
```

---

## Usage
TBD

---

## License

This project is licensed under the [Mozilla Public License, Version 2.0](LICENSE).

---

## Support

For any issues or questions, please open an issue in the repository or contact the support team.

---

## Additional Resources

- [Emarsys Documentation](https://www.emarsys.com/)
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Swift Package Manager Documentation](https://swift.org/package-manager/)
- [NPM Documentation](https://www.npmjs.com/)
