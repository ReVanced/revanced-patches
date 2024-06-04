package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal fun indexOfSetViewSelectedInstruction(methodDef: Method) = methodDef.indexOfFirstInstruction {
    opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>()?.name == "setSelected"
}

internal val pivotBarButtonsViewSetSelectedFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("I", "Z")
    custom { methodDef, classDef ->
        classDef.type == "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;" &&
            indexOfSetViewSelectedInstruction(methodDef) >= 0
    }
}
