package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.booleanPatchOption
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.youtube.layout.hide.shorts.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.navigation.NavigationBarHookPatch
import app.revanced.patches.youtube.misc.playservice.VersionCheckPatch
import app.revanced.util.forEachLiteralValueInstruction
import app.revanced.util.alsoResolve
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstWideLiteralInstructionValue
import app.revanced.util.indexOfIdResourceOrThrow
import app.revanced.util.injectHideViewCall
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Hide Shorts components",
    description = "Adds options to hide components related to YouTube Shorts.",
    dependencies = [
        IntegrationsPatch::class,
        LithoFilterPatch::class,
        HideShortsComponentsResourcePatch::class,
        ResourceMappingPatch::class,
        NavigationBarHookPatch::class,
        VersionCheckPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ],
        ),
    ],
)
@Suppress("unused")
object HideShortsComponentsPatch : BytecodePatch(
    setOf(
        CreateShortsButtonsFingerprint,
        ReelConstructorFingerprint,
        ShortsBottomBarContainerFingerprint,
        LegacyRenderBottomNavigationBarParentFingerprint,
        RenderBottomNavigationBarParentFingerprint,
        SetPivotBarVisibilityParentFingerprint,
    ),
) {
    private const val FILTER_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/components/ShortsFilter;"

    internal val hideShortsAppShortcut by booleanPatchOption(
        key = "hideShortsAppShortcut",
        default = false,
        title = "Hide Shorts app shortcut",
        description = "Permanently hides the shortcut to open Shorts when long pressing the app icon in your launcher."
    )

    internal val hideShortsWidget by booleanPatchOption(
        key = "hideShortsWidget",
        default = false,
        title = "Hide Shorts widget",
        description = "Permanently hides the launcher widget Shorts button."
    )

    override fun execute(context: BytecodeContext) {
        // region Hide the Shorts shelf.

        // This patch point is not present in 19.03.x and greater.
        if (!VersionCheckPatch.is_19_03_or_greater) {
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
            }
        }

        // endregion

        // region Hide the Shorts buttons in older versions of YouTube.

        // Some Shorts buttons are views, hide them by setting their visibility to GONE.
        CreateShortsButtonsFingerprint.resultOrThrow().let {
            ShortsButtons.entries.forEach { button -> button.injectHideCall(it.mutableMethod) }
        }

        // endregion

        // region Hide the Shorts buttons in newer versions of YouTube.

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)

        context.forEachLiteralValueInstruction(
            HideShortsComponentsResourcePatch.reelPlayerRightPivotV2Size
        ) { literalInstructionIndex ->
            val targetIndex = indexOfFirstInstructionOrThrow(literalInstructionIndex) {
                getReference<MethodReference>()?.name == "getDimensionPixelSize"
            } + 1

            val sizeRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

            addInstructions(targetIndex + 1, """
                    invoke-static { v$sizeRegister }, $FILTER_CLASS_DESCRIPTOR->getSoundButtonSize(I)I
                    move-result v$sizeRegister
                """
            )
        }

        // endregion

        // region Hide the navigation bar.

        // Hook to get the pivotBar view.
        SetPivotBarVisibilityFingerprint.alsoResolve(
            context,
            SetPivotBarVisibilityParentFingerprint
        ).let { result->
            result.mutableMethod.apply {
                val insertIndex = result.scanResult.patternScanResult!!.endIndex
                val viewRegister = getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA
                addInstruction(insertIndex, "invoke-static {v$viewRegister}," +
                        " $FILTER_CLASS_DESCRIPTOR->setNavigationBar(Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;)V"
                )
            }
        }

        // Hook to hide the shared navigation bar when the Shorts player is opened.
        RenderBottomNavigationBarFingerprint.alsoResolve(
            context,
            if (VersionCheckPatch.is_19_41_or_greater)
                RenderBottomNavigationBarParentFingerprint
            else
                LegacyRenderBottomNavigationBarParentFingerprint
        ).mutableMethod.addInstruction(
            0,
            "invoke-static { p1 }, $FILTER_CLASS_DESCRIPTOR->hideNavigationBar(Ljava/lang/String;)V"
        )

        // Hide the bottom bar container of the Shorts player.
        ShortsBottomBarContainerFingerprint.resultOrThrow().mutableMethod.apply {
            val resourceIndex = indexOfFirstWideLiteralInstructionValue(HideShortsComponentsResourcePatch.bottomBarContainer)

            val targetIndex = indexOfFirstInstructionOrThrow(resourceIndex) {
                getReference<MethodReference>()?.name == "getHeight"
            } + 1

            val heightRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

            addInstructions(targetIndex + 1, """
                    invoke-static { v$heightRegister }, $FILTER_CLASS_DESCRIPTOR->getNavigationBarHeight(I)I
                    move-result v$heightRegister
                """
            )
        }

        // endregion
    }

    private enum class ShortsButtons(private val resourceName: String, private val methodName: String) {
        LIKE("reel_dyn_like", "hideLikeButton"),
        DISLIKE("reel_dyn_dislike", "hideDislikeButton"),
        COMMENTS("reel_dyn_comment", "hideShortsCommentsButton"),
        REMIX("reel_dyn_remix", "hideShortsRemixButton"),
        SHARE("reel_dyn_share", "hideShortsShareButton");

        fun injectHideCall(method: MutableMethod) {
            val referencedIndex = method.indexOfIdResourceOrThrow(resourceName)

            val setIdIndex = method.indexOfFirstInstructionOrThrow(referencedIndex) {
                opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>()?.name == "setId"
            }

            val viewRegister = method.getInstruction<FiveRegisterInstruction>(setIdIndex).registerC

            method.injectHideViewCall(setIdIndex + 1, viewRegister, FILTER_CLASS_DESCRIPTOR, methodName)
        }
    }
}
