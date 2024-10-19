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
import app.revanced.patches.youtube.misc.playservice.VersionCheckPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Bypass URL redirects",
    description = "Adds an option to bypass URL redirects and open the original URL directly.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
        VersionCheckPatch::class
   ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
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
            if (VersionCheckPatch.is_19_33_or_greater)
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
                    val insertIndex = findUriParseIndex()

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

    internal fun Method.findUriParseIndex(): Int = indexOfFirstInstruction {
        val reference = getReference<MethodReference>()
        reference?.returnType == "Landroid/net/Uri;" &&
                reference.name == "parse"
    }
}
