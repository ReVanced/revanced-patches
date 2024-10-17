package app.revanced.patches.facebook.ads.mainfeed.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object BaseModelMapperFingerprint : MethodFingerprint(

    accessFlags = (AccessFlags.PUBLIC or AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Class","I","I"),
    returnType = "Lcom/facebook/graphql/modelutil/BaseModelWithTree;",
    opcodes = listOf(
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.IF_EQ
    )

)