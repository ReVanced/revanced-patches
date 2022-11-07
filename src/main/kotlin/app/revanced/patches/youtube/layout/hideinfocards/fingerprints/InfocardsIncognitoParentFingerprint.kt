package app.revanced.patches.youtube.layout.hideinfocards.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hideinfocards.annotations.HideInfocardsCompatibility
import org.jf.dexlib2.AccessFlags

@Name("infocards-incognito-parent-fingerprint")
@HideInfocardsCompatibility
@Version("0.0.1")
object InfocardsIncognitoParentFingerprint : MethodFingerprint(
    "Ljava/lang/String;",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("player_overlay_info_card_teaser"),
)