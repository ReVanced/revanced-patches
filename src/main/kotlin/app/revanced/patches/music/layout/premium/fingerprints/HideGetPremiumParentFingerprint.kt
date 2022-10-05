package app.revanced.patches.music.layout.premium.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.music.layout.premium.annotations.HideGetPremiumCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("hide-get-premium-parent-fingerprint")
@HideGetPremiumCompatibility
@Version("0.0.1")
object HideGetPremiumParentFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf(), listOf(
        Opcode.IGET_BOOLEAN,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC
    ),
    listOf("FEmusic_history"),
)
