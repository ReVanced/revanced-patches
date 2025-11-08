package app.revanced.patches.youtube.misc.fix.contentprovider

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/FixContentProviderPatch;"

/**
 * Fixes crashing for some users with a beta release where the YouTube content provider uses null map values.
 * It unknown if this crash can happen on stable releases.
 */
internal val fixContentProviderPatch = bytecodePatch{
    dependsOn(
        sharedExtensionPatch
    )

    execute {
        unstableContentProviderFingerprint.let {
            val insertIndex = it.instructionMatches.first().index

            it.method.apply {
                val register = getInstruction<FiveRegisterInstruction>(insertIndex).registerD

                it.method.addInstruction(
                    insertIndex,
                    "invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->removeNullMapEntries(Ljava/util/Map;)V"
                )
            }
        }
    }
}