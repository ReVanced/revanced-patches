package app.revanced.patches.facebook.ads.mainfeed.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Annotation
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

internal object GetStoryVisibilityFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/Integer;",
    accessFlags = (AccessFlags.PUBLIC or AccessFlags.STATIC),
    parameters = listOf(), // Means at least one ?
    opcodes = listOf(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
    ),
    customFingerprint = { methodDef, classDef ->
        // Method has a deprecated annotation
        methodDef.annotations.any any@{annotation ->
            return@any annotation.type == "Ljava/lang/Deprecated;"
        }
    },
)