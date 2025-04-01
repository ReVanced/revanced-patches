package app.revanced.patches.spotify.misc.voicesearch

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/misc/Misc;"

@Suppress("unused")
val bypassVoiceSearchRestrictions = bytecodePatch(
    name = "Bypass Voice Search Restrictions",
    description = "Enable playing the requested song/artist when asking it via Voice Search (Google Assistant and similar), rather it's station/radio",
) {
    compatibleWith("com.spotify.music")

    dependsOn(sharedExtensionPatch)

    execute {

        contextFromJsonFingerprint.method.apply {
            val insertIndex = contextFromJsonFingerprint.patternMatch!!.startIndex
            val registerUrl = getInstruction<FiveRegisterInstruction>(insertIndex).registerC
            val registerUri = getInstruction<FiveRegisterInstruction>(insertIndex + 2).registerD

            addInstructions(
                insertIndex,
                """
                invoke-static { v$registerUrl }, $EXTENSION_CLASS_DESCRIPTOR->removeStationString(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$registerUrl
                invoke-static { v$registerUri }, $EXTENSION_CLASS_DESCRIPTOR->removeStationString(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$registerUri
                """
            )
        }

        readPlayerOptionOverridesFingerprint.method.apply {
            val shufflingContextCallIndex = instructions.indexOfFirst { instruction ->
                if (instruction.opcode != Opcode.INVOKE_VIRTUAL) return@indexOfFirst false

                val invokeInstruction = instruction as Instruction35c
                val methodRef = invokeInstruction.reference as MethodReference

                if (methodRef.name != "shufflingContext") {
                    return@indexOfFirst false
                }
                true
            }

            val registerBool = getInstruction<FiveRegisterInstruction>(shufflingContextCallIndex).registerD
            addInstructions(
                shufflingContextCallIndex,
                """
                const/4 v${registerBool}, 0x0
                invoke-static {v${registerBool}}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                move-result-object v${registerBool}
                """
            )
        }
    }
}

