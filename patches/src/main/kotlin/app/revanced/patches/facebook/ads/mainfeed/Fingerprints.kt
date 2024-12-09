package app.revanced.patches.facebook.ads.mainfeed

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val baseModelMapperFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Lcom/facebook/graphql/modelutil/BaseModelWithTree;")
    parameters("Ljava/lang/Class", "I", "I")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.IF_EQ,
    )
}

internal val getSponsoredDataModelTemplateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    opcodes(
        Opcode.CONST,
        Opcode.CONST,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    )
    custom { _, classDef ->
        classDef.type == "Lcom/facebook/graphql/model/GraphQLFBMultiAdsFeedUnit;"
    }
}

internal val getStoryVisibilityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    opcodes(
        Opcode.INSTANCE_OF,
        Opcode.IF_NEZ,
        Opcode.INSTANCE_OF,
        Opcode.IF_NEZ,
        Opcode.INSTANCE_OF,
        Opcode.IF_NEZ,
        Opcode.CONST,
    )
    strings("This should not be called for base class object")
}
