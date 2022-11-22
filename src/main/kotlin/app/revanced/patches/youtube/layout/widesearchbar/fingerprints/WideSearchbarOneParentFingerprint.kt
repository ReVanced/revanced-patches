package app.revanced.patches.youtube.layout.widesearchbar.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object WideSearchbarOneParentFingerprint : MethodFingerprint(
    "V", AccessFlags.PRIVATE or AccessFlags.FINAL, listOf("L"),
    strings = listOf("FEhistory", "FEmy_videos", "FEpurchases")
)