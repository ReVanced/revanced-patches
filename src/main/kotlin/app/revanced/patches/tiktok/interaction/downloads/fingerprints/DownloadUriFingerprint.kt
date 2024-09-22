package app.revanced.patches.tiktok.interaction.downloads.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object DownloadUriFingerprint : MethodFingerprint(
    "Landroid/net/Uri;",
    AccessFlags.PUBLIC or AccessFlags.STATIC,
    strings = listOf(
        "/",
        "/Camera",
        "/Camera/",
        "video/mp4",
    ),
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
    ),
)
