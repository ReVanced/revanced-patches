package app.revanced.patches.twitter.misc.hook.json.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val jsonHookPatchFingerprint = methodFingerprint {
    opcodes(
        Opcode.INVOKE_INTERFACE, // Add dummy hook to hooks list.
        // Add hooks to the hooks list.
        Opcode.INVOKE_STATIC // Call buildList.
    )
    custom { methodDef, _ -> methodDef.name == "<clinit>" }
}