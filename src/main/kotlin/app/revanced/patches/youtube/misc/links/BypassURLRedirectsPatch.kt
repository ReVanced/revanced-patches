package app.revanced.patches.youtube.misc.links

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.links.fingerprints.OpenLinksDirectlyPrimaryFingerprint
import app.revanced.patches.youtube.misc.links.fingerprints.OpenLinksDirectlySecondaryFingerprint
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch(
    name = "Bypass URL redirects",
    description = "Bypass URL redirects and open the original URL directly.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39",
                "18.37.36",
                "18.38.44"
            ]
        )
    ]
)
object BypassURLRedirectsPatch : BytecodePatch(
    setOf(OpenLinksDirectlyPrimaryFingerprint, OpenLinksDirectlySecondaryFingerprint)
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

        arrayOf(
            OpenLinksDirectlyPrimaryFingerprint,
            OpenLinksDirectlySecondaryFingerprint
        ).map {
            it.result ?: throw it.exception
        }.forEach { result ->
            result.mutableMethod.apply {
                val insertIndex = result.scanResult.patternScanResult!!.startIndex
                val uriStringRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerC

                replaceInstruction(
                    insertIndex,
                    "invoke-static {v$uriStringRegister}," +
                            "Lapp/revanced/integrations/patches/BypassURLRedirectsPatch;" +
                            "->" +
                            "parseRedirectUri(Ljava/lang/String;)Landroid/net/Uri;"
                )
            }
        }
    }
}