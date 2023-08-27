package app.revanced.patches.youtube.layout.hide.infocards.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object InfocardsIncognitoParentFingerprint : MethodFingerprint(
    "Ljava/lang/String;",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("player_overlay_info_card_teaser"),
)