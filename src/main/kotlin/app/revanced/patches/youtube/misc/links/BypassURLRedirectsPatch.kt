package app.revanced.patches.youtube.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Suppress("unused")
val bypassURLRedirectsPatch = bytecodePatch(
    name = "Bypass URL redirects",
    description = "Adds an option to bypass URL redirects and open the original URL directly.",
) {
    dependsOn(
        integrationsPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val abUriParserResult by abUriParserFingerprint
    val httpUriParserResult by httpUriParserFingerprint

    execute {
        addResources("youtube", "misc.links.BypassURLRedirectsPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_bypass_url_redirects"),
        )

        mapOf(
            abUriParserResult to 7, // Offset to Uri.parse.
            httpUriParserResult to 0, // Offset to Uri.parse.
        ).forEach { (result, offset) ->
            result.mutableMethod.apply {
                val insertIndex = result.scanResult.patternScanResult!!.startIndex + offset
                val uriStringRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerC

                replaceInstruction(
                    insertIndex,
                    "invoke-static {v$uriStringRegister}," +
                        "Lapp/revanced/integrations/youtube/patches/BypassURLRedirectsPatch;" +
                        "->" +
                        "parseRedirectUri(Ljava/lang/String;)Landroid/net/Uri;",
                )
            }
        }
    }
}
