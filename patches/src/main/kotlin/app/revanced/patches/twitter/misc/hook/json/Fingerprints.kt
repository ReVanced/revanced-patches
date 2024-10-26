package app.revanced.patches.twitter.misc.hook.json

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val jsonHookPatchFingerprint = fingerprint {
    opcodes(
        Opcode.INVOKE_INTERFACE, // Add dummy hook to hooks list.
        // Add hooks to the hooks list.
        Opcode.INVOKE_STATIC, // Call buildList.
    )
    custom { method, _ -> method.name == "<clinit>" }
}

internal val jsonInputStreamFingerprint = fingerprint {
    custom { method, _ ->
        if (method.parameterTypes.isEmpty()) {
            false
        } else {
            method.parameterTypes.first() == "Ljava/io/InputStream;"
        }
    }
}

internal val loganSquareFingerprint = fingerprint {
    custom { _, classDef -> classDef.endsWith("LoganSquare;") }
}
