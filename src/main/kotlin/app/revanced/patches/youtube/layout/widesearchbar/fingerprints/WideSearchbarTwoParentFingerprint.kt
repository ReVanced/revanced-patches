package app.revanced.patches.youtube.layout.widesearchbar.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.widesearchbar.annotations.WideSearchbarCompatibility
import org.jf.dexlib2.AccessFlags

@Name("wide-searchbar-methodtwo-parent-fingerprint")
@WideSearchbarCompatibility
@Version("0.0.1")
object WideSearchbarTwoParentFingerprint : MethodFingerprint(
    "L", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("L"),
    strings = listOf("VIDEO_QUALITIES_QUICK_MENU_BOTTOM_SHEET_FRAGMENT")
)