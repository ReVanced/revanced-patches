package app.revanced.patches.tiktok.interaction.downloads.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object DownloadPathParentFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf(
        "code",
        "reason",
        "params insufficient"
    )
)