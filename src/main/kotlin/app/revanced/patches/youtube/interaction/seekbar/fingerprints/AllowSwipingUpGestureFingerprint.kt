package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.util.containsWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.AccessFlags

internal object AllowSwipingUpGestureFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("L"),
    customFingerprint = { _, classDef ->
        classDef.methods.any { method ->
            method.containsWideLiteralInstructionValue(45379021L)
        }
    }
)