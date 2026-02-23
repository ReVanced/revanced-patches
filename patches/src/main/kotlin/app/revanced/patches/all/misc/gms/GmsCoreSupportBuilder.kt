package app.revanced.patches.all.misc.gms

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.Option
import app.revanced.patcher.patch.Patch
import app.revanced.patches.shared.misc.gms.gmsCoreSupportPatch

/**
 * Builder function to simplify creating GmsCore support patches for Google apps.
 * 
 * This condenses the bytecode and resource patches into a single builder call,
 * reducing boilerplate code from ~80 lines to ~15 lines per app.
 * 
 * @param fromPackageName The original package name of the app
 * @param toPackageName The target ReVanced package name
 * @param spoofedPackageSignature The app's original signature for spoofing
 * @param mainActivityOnCreateFingerprint Fingerprint for main activity onCreate method
 * @param extensionPatch The app's extension patch
 * @param primeMethodFingerprint Fingerprint for prime method (optional)
 * @param earlyReturnFingerprints Set of fingerprints for methods that need early returns
 * @param executeBlock Additional execution block for the bytecode patch
 * @param block Additional configuration block for the patch
 * 
 * @return A complete GmsCore support patch for the app
 * 
 * Example usage:
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
        createGmsCoreSupportResourcePatch(
            fromPackageName = fromPackageName,
            toPackageName = toPackageName,
            spoofedPackageSignature = spoofedPackageSignature,
            gmsCoreVendorGroupIdOption = gmsCoreVendorGroupIdOption,
        )
    },
    executeBlock = executeBlock,
    block = block,
)

/**
 * Internal helper to create the resource patch.
 */
private fun createGmsCoreSupportResourcePatch(
    fromPackageName: String,
    toPackageName: String,
    spoofedPackageSignature: String,
    gmsCoreVendorGroupIdOption: Option<String>,
) = app.revanced.patches.shared.misc.gms.gmsCoreSupportResourcePatch(
    fromPackageName = fromPackageName,
    toPackageName = toPackageName,
    spoofedPackageSignature = spoofedPackageSignature,
    gmsCoreVendorGroupIdOption = gmsCoreVendorGroupIdOption,
)
