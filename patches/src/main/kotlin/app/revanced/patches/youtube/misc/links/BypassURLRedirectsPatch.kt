package app.revanced.patches.youtube.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_33_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

val bypassURLRedirectsPatch = bytecodePatch(
    name = "Bypass URL redirects",
    description = "Adds an option to bypass URL redirects and open the original URL directly.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "misc.links.bypassURLRedirectsPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_bypass_url_redirects"),
        )

        val fingerprints = if (is_19_33_or_greater) {
            arrayOf(
                abUriParserFingerprint,
                httpUriParserFingerprint,
            )
        } else {
            arrayOf(
                abUriParserLegacyFingerprint,
                httpUriParserLegacyFingerprint,
            )
        }

        fingerprints.forEach {
            it.method.apply {
                val insertIndex = findUriParseIndex()
                val uriStringRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerC

                replaceInstruction(
                    insertIndex,
                    "invoke-static {v$uriStringRegister}," +
                        "Lapp/revanced/extension/youtube/patches/BypassURLRedirectsPatch;" +
                        "->" +
                        "parseRedirectUri(Ljava/lang/String;)Landroid/net/Uri;",
                )
            }
        }
    }
}

internal fun Method.findUriParseIndex() = indexOfFirstInstruction {
    val reference = getReference<MethodReference>()
    reference?.returnType == "Landroid/net/Uri;" && reference.name == "parse"
}

internal fun Method.findWebViewCheckCastIndex() = indexOfFirstInstruction {
    val reference = getReference<TypeReference>()
    opcode == Opcode.CHECK_CAST && reference?.type?.endsWith("/WebviewEndpointOuterClass${'$'}WebviewEndpoint;") == true
}
