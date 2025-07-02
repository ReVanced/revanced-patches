package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val getPackageInfoFingerprint = fingerprint {
    strings(
        "Failed to get the application signatures"
    )
}

internal val loadOrbitLibraryFingerprint = fingerprint {
    strings("/liborbit-jni-spotify.so")
}

internal val startupPageLayoutInflateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("Landroid/view/LayoutInflater;", "Landroid/view/ViewGroup;", "Landroid/os/Bundle;")
    strings("blueprintContainer", "gradient", "valuePropositionTextView")
}

internal val renderStartLoginScreenFingerprint = fingerprint {
    strings("authenticationButtonFactory", "MORE_OPTIONS")
}

internal val renderSecondLoginScreenFingerprint = fingerprint {
    strings("authenticationButtonFactory", "intent_login")
}

internal val renderThirdLoginScreenFingerprint = fingerprint {
    strings("EMAIL_OR_USERNAME", "listener")
}

internal val thirdLoginScreenLoginOnClickFingerprint = fingerprint {
    strings("login", "listener", "none")
}

internal val runIntegrityVerificationFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_STATIC, // Calendar.getInstance()
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL, // instance.get(6)
        Opcode.MOVE_RESULT,
        Opcode.IF_EQ, // if (x == instance.get(6)) return
    )
    custom { method, _ ->
        method.indexOfFirstInstruction {
            val reference = getReference<MethodReference>()
            reference?.definingClass == "Ljava/util/Calendar;" && reference.name == "get"
        } >= 0
    }
}
