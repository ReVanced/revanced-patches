package app.revanced.patches.twitter.misc.hook.json.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object JsonHookPatchFingerprint : MethodFingerprint(
    customFingerprint = { methodDef -> methodDef.name == "<clinit>" },
    opcodes = listOf(Opcode.IGET_OBJECT)
)