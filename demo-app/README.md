# Tridentity SDK Demo App

Simple demo app for Tridentity SDK integration (no flavors).  
Package: `com.example.helloTridentity`.

## Firebase Push Notifications

The demo app includes Firebase Cloud Messaging for push notifications (transaction auth flow).

**Setup (reuse existing Firebase project):**

1. In your existing Firebase project, add an Android app with package name **`com.example.helloTridentity`** (or use the same `google-services.json` as the sample app if it already includes this package).
2. Download `google-services.json` and place it in the **`demo-app/`** folder (same level as `build.gradle`).

You can also copy `app/google-services.json` into `demo-app/` if that file already contains a client for `com.example.helloTridentity`. Without `google-services.json` in `demo-app/`, the build will fail when applying the Google services plugin.
