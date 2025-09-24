package app.revanced.patches.youtube.layout.hide.viewcount

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.Opcode 
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow


private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/HideViewCountPatch;"

@Suppress("unused")
val hideViewCountPatch = bytecodePatch(
    name = "Hide View Count",
    description = "Hide the view count from the video feed list.",
) {
    dependsOn(
        settingsPatch,
        addResourcesPatch,
    ) 

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "19.43.41",
            "19.47.53", 
            "20.07.39",
            "20.12.46", 
            "20.13.41",
        )
    ) 

    execute {
        addResources("youtube", "layout.hide.viewcount.hideViewCountPatch")

        PreferenceScreen.FEED.addPreferences(
            SwitchPreference("revanced_hide_view_count"),
        )

        hideViewCountFingerprint.method.apply {

            val startIndex = hideViewCountFingerprint.patternMatch!!.startIndex
            var returnStringRegister = getInstruction<OneRegisterInstruction>(startIndex).registerA

            var instructions = implementation!!.instructions.toMutableList();
            
            // Find the instruction where the text dimension is retrieved.
            val injectPointRegisterIndex = instructions.indexOfLast { instruction ->
                instruction.opcode == Opcode.INVOKE_STATIC &&
                    instruction.getReference<MethodReference>()?.let { methodRef ->
                        methodRef.definingClass == "Landroid/util/TypedValue;" &&
                        methodRef.name == "applyDimension" &&
                        methodRef.parameterTypes == listOf("I", "F", "Landroid/util/DisplayMetrics;") &&
                        methodRef.returnType == "F"
                    } == true
            }
            // A float value is passed which is used to determine subtitle text size.
            val floatDimensionRegister = getInstruction<OneRegisterInstruction>(injectPointRegisterIndex+1).registerA

            addInstructions(
                injectPointRegisterIndex-1,
                """
                    invoke-static {v$returnStringRegister, v$floatDimensionRegister}, $EXTENSION_CLASS_DESCRIPTOR->hideViewCount(Landroid/text/SpannableString;F)Landroid/text/SpannableString;
                    move-result-object v$returnStringRegister
                """
            )
        }
    }
}