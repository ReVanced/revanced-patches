package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.fingerprint
import app.revanced.patcher.string
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val getPackageInfoFingerprint by fingerprint {
    instructions(
        string("Failed to get the application signatures")
    )
}

internal val loadOrbitLibraryFingerprint by fingerprint {
    strings("/liborbit-jni-spotify.so")
}

internal val startupPageLayoutInflateFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("Landroid/view/LayoutInflater;", "Landroid/view/ViewGroup;", "Landroid/os/Bundle;")
    strings("blueprintContainer", "gradient", "valuePropositionTextView")
}

internal val renderStartLoginScreenFingerprint by fingerprint {
    strings("authenticationButtonFactory", "MORE_OPTIONS")
}

internal val renderSecondLoginScreenFingerprint by fingerprint {
    strings("authenticationButtonFactory", "intent_login")
}

internal val renderThirdLoginScreenFingerprint by fingerprint {
    strings("EMAIL_OR_USERNAME", "listener")
}

internal val thirdLoginScreenLoginOnClickFingerprint by fingerprint {
    strings("login", "listener", "none")
}

internal val runIntegrityVerificationFingerprint by fingerprint {
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
