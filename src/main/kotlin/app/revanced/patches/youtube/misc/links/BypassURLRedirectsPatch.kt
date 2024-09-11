package app.revanced.patches.youtube.misc.links

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.links.fingerprints.ABUriParserFingerprint
import app.revanced.patches.youtube.misc.links.fingerprints.ABUriParserLegacyFingerprint
import app.revanced.patches.youtube.misc.links.fingerprints.HTTPUriParserFingerprint
import app.revanced.patches.youtube.misc.links.fingerprints.HTTPUriParserLegacyFingerprint
import app.revanced.patches.youtube.misc.playservice.YouTubeVersionCheck
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Bypass URL redirects",
    description = "Adds an option to bypass URL redirects and open the original URL directly.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
        YouTubeVersionCheck::class
   ],
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
                "19.17.41",
                "19.18.41",
                "19.19.39",
                "19.20.35",
                "19.21.40",
                "19.22.43",
                "19.23.40",
                "19.24.45",
                "19.25.37", 
                "19.26.42",
                "19.28.42",
                "19.29.42",
                "19.30.39",
            ],
        ),
    ],
)
@Suppress("unused")
object BypassURLRedirectsPatch : BytecodePatch(
    setOf(
        ABUriParserFingerprint,
        ABUriParserLegacyFingerprint,
        HTTPUriParserFingerprint,
        HTTPUriParserLegacyFingerprint,
    ),
) {
    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_bypass_url_redirects"),
        )

        val fingerprints =
            if (YouTubeVersionCheck.is_19_25_or_greater)
                arrayOf(
                    ABUriParserFingerprint,
                    HTTPUriParserFingerprint
                )
            else arrayOf(
                ABUriParserLegacyFingerprint,
                HTTPUriParserLegacyFingerprint
            )

        fingerprints.forEach { fingerprint ->
            fingerprint.resultOrThrow().let {
                it.mutableMethod.apply {
                    val insertIndex = indexOfFirstInstructionOrThrow {
                        val reference = getReference<MethodReference>()
                        reference?.returnType == "Landroid/net/Uri;" &&
                        reference.name == "parse"
                    }

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
}
