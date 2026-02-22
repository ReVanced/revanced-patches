package app.revanced.patches.all.misc.gms

/**
 * Example usage of the Universal GmsCore Support Patch.
 * 
 * This file demonstrates how to use the universal patch for different scenarios.
 * Copy and adapt these examples for your specific app.
 */

// Example 1: Simple app with minimal requirements
// ================================================
// For a simple Google app that just needs basic GmsCore support:
//
// @Suppress("unused")
// val myAppGmsCoreSupportPatch = createUniversalGmsCoreSupportPatch(
//     fromPackageName = "com.google.android.apps.myapp",
//     extensionPatch = myAppExtensionPatch,
// )

// Example 2: App with custom package name
// ========================================
// If you want to specify a custom ReVanced package name:
//
// @Suppress("unused")
// val myAppGmsCoreSupportPatch = createUniversalGmsCoreSupportPatch(
//     fromPackageName = "com.google.android.apps.myapp",
//     toPackageName = "app.revanced.myapp.custom",
//     extensionPatch = myAppExtensionPatch,
// )

// Example 3: App with additional dependencies
// ============================================
// For apps that need additional patches (like hiding cast buttons):
//
// @Suppress("unused")
// val myAppGmsCoreSupportPatch = createUniversalGmsCoreSupportPatch(
//     fromPackageName = "com.google.android.apps.myapp",
//     extensionPatch = myAppExtensionPatch,
//     additionalDependencies = setOf(
//         hideCastButtonPatch,
//         spoofVideoStreamsPatch,
//     ),
// )

// Example 4: Complete implementation for a new app
// =================================================
// Here's a complete example for adding GmsCore support to a new app:
//
// package app.revanced.patches.myapp.misc.gms
//
// import app.revanced.patches.all.misc.gms.createUniversalGmsCoreSupportPatch
// import app.revanced.patches.myapp.misc.extension.sharedExtensionPatch
//
// // First, add your app's signature to SignatureRegistry.kt:
// // "com.google.android.apps.myapp" to "your_signature_here"
//
// @Suppress("unused")
// val gmsCoreSupportPatch = createUniversalGmsCoreSupportPatch(
//     fromPackageName = "com.google.android.apps.myapp",
//     extensionPatch = sharedExtensionPatch,
// ) {
//     // Optional: Add compatibility information
//     compatibleWith("com.google.android.apps.myapp"("1.0.0", "2.0.0"))
// }

// Example 5: Migrating from app-specific patch
// =============================================
// If you have an existing app-specific patch, migration is easy:
//
// BEFORE (app-specific):
// ----------------------
// val gmsCoreSupportPatch = gmsCoreSupportPatch(
//     fromPackageName = MY_APP_PACKAGE_NAME,
//     toPackageName = REVANCED_MY_APP_PACKAGE_NAME,
//     primeMethodFingerprint = primeMethodFingerprint,
//     earlyReturnFingerprints = setOf(castContextFetchFingerprint),
//     mainActivityOnCreateFingerprint = mainActivityOnCreateFingerprint,
//     extensionPatch = sharedExtensionPatch,
//     gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
// ) {
//     dependsOn(additionalPatch1, additionalPatch2)
// }
//
// AFTER (universal):
// ------------------
// // 1. Add signature to SignatureRegistry.kt
// // 2. Use the universal helper:
// val gmsCoreSupportPatch = createUniversalGmsCoreSupportPatch(
//     fromPackageName = MY_APP_PACKAGE_NAME,
//     toPackageName = REVANCED_MY_APP_PACKAGE_NAME,
//     extensionPatch = sharedExtensionPatch,
//     additionalDependencies = setOf(additionalPatch1, additionalPatch2),
// )

/**
 * Steps to add GmsCore support to a new app:
 * 
 * 1. Find your app's signature (use apksigner or similar tool)
 * 2. Add it to SignatureRegistry.kt:
 *    "com.google.android.apps.yourapp" to "your_signature_here"
 * 
 * 3. Create a patch file in your app's directory:
 *    patches/src/main/kotlin/app/revanced/patches/yourapp/misc/gms/GmsCoreSupportPatch.kt
 * 
 * 4. Use the universal helper:
 *    @Suppress("unused")
 *    val gmsCoreSupportPatch = createUniversalGmsCoreSupportPatch(
 *        fromPackageName = "com.google.android.apps.yourapp",
 *        extensionPatch = yourAppExtensionPatch,
 *    )
 * 
 * 5. Test with ReVanced Manager or CLI
 * 
 * That's it! The universal patch handles:
 * - Package name transformation
 * - Permission updates
 * - Provider authority changes
 * - Signature spoofing
 * - GmsCore verification
 * - Vendor selection
 */
