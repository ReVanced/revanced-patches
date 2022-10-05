package app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.sponsorblock.annotations.SponsorBlockCompatibility
import app.revanced.patches.youtube.layout.sponsorblock.resource.patch.SponsorBlockResourcePatch
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

@Name("shorts-player-constructor-fingerprint")
@FuzzyPatternScanMethod(3)
@SponsorBlockCompatibility
@Version("0.0.1")
object ShortsPlayerConstructorFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    opcodes = listOf(
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.INVOKE_DIRECT_RANGE,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.CONST_4
    ),
    customFingerprint = { methodDef ->
        methodDef.implementation?.instructions?.any { instruction ->
            (instruction as? WideLiteralInstruction)?.wideLiteral == SponsorBlockResourcePatch.reelButtonGroupResourceId
        } == true
    }
)