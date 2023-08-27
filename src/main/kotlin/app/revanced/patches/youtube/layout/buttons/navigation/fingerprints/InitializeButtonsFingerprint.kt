package app.revanced.patches.youtube.layout.buttons.navigation.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.buttons.navigation.patch.ResolvePivotBarFingerprintsPatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object InitializeButtonsFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf(),
    literal = ResolvePivotBarFingerprintsPatch.imageOnlyTabResourceId
)