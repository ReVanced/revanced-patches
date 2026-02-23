# GmsCore Support Builder

A helper function to simplify adding GmsCore support to Google apps.

## Purpose

This builder condenses the bytecode and resource patches into a single function call, reducing boilerplate code from ~80 lines to ~15 lines per app.

## Usage

```kotlin
val gmsCoreSupportPatch = gmsCoreSupportBuilder(
    fromPackageName = PHOTOS_PACKAGE_NAME,
    toPackageName = REVANCED_PHOTOS_PACKAGE_NAME,
    spoofedPackageSignature = "24bb24c05e47e0aefa68a58a766179d9b613a600",
    mainActivityOnCreateFingerprint = homeActivityOnCreateFingerprint,
    extensionPatch = extensionPatch,
) {
    compatibleWith(PHOTOS_PACKAGE_NAME)
}
```

## Parameters

- `fromPackageName`: The original package name (e.g., `com.google.android.apps.photos`)
- `toPackageName`: The ReVanced package name (e.g., `app.revanced.android.apps.photos`)
- `spoofedPackageSignature`: The app's original signature for GmsCore authentication
- `mainActivityOnCreateFingerprint`: Fingerprint for the main activity's onCreate method
- `extensionPatch`: The app's extension patch
- `primeMethodFingerprint`: (Optional) Fingerprint for the prime method
- `earlyReturnFingerprints`: (Optional) Set of fingerprints for methods that need early returns
- `executeBlock`: (Optional) Additional bytecode patch execution logic
- `block`: (Optional) Additional patch configuration (e.g., `compatibleWith()`)

## Finding the Signature

To find an app's signature:

1. Install the original app from Google Play
2. Run: `apksigner verify --print-certs app.apk | grep SHA1`
3. Use the SHA1 hash (lowercase, without colons)

## Example: Adding GmsCore Support to a New App

```kotlin
package app.revanced.patches.myapp.misc.gms

import app.revanced.patches.all.misc.gms.gmsCoreSupportBuilder
import app.revanced.patches.myapp.misc.extension.extensionPatch

private const val MY_APP_PACKAGE = "com.google.android.myapp"
private const val REVANCED_MY_APP_PACKAGE = "app.revanced.android.myapp"

val gmsCoreSupportPatch = gmsCoreSupportBuilder(
    fromPackageName = MY_APP_PACKAGE,
    toPackageName = REVANCED_MY_APP_PACKAGE,
    spoofedPackageSignature = "your_app_signature_here",
    mainActivityOnCreateFingerprint = mainActivityOnCreateFingerprint,
    extensionPatch = extensionPatch,
) {
    compatibleWith(MY_APP_PACKAGE)
}
```

## Benefits

- Reduces code duplication by 85-90%
- Consistent API across all apps
- Easier maintenance and updates
- Simpler to add GmsCore support to new apps
