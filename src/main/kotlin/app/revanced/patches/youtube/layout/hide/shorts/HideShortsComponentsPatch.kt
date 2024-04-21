package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.youtube.layout.hide.shorts.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.navigation.NavigationBarHookPatch
import app.revanced.util.exception
import app.revanced.util.indexOfIdResourceOrThrow
import app.revanced.util.injectHideViewCall
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Hide Shorts components",
    description = "Adds options to hide components related to YouTube Shorts.",
    dependencies = [
        IntegrationsPatch::class,
        LithoFilterPatch::class,
        HideShortsComponentsResourcePatch::class,
        ResourceMappingPatch::class,
        NavigationBarHookPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43",
            ],
        ),
    ],
)
@Suppress("unused")
object HideShortsComponentsPatch : BytecodePatch(
    setOf(
        CreateShortsButtonsFingerprint,
        ReelConstructorFingerprint,
        BottomNavigationBarFingerprint,
        RenderBottomNavigationBarParentFingerprint,
        SetPivotBarVisibilityParentFingerprint,
    ),
) {
    private const val FILTER_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/components/ShortsFilter;"

    override fun execute(context: BytecodeContext) {
        // region Hide the Shorts shelf.

        // This patch point is not present in 19.03.x and greater.
        // If 19.02.x and lower is dropped, then this section of code and the fingerprint should be removed.
        ReelConstructorFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.startIndex + 2
                val viewRegister = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                injectHideViewCall(
                    insertIndex,
                    viewRegister,
                    FILTER_CLASS_DESCRIPTOR,
                    "hideShortsShelf",
                )
            }
        } // Do not throw an exception if not resolved.

        // endregion

        // region Hide the Shorts buttons in older versions of YouTube.

        // Some Shorts buttons are views, hide them by setting their visibility to GONE.
        CreateShortsButtonsFingerprint.result?.let {
            ShortsButtons.entries.forEach { button -> button.injectHideCall(it.mutableMethod) }
        } ?: throw CreateShortsButtonsFingerprint.exception

        // endregion

        // region Hide the Shorts buttons in newer versions of YouTube.

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)

        // endregion

        // region Hide the navigation bar.

        // Hook to get the pivotBar view.
        SetPivotBarVisibilityParentFingerprint.result?.let {
            if (!SetPivotBarVisibilityFingerprint.resolve(context, it.classDef)) {
                throw SetPivotBarVisibilityFingerprint.exception
            }

            SetPivotBarVisibilityFingerprint.result!!.let { result ->
                result.mutableMethod.apply {
                    val insertIndex = result.scanResult.patternScanResult!!.endIndex
                    val viewRegister = getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA
                    addInstruction(
                        insertIndex,
                        "sput-object v$viewRegister, $FILTER_CLASS_DESCRIPTOR->pivotBar:" +
                            "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;",
                    )
                }
            }
        } ?: throw SetPivotBarVisibilityParentFingerprint.exception

        // Hook to hide the navigation bar when Shorts are being played.
        RenderBottomNavigationBarParentFingerprint.result?.let {
            if (!RenderBottomNavigationBarFingerprint.resolve(context, it.classDef)) {
                throw RenderBottomNavigationBarFingerprint.exception
            }

            RenderBottomNavigationBarFingerprint.result!!.mutableMethod.apply {
                addInstruction(0, "invoke-static { }, $FILTER_CLASS_DESCRIPTOR->hideNavigationBar()V")
            }
        } ?: throw RenderBottomNavigationBarParentFingerprint.exception

        // Required to prevent a black bar from appearing at the bottom of the screen.
        BottomNavigationBarFingerprint.result?.let {
            it.mutableMethod.apply {
                val moveResultIndex = it.scanResult.patternScanResult!!.startIndex + 2
                val viewRegister = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA
                val insertIndex = moveResultIndex + 1

                addInstruction(
                    insertIndex,
                    "invoke-static { v$viewRegister }, $FILTER_CLASS_DESCRIPTOR->" +
                        "hideNavigationBar(Landroid/view/View;)Landroid/view/View;",
                )
            }
        } ?: throw BottomNavigationBarFingerprint.exception

        // endregion
    }

    private enum class ShortsButtons(private val resourceName: String, private val methodName: String) {
        COMMENTS("reel_dyn_comment", "hideShortsCommentsButton"),
        REMIX("reel_dyn_remix", "hideShortsRemixButton"),
        SHARE("reel_dyn_share", "hideShortsShareButton"),
        ;

        fun injectHideCall(method: MutableMethod) {
            val referencedIndex = method.indexOfIdResourceOrThrow(resourceName)

            val setIdIndex = referencedIndex + 1
            val viewRegister = method.getInstruction<FiveRegisterInstruction>(setIdIndex).registerC
            method.injectHideViewCall(setIdIndex, viewRegister, FILTER_CLASS_DESCRIPTOR, methodName)
        }
    }
}
