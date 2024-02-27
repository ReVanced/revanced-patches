package app.revanced.patches.youtube.buttomplayer.comment.fingerprints

import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object ShortsLiveStreamEmojiPickerOnClickListenerFingerprint : LiteralValueFingerprint(
    returnType = "V",
    parameters = listOf("L"),
    accessFlags = AccessFlags.PUBLIC.value,
    literalSupplier = { 126326492 }
)