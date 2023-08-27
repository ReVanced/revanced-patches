package app.revanced.patches.twitter.misc.hook.json.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object JsonHookPatchFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ -> methodDef.name == "<clinit>" },
    opcodes = listOf(
        Opcode.INVOKE_INTERFACE, // Add dummy hook to hooks list.
        // Add hooks to the hooks list.
        Opcode.INVOKE_STATIC // Call buildList.
    )
)