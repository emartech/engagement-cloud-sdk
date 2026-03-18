# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.0.0] - 2026-03-18

First public release of the **SAP Engagement Cloud SDK** — the next-generation, Kotlin Multiplatform SDK that replaces the SAP Emarsys SDKs for Android, iOS, and Web.

### Highlights

- **One SDK, all platforms.** A single Kotlin Multiplatform codebase powers Android, iOS (via XCFramework + SPM), and Web (via JavaScript/TypeScript). Integration patterns, API naming, and behavior are consistent across every platform.
- **Two-phase initialization.** `initialize()` (automatic on Android and Web) prepares the SDK without network traffic. `setup.enable(config)` starts data flow only after marketing consent is obtained — giving full control over when tracking begins.
- **First-party data collection on Web.** The Web target collects behavioral data through first-party mechanisms, avoiding the limitations of third-party cookies and ad blockers for more accurate personalization.
- **Redesigned architecture.** Event-driven internals with a state-machine core deliver measurable performance gains — for example, in-app messages appear faster than in the SAP Emarsys SDK.

### Added

- **Setup & Lifecycle**
  - `EngagementCloud.setup.enable(config)` — start tracking with consent-aware two-phase flow
  - `EngagementCloud.setup.disable()` — stop all tracking at any time (replaces the `changeApplicationCode(null)` workaround)
  - `EngagementCloud.setup.setOnContactLinkingFailedCallback(callback)` — handle contact linking failures with a retry prompt
  - Automatic initialization on Android (via AndroidX Startup) and Web (via loader script)

- **Contact Management**
  - `contact.link(contactFieldValue)` — identify a contact (replaces `setContact`)
  - `contact.linkAuthenticated(openIdToken)` — authenticate via OpenID (replaces `setAuthenticatedContact`)
  - `contact.unlink()` — clear the current contact (replaces `clearContact`)

- **Push Notifications**
  - `push.registerToken(token)` — register a push token (replaces `setPushToken`)
  - `push.clearToken()` — unregister (replaces `clearPushToken`)
  - `push.getToken()` — retrieve the current token
  - Rich push support: images, action buttons, badge count, collapse/replace behavior
  - Android: FCM and HMS push provider modules (`engagement-cloud-sdk-android-fcm`, `engagement-cloud-sdk-android-hms`)
  - iOS: Notification Service Extension for media attachments (`EngagementCloudSDKNotificationService` XCFramework)
  - Web: VAPID-based web push with service worker

- **Event Tracking**
  - `event.track(CustomEvent(name, attributes))` — track custom events with typed models (replaces `trackCustomEvent`)

- **In-App Messaging**
  - Overlay in-app messages with automatic display management
  - Inline in-app via `InlineInAppView` (Android/KMP) and `InlineInAppViewController` (iOS) with `onLoaded` / `onClose` callbacks
  - `inApp.pause()` / `inApp.resume()` / `inApp.isPaused` — control in-app display

- **Embedded Messaging**
  - Message inbox with paging support (replaces the legacy `MessageInbox` API)
  - Composable/SwiftUI-ready embedded message views

- **Deep Link Tracking**
  - `deepLink.track(activity, intent)` (Android) / `deepLink.track(userActivity:)` (iOS) / `deepLink.track(url)` (Web/KMP)

- **Reactive Event Stream**
  - `EngagementCloud.events` — a unified reactive stream (`Flow` on Kotlin, `AsyncSequence` on Swift, `EventEmitter` on Web) emitting `AppEvent`, `BadgeCountEvent`, and other SDK events. Replaces per-feature `onEventAction` handler registrations.

### Changed (vs. SAP Emarsys SDK)

- **Maven coordinates**: `com.emarsys:emarsys-sdk` → `com.sap.engagement-cloud:engagement-cloud-sdk`
- **iOS distribution**: CocoaPods → Swift Package Manager
- **API style**: Completion-listener callbacks → Kotlin `suspend` functions returning `Result`, Swift `async throws`, JavaScript `Promise`
- **Contact API naming**: `setContact` / `clearContact` → `link` / `unlink`
- **Push API naming**: `setPushToken` / `clearPushToken` → `registerToken` / `clearToken`
- **Event handling**: Per-feature handler interfaces → unified `EngagementCloud.events` reactive stream
- **Inline in-app callbacks**: `onCompletion` / `onEvent` → `onLoaded` (app events via `EngagementCloud.events` stream)

### Known Limitations

The following SAP Emarsys SDK features are not yet available:

- Geofencing (enable/disable, events, registered geofences)
- Web Extend events (cart, purchase, item view, category view, search term, tag)
- Product recommendations, variants, and filters (Predict)

For migration guidance, see the [Migration Guide](https://emartech.github.io/engagement-cloud-sdk/docs/migrationGuide/overview).
