package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object ComponentContextParserFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_INTERFACE,
        Opcode.INVOKE_STATIC_RANGE
    ),
    strings = listOf("Component was not found %s because it was removed due to duplicate converter bindings.")
)