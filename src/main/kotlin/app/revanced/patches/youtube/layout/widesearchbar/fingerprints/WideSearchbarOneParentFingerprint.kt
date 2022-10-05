package app.revanced.patches.youtube.layout.widesearchbar.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.widesearchbar.annotations.WideSearchbarCompatibility
import org.jf.dexlib2.AccessFlags

@Name("wide-searchbar-methodone-parent-fingerprint")
@WideSearchbarCompatibility
@Version("0.0.1")
object WideSearchbarOneParentFingerprint : MethodFingerprint(
    "V", AccessFlags.PRIVATE or AccessFlags.FINAL, listOf("L"),
    strings = listOf("FEhistory", "FEmy_videos", "FEpurchases")
)