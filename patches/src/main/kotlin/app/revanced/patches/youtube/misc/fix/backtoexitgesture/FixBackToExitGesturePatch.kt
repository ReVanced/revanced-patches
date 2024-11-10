package app.revanced.patches.youtube.misc.fix.backtoexitgesture

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

internal val fixBackToExitGesturePatch = bytecodePatch(
    description = "Fixes the swipe back to exit gesture.",
) {

    execute {
        /**
         * Inject a call to a method from the extension.
         *
         * @param targetMethod The target method to call.
         */
        suspend fun Fingerprint.injectCall(targetMethod: ExtensionMethod) = method().addInstruction(
            patternMatch()!!.endIndex,
            targetMethod.toString(),
        )

        mapOf(
            recyclerViewTopScrollingFingerprint.also {
                it.match(recyclerViewTopScrollingParentFingerprint.originalClassDef())
            } to ExtensionMethod(
                methodName = "onTopView",
            ),
            recyclerViewScrollingFingerprint to ExtensionMethod(
                methodName = "onScrollingViews",
            ),
            onBackPressedFingerprint to ExtensionMethod(
                "p0",
                "onBackPressed",
                "Landroid/app/Activity;",
            ),
        ).forEach { (fingerprint, target) -> fingerprint.injectCall(target) }
    }
}

/**
 * A reference to a method from the extension for [fixBackToExitGesturePatch].
 *
 * @param register The method registers.
 * @param methodName The method name.
 * @param parameterTypes The parameters of the method.
 */
private class ExtensionMethod(
    val register: String = "",
    val methodName: String,
    val parameterTypes: String = "",
) {
    override fun toString() =
        "invoke-static {$register}, Lapp/revanced/extension/youtube/patches/FixBackToExitGesturePatch;->$methodName($parameterTypes)V"
}
