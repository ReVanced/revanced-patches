package app.revanced.patches.youtube.layout.hide.endscreensuggestion

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val autoNavConstructorFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    strings("main_app_autonav")
}

internal val autoNavStatusFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
}

internal val removeOnLayoutChangeListenerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    opcodes(
        Opcode.IPUT,
        Opcode.INVOKE_VIRTUAL
    )
    // This is the only reference present in the entire smali.
    custom { method, _ ->
        method.indexOfFirstInstruction {
            val reference = getReference<MethodReference>()
            reference?.name == "removeOnLayoutChangeListener" &&
            reference.definingClass.endsWith("/YouTubePlayerOverlaysLayout;")
        } >= 0
    }
}