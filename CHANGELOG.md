# Changelog

## [4.0.3]

### Fixed

- Setup: `InvalidApplicationCodeException` is now handled gracefully instead of crashing.
- Setup: Calling `disable()` when the SDK is already disabled no longer throws a `SdkAlreadyDisabledException`.
- Custom event: Tracking an event with a blank name now returns a validation error instead of sending a malformed request.
- Contact: Session is now correctly restarted in the contact client after any successful contact-linking operation.
- Embedded Messaging: Loading additional pages in the JS list view now works reliably.
- Embedded Messaging: Detailed messages are now scrollable on iOS.
- Web push: Service worker registration is now correctly fetched from `serviceWorkerContainer`, fixing web push on some browsers.
- Device info is now cleared when `disable()` is called, preventing stale data from leaking into subsequent sessions.


The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).