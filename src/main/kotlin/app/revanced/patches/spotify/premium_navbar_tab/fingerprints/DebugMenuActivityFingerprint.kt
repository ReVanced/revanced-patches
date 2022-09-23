package app.revanced.patches.spotify.premium_navbar_tab.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.spotify.premium_navbar_tab.annotations.PremiumNavbarTabCompatibility

@Name("debug-menu-activity-fingerprint")
@Version("0.0.1")
@PremiumNavbarTabCompatibility
object DebugMenuActivityFingerprint : MethodFingerprint(
     strings = listOf("com.spotify.app.music.debugtools.menu.DebugMenuActivity"),
     parameters = listOf("L", "L"),
)