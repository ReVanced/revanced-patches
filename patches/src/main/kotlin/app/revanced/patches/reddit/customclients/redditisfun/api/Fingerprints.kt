package app.revanced.patches.reddit.customclients.redditisfun.api

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal fun baseClientIdFingerprint(string: String) = fingerprint {
    strings("yyOCBp.RHJhDKd", string)
}

internal val basicAuthorizationFingerprint = baseClientIdFingerprint(
    string = "fJOxVwBUyo*=f:<OoejWs:AqmIJ", // Encrypted basic authorization string.
)

internal val buildAuthorizationStringFingerprint = baseClientIdFingerprint(
    string = "client_id",
)

internal val getUserAgentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters()
    opcodes(
        Opcode.NEW_ARRAY,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.APUT_OBJECT,
        Opcode.CONST,
    )
}

// Should usually match: g2/c.x()
internal val imgurApiFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Landroid/net/Uri;")
    parameters("Ljava/lang/String;", "Z")
    strings("https", "api", "imgur", "3", "gallery", "album")
}
