package app.revanced.patches.youtube.layout.watchinvr.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.watchinvr.annotations.WatchinVRCompatibility
import org.jf.dexlib2.AccessFlags

@Name("watch-in-vr-fingerprint")
@WatchinVRCompatibility
@Version("0.0.1")
object WatchinVRFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("Z"),
    strings = listOf("menu_item_cardboard_vr")
)