package app.revanced.patches.facebook.ads.mainfeed

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.baseModelMapperMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Lcom/facebook/graphql/modelutil/BaseModelWithTree;")
    parameterTypes("Ljava/lang/Class", "I", "I")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.IF_EQ,
    )
}
internal val BytecodePatchContext.getSponsoredDataModelTemplateMethod by gettingFirstMutableMethodDeclaratively {
    definingClass("Lcom/facebook/graphql/model/GraphQLFBMultiAdsFeedUnit;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes()
    opcodes(
        Opcode.CONST,
        Opcode.CONST,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    )

}
internal val BytecodePatchContext.getStoryVisibilityMethod by gettingFirstMutableMethodDeclaratively("This should not be called for base class object") {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    opcodes(
        Opcode.INSTANCE_OF,
        Opcode.IF_NEZ,
        Opcode.INSTANCE_OF,
        Opcode.IF_NEZ,
        Opcode.INSTANCE_OF,
        Opcode.IF_NEZ,
        Opcode.CONST,
    )
}