package app.revanced.patches.youtube.misc.links

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.links.fingerprints.ABUriParserFingerprint
import app.revanced.patches.youtube.misc.links.fingerprints.HTTPUriParserFingerprint
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch(
    name = "Bypass URL redirects",
    description = "Adds an option to bypass URL redirects and open the original URL directly.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.34"
            ]
        )
    ]
)
@Suppress("unused")
object BypassURLRedirectsPatch : BytecodePatch(
    setOf(ABUriParserFingerprint, HTTPUriParserFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_bypass_url_redirects",
                StringResource("revanced_bypass_url_redirects_title", "Bypass URL redirects"),
                StringResource("revanced_bypass_url_redirects_summary_on", "URL redirects are bypassed"),
                StringResource("revanced_bypass_url_redirects_summary_off", "URL redirects are not bypassed"),
            )
        )

        mapOf(
            ABUriParserFingerprint to 7, // Offset to Uri.parse.
            HTTPUriParserFingerprint to 0 // Offset to Uri.parse.
        ).map { (fingerprint, offset) ->
            (fingerprint.result ?: throw fingerprint.exception) to offset
        }.forEach { (result, offset) ->
            result.mutableMethod.apply {
                val insertIndex = result.scanResult.patternScanResult!!.startIndex + offset
                val uriStringRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerC

                replaceInstruction(
                    insertIndex,
                    "invoke-static {v$uriStringRegister}," +
                            "Lapp/revanced/integrations/youtube/patches/BypassURLRedirectsPatch;" +
                            "->" +
                            "parseRedirectUri(Ljava/lang/String;)Landroid/net/Uri;"
                )
            }
        }
    }
}