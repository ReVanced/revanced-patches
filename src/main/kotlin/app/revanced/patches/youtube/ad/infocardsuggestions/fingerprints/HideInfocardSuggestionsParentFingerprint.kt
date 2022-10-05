package app.revanced.patches.youtube.ad.infocardsuggestions.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.ad.infocardsuggestions.annotations.HideInfocardSuggestionsCompatibility
import org.jf.dexlib2.AccessFlags

@Name("hide-infocard-suggestions-parent-fingerprint")
@FuzzyPatternScanMethod(2)
@HideInfocardSuggestionsCompatibility
@Version("0.0.1")
object HideInfocardSuggestionsParentFingerprint : MethodFingerprint(
    "Ljava/lang/String;",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("player_overlay_info_card_teaser"),
)