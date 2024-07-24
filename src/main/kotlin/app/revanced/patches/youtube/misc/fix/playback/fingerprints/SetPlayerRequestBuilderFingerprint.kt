package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object SetPlayerRequestBuilderFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    returnType = "Lorg/chromium/net/UrlRequest;",
    opcodes = listOf(
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL
    ),
    customFingerprint = custom@{ methodDef, _ ->
        // 19.16.39(+?) add additional param: "L"
        val parameterTypes = methodDef.parameterTypes

        parameterTypes.size in 7..8 && parameterTypes[6] == "Lorg/chromium/net/UrlRequest\$Callback;"
    }
)