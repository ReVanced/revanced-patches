package app.revanced.patches.twitter.misc.links.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Gets Resource string for share link view available by pressing "Share via" button.
internal object LinkResourceGetterFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Landroid/content/res/Resources;"),
    strings = listOf("res.getString(R.string.t…lUsername, id.toString())"),
)