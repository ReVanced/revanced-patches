package app.revanced.patches.youtube.layout.seekbar.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.seekbar.SeekbarColorResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object ShortsSeekbarColorFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    literalSupplier = { SeekbarColorResourcePatch.reelTimeBarPlayedColorId },
)