package app.revanced.patches.youtube.ad.general.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.ad.general.annotation.GeneralAdsCompatibility
import app.revanced.patches.youtube.ad.general.resource.patch.GeneralAdsResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

@Name("reel-constructor-fingerprint")
@GeneralAdsCompatibility
@Version("0.0.1")
object ReelConstructorFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL
    ),
    customFingerprint = { method ->
        method.implementation?.instructions?.any {
            it.opcode == Opcode.CONST && (it as WideLiteralInstruction).wideLiteral == GeneralAdsResourcePatch.reelMultipleItemShelfId
        } ?: false
    }
)