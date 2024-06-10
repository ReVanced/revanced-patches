package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.youtube.layout.hide.shorts.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.navigation.navigationBarHookPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val FILTER_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/components/ShortsFilter;"

@Suppress("unused")
val hideShortsComponentsPatch = bytecodePatch(
    name = "Hide Shorts components",
    description = "Adds options to hide components related to YouTube Shorts.",
) {
    dependsOn(
        integrationsPatch,
        lithoFilterPatch,
        hideShortsComponentsResourcePatch,
        resourceMappingPatch,
        navigationBarHookPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val createShortsButtonsResult by createShortsButtonsFingerprint
    val bottomNavigationBarResult by bottomNavigationBarFingerprint
    val renderBottomNavigationBarParentResult by renderBottomNavigationBarParentFingerprint
    val setPivotBarVisibilityParentResult by setPivotBarVisibilityParentFingerprint
    reelConstructorFingerprint()

    execute { context ->
        // region Hide the Shorts shelf.

        // This patch point is not present in 19.03.x and greater.
        // If 19.02.x and lower is dropped, then this section of code and the fingerprint should be removed.
        reelConstructorFingerprint.result?.let {
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
        ShortsButtons.entries.forEach { button -> button.injectHideCall(createShortsButtonsResult.mutableMethod) }

        // endregion

        // region Hide the Shorts buttons in newer versions of YouTube.

        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        // endregion

        // region Hide the navigation bar.

        // Hook to get the pivotBar view.
        if (!setPivotBarVisibilityFingerprint.resolve(context, setPivotBarVisibilityParentResult.classDef)) {
            throw setPivotBarVisibilityFingerprint.exception
        }

        setPivotBarVisibilityFingerprint.resultOrThrow().let { result ->
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

        // Hook to hide the navigation bar when Shorts are being played.
        if (!renderBottomNavigationBarFingerprint.resolve(context, renderBottomNavigationBarParentResult.classDef)) {
            throw renderBottomNavigationBarFingerprint.exception
        }

        renderBottomNavigationBarFingerprint.resultOrThrow().mutableMethod.apply {
            addInstruction(0, "invoke-static { }, $FILTER_CLASS_DESCRIPTOR->hideNavigationBar()V")
        }

        // Required to prevent a black bar from appearing at the bottom of the screen.
        bottomNavigationBarResult.mutableMethod.apply {
            val moveResultIndex = bottomNavigationBarResult.scanResult.patternScanResult!!.startIndex + 2
            val viewRegister = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA
            val insertIndex = moveResultIndex + 1

            addInstruction(
                insertIndex,
                "invoke-static { v$viewRegister }, $FILTER_CLASS_DESCRIPTOR->" +
                    "hideNavigationBar(Landroid/view/View;)Landroid/view/View;",
            )
        }

        // endregion
    }
}

private enum class ShortsButtons(private val resourceName: String, private val methodName: String) {
    LIKE("reel_dyn_like", "hideLikeButton"),
    DISLIKE("reel_dyn_dislike", "hideDislikeButton"),
    COMMENTS("reel_dyn_comment", "hideShortsCommentsButton"),
    REMIX("reel_dyn_remix", "hideShortsRemixButton"),
    SHARE("reel_dyn_share", "hideShortsShareButton"),
    ;

    fun injectHideCall(method: MutableMethod) {
        val referencedIndex = method.indexOfIdResourceOrThrow(resourceName)

        val instruction = method.implementation!!.instructions
            .subList(referencedIndex, referencedIndex + 20)
            .first {
                it.opcode == Opcode.INVOKE_VIRTUAL && it.getReference<MethodReference>()?.name == "setId"
            }

        val setIdIndex = instruction.location.index
        val viewRegister = method.getInstruction<FiveRegisterInstruction>(setIdIndex).registerC
        method.injectHideViewCall(setIdIndex + 1, viewRegister, FILTER_CLASS_DESCRIPTOR, methodName)
    }
}
