package app.revanced.patches.youtube.misc.links

import app.revanced.patcher.CompositeMatch
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_20_37_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_49_or_greater
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/BypassURLRedirectsPatch;"

@Suppress("unused")
val bypassURLRedirectsPatch = bytecodePatch(
    name = "Bypass URL redirects",
    description = "Adds an option to bypass URL redirects and open the original URL directly.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    )

    apply {
        addResources("youtube", "misc.links.bypassURLRedirectsPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_bypass_url_redirects"),
        )

        arrayOf(
            if (is_20_49_or_greater) {
                // Code has moved, and now seems to be an account URL
                // and may not be anything to do with sharing links.
                null to -1
            } else if (is_20_37_or_greater) {
                abUriParserMethodMatch to 2
            } else {
                abUriParserLegacyMethodMatch to 2
            }
        ).forEach { (match, index) ->
            val insertIndex = match?.get(index) ?: return@forEach
            val uriStringRegister =
                match.method.getInstruction<FiveRegisterInstruction>(insertIndex).registerC

            match.method.replaceInstruction(
                insertIndex,
                "invoke-static { v$uriStringRegister }, $EXTENSION_CLASS_DESCRIPTOR->" +
                        "parseRedirectUri(Ljava/lang/String;)Landroid/net/Uri;",
            )
        }
    }
}
