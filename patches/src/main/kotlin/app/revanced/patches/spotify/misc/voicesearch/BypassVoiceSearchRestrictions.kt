package app.revanced.patches.spotify.misc.voicesearch

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/misc/UnlockPremiumPatch;"

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
            val registerUrl = getInstruction<OneRegisterInstruction>(insertIndex).registerA;
            val registerUri = getInstruction<TwoRegisterInstruction>(insertIndex + 2).registerB;

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
    }
}
