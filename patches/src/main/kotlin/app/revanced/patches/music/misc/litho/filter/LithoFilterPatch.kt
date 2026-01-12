package app.revanced.patches.music.misc.litho.filter

import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.shared.conversionContextFingerprintToString
import app.revanced.patches.shared.misc.litho.filter.lithoFilterPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode

val lithoFilterPatch = lithoFilterPatch(
    componentCreateInsertionIndex = {
        // No supported version clobbers p2 so we can just do our things before the return instruction.
        indexOfFirstInstructionOrThrow(Opcode.RETURN_OBJECT)
    },
    conversionContextFingerprintToString = conversionContextFingerprintToString,
) {
    dependsOn(sharedExtensionPatch)
}