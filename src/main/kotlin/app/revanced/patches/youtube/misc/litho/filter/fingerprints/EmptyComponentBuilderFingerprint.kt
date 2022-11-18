package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.litho.filter.annotation.LithoFilterCompatibility
import org.jf.dexlib2.Opcode

@Name("empty-component-builder-fingerprint")
@LithoFilterCompatibility
@Version("0.0.1")
object EmptyComponentBuilderFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_STATIC_RANGE
    ),
)