package app.revanced.patches.youtube.buttomplayer.comment.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.EmojiPickerIcon
import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object ShortsLiveStreamEmojiPickerOpacityFingerprint : LiteralValueFingerprint(
    returnType = "Landroid/widget/ImageView;",
    accessFlags = AccessFlags.PROTECTED or AccessFlags.FINAL,
    parameters = emptyList(),
    literalSupplier = { EmojiPickerIcon }
)