package app.revanced.patches.youtube.layout.startupshortsreset

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

internal val userWasInShortsFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Ljava/lang/Object;")
    strings("Failed to read user_was_in_shorts proto after successful warmup")
}

/**
 * 18.15.40+
 */
internal val userWasInShortsConfigFingerprint = fingerprint {
    returns("V")
    strings("Failed to get offline response: ")
    custom { method, _ ->
        indexOfOptionalInstruction(method) >= 0
    }
}

private val optionalOfMethodReference = ImmutableMethodReference(
    "Lj${'$'}/util/Optional;",
    "of",
    listOf("Ljava/lang/Object;"),
    "Lj${'$'}/util/Optional;",
)

fun indexOfOptionalInstruction(method: Method) = method.indexOfFirstInstruction {
    val reference = getReference<MethodReference>() ?: return@indexOfFirstInstruction false

    MethodUtil.methodSignaturesMatch(reference, optionalOfMethodReference)
}
