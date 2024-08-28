package app.revanced.patches.youtube.misc.fix.backtoexitgesture

import app.revanced.patcher.Match
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.matchOrThrow

@Suppress("unused")
internal val fixBackToExitGesturePatch = bytecodePatch(
    description = "Fixes the swipe back to exit gesture.",
) {
    val recyclerViewTopScrollingParentMatch by recyclerViewTopScrollingParentFingerprint()
    val recyclerViewScrollingMatch by recyclerViewScrollingFingerprint()
    val onBackPressedMatch by onBackPressedFingerprint()

    execute { context ->
        recyclerViewTopScrollingFingerprint.apply {
            match(context, recyclerViewTopScrollingParentMatch.classDef)
        }

        /**
         * Inject a call to a method from the extension.
         *
         * @param targetMethod The target method to call.
         */
        fun Match.injectCall(targetMethod: ExtensionMethod) = mutableMethod.addInstruction(
            patternMatch!!.endIndex,
            targetMethod.toString(),
        )

        mapOf(
            recyclerViewTopScrollingFingerprint.matchOrThrow() to ExtensionMethod(
                methodName = "onTopView",
            ),
            recyclerViewScrollingMatch to ExtensionMethod(
                methodName = "onScrollingViews",
            ),
            onBackPressedMatch to ExtensionMethod(
                "p0",
                "onBackPressed",
                "Landroid/app/Activity;",
            ),
        ).forEach { (match, target) -> match.injectCall(target) }
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
