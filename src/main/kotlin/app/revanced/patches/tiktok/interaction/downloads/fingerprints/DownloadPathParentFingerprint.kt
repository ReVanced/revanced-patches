package app.revanced.patches.tiktok.interaction.downloads.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.interaction.downloads.annotations.DownloadsCompatibility
import org.jf.dexlib2.AccessFlags

@Name("download-path-parent-fingerprint")
@DownloadsCompatibility
@Version("0.0.1")
object DownloadPathParentFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf(
        "code",
        "reason",
        "params insufficient"
    )
)