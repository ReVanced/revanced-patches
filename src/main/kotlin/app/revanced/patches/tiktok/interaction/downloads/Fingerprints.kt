package app.revanced.patches.tiktok.interaction.downloads

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val aclCommonShareFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("I")
    custom { methodDef, classDef ->
        classDef.endsWith("/ACLCommonShare;") &&
            methodDef.name == "getCode"
    }
}

internal val aclCommonShare2Fingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("I")
    custom { methodDef, classDef ->
        classDef.endsWith("/ACLCommonShare;") &&
            methodDef.name == "getShowType"
    }
}

internal val aclCommonShare3Fingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("I")
    custom { methodDef, classDef ->
        classDef.endsWith("/ACLCommonShare;") &&
            methodDef.name == "getTranscode"
    }
}

internal val downloadPathParentFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("L")
    strings("video/mp4")
    parameters("L", "L")
    opcodes(
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    )
}
