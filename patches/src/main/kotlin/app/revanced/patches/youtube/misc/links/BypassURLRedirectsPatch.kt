package app.revanced.patches.youtube.misc.links

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_20_37_or_greater
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/BypassURLRedirectsPatch;"

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
            "19.43.41",
            "20.14.43",
            "20.21.37",
            "20.31.40",
        )
    )

    execute {
        addResources("youtube", "misc.links.bypassURLRedirectsPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_bypass_url_redirects"),
        )

        arrayOf(
            if (is_20_37_or_greater) {
                (abUriParserFingerprint to 2)
            } else {
                (abUriParserLegacyFingerprint to 2)
            },
            httpUriParserFingerprint to 0
        ).forEach { (fingerprint, index) ->
            fingerprint.method.apply {
                val insertIndex = fingerprint.instructionMatches[index].index
                val uriStringRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerC

                replaceInstruction(
                    insertIndex,
                    "invoke-static { v$uriStringRegister }, $EXTENSION_CLASS_DESCRIPTOR->" +
                            "parseRedirectUri(Ljava/lang/String;)Landroid/net/Uri;",
                )
            }
        }
    }
}
