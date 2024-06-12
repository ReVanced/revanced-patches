package app.revanced.patches.youtube.misc.fix.backtoexitgesture

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.resultOrThrow

@Suppress("unused")
internal val fixBackToExitGesturePatch = bytecodePatch(
    description = "Fixes the swipe back to exit gesture.",
) {
    val recyclerViewTopScrollingParentFingerprintResult by recyclerViewTopScrollingParentFingerprint
    val recyclerViewScrollingFingerprintResult by recyclerViewScrollingFingerprint
    val onBackPressedFingerprintResult by onBackPressedFingerprint

    execute { context ->
        recyclerViewTopScrollingFingerprint.apply {
            resolve(context, recyclerViewTopScrollingParentFingerprintResult.classDef)
        }

        /**
         * Inject a call to a method from the integrations.
         *
         * @param targetMethod The target method to call.
         */
        fun MethodFingerprintResult.injectCall(targetMethod: IntegrationsMethod) = mutableMethod.addInstruction(
            scanResult.patternScanResult!!.endIndex,
            targetMethod.toString(),
        )

        mapOf(
            recyclerViewTopScrollingFingerprint.resultOrThrow() to IntegrationsMethod(
                methodName = "onTopView",
            ),
            recyclerViewScrollingFingerprintResult to IntegrationsMethod(
                methodName = "onScrollingViews",
            ),
            onBackPressedFingerprintResult to IntegrationsMethod(
                "p0",
                "onBackPressed",
                "Landroid/app/Activity;",
            ),
        ).forEach { (result, target) -> result.injectCall(target) }
    }
}

/**
 * A reference to a method from the integrations for [fixBackToExitGesturePatch].
 *
 * @param register The method registers.
 * @param methodName The method name.
 * @param parameterTypes The parameters of the method.
 */
private class IntegrationsMethod(
    val register: String = "",
    val methodName: String,
    val parameterTypes: String = "",
) {
    override fun toString() =
        "invoke-static {$register}, Lapp/revanced/integrations/youtube/patches/FixBackToExitGesturePatch;->$methodName($parameterTypes)V"
}
