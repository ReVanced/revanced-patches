package app.revanced.patches.youtube.layout.minimizedplayback.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.minimizedplayback.annotations.MinimizedPlaybackCompatibility
import app.revanced.patches.youtube.misc.mapping.patch.ResourceIdMappingProviderResourcePatch
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

@Name("minimized-playback-manager-fingerprint")
@MatchingMethod(
    "Ladj", "w"
)
@FuzzyPatternScanMethod(2)
@MinimizedPlaybackCompatibility
@Version("0.0.1")
object MinimizedPlaybackSettingsFingerprint : MethodFingerprint(
    "L",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    null,
    listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
    ),
    customFingerprint = {
        it.implementation!!.instructions.any {
            (it as? WideLiteralInstruction)?.wideLiteral == resourceId 
        }
    }
)

val resourceId = ResourceIdMappingProviderResourcePatch.resourceMappings.first { it.type == "string" && it.name == "pref_background_category" }.id
