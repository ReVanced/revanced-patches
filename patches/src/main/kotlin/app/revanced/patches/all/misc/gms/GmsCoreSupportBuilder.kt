package app.revanced.patches.all.misc.gms

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.Patch
import app.revanced.patches.shared.misc.gms.gmsCoreSupportPatch
import app.revanced.patches.shared.misc.gms.gmsCoreSupportResourcePatch

/**
 * Builder function to simplify creating GmsCore support patches for Google apps.
 * 
 * This condenses the bytecode and resource patches into a single builder call,
 * reducing boilerplate code from ~80 lines to ~15 lines per app.
 * 
 * ## Purpose
 * 
 * This builder eliminates the need to manually create a separate resource patch factory function,
 * making it easier to add GmsCore support to new apps while maintaining consistency across implementations.
 * 
 * ## Usage
 * 
 * ```kotlin
 * val gmsCoreSupportPatch = gmsCoreSupportBuilder(
 *     fromPackageName = PHOTOS_PACKAGE_NAME,
 *     toPackageName = REVANCED_PHOTOS_PACKAGE_NAME,
 *     spoofedPackageSignature = "24bb24c05e47e0aefa68a58a766179d9b613a600",
 *     mainActivityOnCreateFingerprint = homeActivityOnCreateFingerprint,
 *     extensionPatch = extensionPatch,
 * ) {
 *     compatibleWith(PHOTOS_PACKAGE_NAME)
 * }
 * ```
 * 
 * ## Finding the Signature
 * 
 * To find an app's signature for the `spoofedPackageSignature` parameter:
 * 1. Install the original app from Google Play
 * 2. Run: `apksigner verify --print-certs app.apk | grep SHA1`
 * 3. Use the SHA1 hash (lowercase, without colons)
 * 
 * ## Example: Adding GmsCore Support to a New App
 * 
 * ```kotlin
 * package app.revanced.patches.myapp.misc.gms
 * 
 * import app.revanced.patches.all.misc.gms.gmsCoreSupportBuilder
 * import app.revanced.patches.myapp.misc.extension.extensionPatch
 * 
 * private const val MY_APP_PACKAGE = "com.google.android.myapp"
 * private const val REVANCED_MY_APP_PACKAGE = "app.revanced.android.myapp"
 * 
 * val gmsCoreSupportPatch = gmsCoreSupportBuilder(
 *     fromPackageName = MY_APP_PACKAGE,
 *     toPackageName = REVANCED_MY_APP_PACKAGE,
 *     spoofedPackageSignature = "your_app_signature_here",
 *     mainActivityOnCreateFingerprint = mainActivityOnCreateFingerprint,
 *     extensionPatch = extensionPatch,
 * ) {
 *     compatibleWith(MY_APP_PACKAGE)
 * }
 * ```
 * 
 * @param fromPackageName The original package name (e.g., `com.google.android.apps.photos`)
 * @param toPackageName The ReVanced package name (e.g., `app.revanced.android.apps.photos`)
 * @param spoofedPackageSignature The app's original signature for GmsCore authentication
 * @param mainActivityOnCreateFingerprint Fingerprint for the main activity's onCreate method
 * @param extensionPatch The app's extension patch
 * @param primeMethodFingerprint (Optional) Fingerprint for the prime method
 * @param earlyReturnFingerprints (Optional) Set of fingerprints for methods that need early returns
 * @param executeBlock (Optional) Additional bytecode patch execution logic
 * @param block (Optional) Additional patch configuration (e.g., `compatibleWith()`)
 * 
 * @return A complete GmsCore support patch for the app
 */
fun gmsCoreSupportBuilder(
    fromPackageName: String,
    toPackageName: String,
    spoofedPackageSignature: String,
    mainActivityOnCreateFingerprint: Fingerprint,
    extensionPatch: Patch<*>,
    primeMethodFingerprint: Fingerprint? = null,
    earlyReturnFingerprints: Set<Fingerprint> = emptySet(),
    executeBlock: BytecodePatchContext.() -> Unit = {},
    block: app.revanced.patcher.patch.BytecodePatchBuilder.() -> Unit = {},
) = gmsCoreSupportPatch(
    fromPackageName = fromPackageName,
    toPackageName = toPackageName,
    primeMethodFingerprint = primeMethodFingerprint,
    earlyReturnFingerprints = earlyReturnFingerprints,
    mainActivityOnCreateFingerprint = mainActivityOnCreateFingerprint,
    extensionPatch = extensionPatch,
    gmsCoreSupportResourcePatchFactory = { gmsCoreVendorGroupIdOption ->
        gmsCoreSupportResourcePatch(
            fromPackageName = fromPackageName,
            toPackageName = toPackageName,
            spoofedPackageSignature = spoofedPackageSignature,
            gmsCoreVendorGroupIdOption = gmsCoreVendorGroupIdOption,
        )
    },
    executeBlock = executeBlock,
    block = block,
)
