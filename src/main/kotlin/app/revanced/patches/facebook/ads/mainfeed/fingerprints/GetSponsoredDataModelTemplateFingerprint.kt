package app.revanced.patches.facebook.ads.mainfeed.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object GetSponsoredDataModelTemplateFingerprint : MethodFingerprint(

    accessFlags = (AccessFlags.PUBLIC or AccessFlags.FINAL),
    parameters = listOf(),
    returnType = "L",
    opcodes = listOf(
        Opcode.CONST,
        Opcode.CONST,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT
    ),
    customFingerprint = { methodDef, classDef ->
        classDef.type == "Lcom/facebook/graphql/model/GraphQLFBMultiAdsFeedUnit;"
    }
)