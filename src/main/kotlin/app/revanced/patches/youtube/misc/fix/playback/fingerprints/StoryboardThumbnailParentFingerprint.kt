package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Here lies code that creates the seekbar thumbnails.
 *
 * An additional change here might force the thumbnails to be created,
 * or possibly a change somewhere else (maybe involving YouTube 18.23.35 class `hte`)
 */
object StoryboardThumbnailParentFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "Landroid/graphics/Bitmap;",
    strings = listOf("Storyboard regionDecoder.decodeRegion exception - "),
)