package app.revanced.patches.youtube.layout.seekbar.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.seekbar.fingerprints.PlayerSeekbarGradientConfigFingerprint.PLAYER_SEEKBAR_GRADIENT_FEATURE_FLAG
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object PlayerSeekbarGradientConfigFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "Z",
    parameters = listOf(),
    literalSupplier = { PLAYER_SEEKBAR_GRADIENT_FEATURE_FLAG },
) {
    const val PLAYER_SEEKBAR_GRADIENT_FEATURE_FLAG = 45617850L
}