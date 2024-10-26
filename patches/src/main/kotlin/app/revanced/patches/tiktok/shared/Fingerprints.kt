package app.revanced.patches.tiktok.shared

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val getEnterFromFingerprint = fingerprint {
    returns("Ljava/lang/String;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Z")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    )
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/BaseListFragmentPanel;")
    }
}

internal val onRenderFirstFrameFingerprint = fingerprint {
    strings("method_enable_viewpager_preload_duration")
    custom { _, classDef ->
        classDef.endsWith("/BaseListFragmentPanel;")
    }
}
