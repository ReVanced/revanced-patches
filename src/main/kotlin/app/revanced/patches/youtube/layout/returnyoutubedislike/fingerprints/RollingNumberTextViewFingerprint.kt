package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object RollingNumberTextViewFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L", "F", "F"),
    opcodes = listOf(
        Opcode.IPUT,
        null,   // invoke-direct or invoke-virtual
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    ),
    customFingerprint = { _, classDef ->
        classDef.superclass == "Landroid/support/v7/widget/AppCompatTextView;" ||
                classDef.superclass == "Lcom/google/android/libraries/youtube/rendering/ui/spec/typography/YouTubeAppCompatTextView;"
    }
)