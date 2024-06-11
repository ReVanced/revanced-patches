package app.revanced.patches.youtube.layout.buttons.navigation

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal const val ANDROID_AUTOMOTIVE_STRING = "Android Automotive"

internal val addCreateButtonViewFingerprint = methodFingerprint {
    strings("Android Wear", ANDROID_AUTOMOTIVE_STRING)
}

internal val createPivotBarFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters(
        "Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;",
        "Landroid/widget/TextView;",
        "Ljava/lang/CharSequence;",
    )
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    )
}