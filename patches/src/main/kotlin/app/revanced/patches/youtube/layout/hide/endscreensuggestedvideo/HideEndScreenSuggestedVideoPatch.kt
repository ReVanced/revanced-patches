package app.revanced.patches.youtube.layout.hide.endscreensuggestedvideo

import app.revanced.patcher.extensions.ExternalLabel
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

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
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    )

    apply {
        addResources("youtube", "layout.hide.endscreensuggestedvideo.hideEndScreenSuggestedVideoPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_end_screen_suggested_video"),
        )

        removeOnLayoutChangeListenerMethodMatch.let {
            val endScreenMethod = navigate(it.immutableMethod).to(it[-1]).stop()

            endScreenMethod.apply {
                val autoNavStatusMethodName =
                    autoNavConstructorMethod.immutableClassDef.getAutoNavStatusMethod().name

                val invokeIndex = indexOfFirstInstructionOrThrow {
                    val reference = methodReference
                    reference?.name == autoNavStatusMethodName &&
                            reference.returnType == "Z" &&
                            reference.parameterTypes.isEmpty()
                }

                val iGetObjectIndex =
                    indexOfFirstInstructionReversedOrThrow(invokeIndex, Opcode.IGET_OBJECT)
                val invokeReference = getInstruction<ReferenceInstruction>(invokeIndex).reference
                val iGetObjectReference =
                    getInstruction<ReferenceInstruction>(iGetObjectIndex).reference
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
                    ExternalLabel("show_end_screen_recommendation", getInstruction(0)),
                )
            }
        }
    }
}
