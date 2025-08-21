package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.redgifs

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val deliverRegifsOauthResponseFingerprint = fingerprint {
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.SGET_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    )
    custom { methodDef, classDef ->
        classDef.sourceFile == "OAuthRedgifTokenRequest.java" && methodDef.name == "deliverResponse"
    }
}
