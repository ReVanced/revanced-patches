package app.revanced.patches.youtube.layout.startupshortsreset.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.layout.startupshortsreset.fingerprints.UserWasInShortsConfigFingerprint.indexOfOptionalInstruction
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

/**
 * 18.15.40+
 */
internal object UserWasInShortsConfigFingerprint : MethodFingerprint(
    returnType = "V",
    strings = listOf("Failed to get offline response: "),
    customFingerprint = { methodDef, _ ->
        indexOfOptionalInstruction(methodDef) >= 0
    }
) {
    private val optionalOfMethodReference = ImmutableMethodReference(
        "Lj${'$'}/util/Optional;",
        "of",
        listOf("Ljava/lang/Object;"),
        "Lj${'$'}/util/Optional;",
    )

    fun indexOfOptionalInstruction(methodDef: Method) =
        methodDef.indexOfFirstInstruction {
            val reference = getReference<MethodReference>() ?: return@indexOfFirstInstruction false

            MethodUtil.methodSignaturesMatch(reference, optionalOfMethodReference)
        }
}