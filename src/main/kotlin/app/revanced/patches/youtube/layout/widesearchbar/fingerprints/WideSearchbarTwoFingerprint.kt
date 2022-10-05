package app.revanced.patches.youtube.layout.widesearchbar.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.widesearchbar.annotations.WideSearchbarCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("wide-searchbar-methodtwo-fingerprint")
@WideSearchbarCompatibility
@Version("0.0.1")
object WideSearchbarTwoFingerprint : MethodFingerprint(
    "L", AccessFlags.PUBLIC or AccessFlags.STATIC, opcodes = listOf(
        Opcode.INVOKE_STATIC, Opcode.MOVE_RESULT, Opcode.IF_EQZ, Opcode.NEW_INSTANCE
    )
)