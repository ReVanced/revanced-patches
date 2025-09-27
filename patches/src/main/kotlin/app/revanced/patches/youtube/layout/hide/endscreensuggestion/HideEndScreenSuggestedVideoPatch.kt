package app.revanced.patches.youtube.layout.hide.endscreensuggestion

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/HideEndScreenSuggestedVideoPatch;"

@Suppress("unused")
val hideEndScreenSuggestedVideoPatch = bytecodePatch(
    name = "Hide end screen suggested video",
    description = "Adds an option to hide the suggested video at the end of videos.",
) {
    dependsOn(
        sharedExtensionPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "layout.hide.endscreensuggestion.hideEndScreenSuggestedVideoPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_end_screen_suggested_video"),
        )

        removeOnLayoutChangeListenerFingerprint.let {
            val endScreenMethod = navigate(it.originalMethod).to(it.patternMatch!!.endIndex).stop()

            endScreenMethod.apply {
                val autoNavStatusMethodName = autoNavStatusFingerprint.match(
                    autoNavConstructorFingerprint.classDef
                ).originalMethod.name

                val invokeIndex = indexOfFirstInstructionOrThrow {
                    val reference = getReference<MethodReference>()
                    reference?.name == autoNavStatusMethodName &&
                            reference.returnType == "Z" &&
                            reference.parameterTypes.isEmpty()
                }
                val iGetObjectIndex = indexOfFirstInstructionReversedOrThrow(invokeIndex, Opcode.IGET_OBJECT)
                val invokeReference = getInstruction<ReferenceInstruction>(invokeIndex).reference
                val iGetObjectReference = getInstruction<ReferenceInstruction>(iGetObjectIndex).reference
                val opcodeName = getInstruction(invokeIndex).opcode.name

                addInstructionsWithLabels(
                    0,
                    """
                        invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->hideEndScreenSuggestedVideo()Z
                        move-result v0
                        if-eqz v0, :show_end_screen_recommendation

                        iget-object v0, p0, $iGetObjectReference

                        # This reference checks whether autoplay is turned on.
                        $opcodeName { v0 }, $invokeReference
                        move-result v0

                        # Hide suggested video end screen only when autoplay is turned off.
                        if-nez v0, :show_end_screen_recommendation
                        return-void
                    """,
                    ExternalLabel("show_end_screen_recommendation", getInstruction(0))
                )
            }
        }
    }
}
