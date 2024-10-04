package app.revanced.patches.tiktok.interaction.downloads

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.ACLCommonShareFingerprint
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.ACLCommonShareFingerprint2
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.ACLCommonShareFingerprint3
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.DownloadUriFingerprint
import app.revanced.patches.tiktok.misc.integrations.IntegrationsPatch
import app.revanced.patches.tiktok.misc.settings.SettingsPatch
import app.revanced.patches.tiktok.misc.settings.fingerprints.SettingsStatusLoadFingerprint
import app.revanced.util.exception
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Downloads",
    description = "Removes download restrictions and changes the default path to download to.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage("com.ss.android.ugc.trill", ["36.5.4"]),
        CompatiblePackage("com.zhiliaoapp.musically", ["36.5.4"]),
    ],
)
@Suppress("unused")
object DownloadsPatch : BytecodePatch(
    setOf(
        ACLCommonShareFingerprint,
        ACLCommonShareFingerprint2,
        ACLCommonShareFingerprint3,
        DownloadUriFingerprint,
        SettingsStatusLoadFingerprint,
    ),
) {
    override fun execute(context: BytecodeContext) {
        fun MethodFingerprint.getMethod() = result?.mutableMethod ?: throw exception

        mapOf<MethodFingerprint, MutableMethod.() -> Unit>(
            ACLCommonShareFingerprint to {
                replaceInstructions(
                    0,
                    """
                        const/4 v0, 0x0
                        return v0
                    """,
                )
            },
            ACLCommonShareFingerprint2 to {
                replaceInstructions(
                    0,
                    """
                        const/4 v0, 0x2
                        return v0
                    """,
                )
            },
            // Download videos without watermark.
            ACLCommonShareFingerprint3 to {
                addInstructionsWithLabels(
                    0,
                    """
                    invoke-static {}, Lapp/revanced/integrations/tiktok/download/DownloadsPatch;->shouldRemoveWatermark()Z
                    move-result v0
                    if-eqz v0, :noremovewatermark
                    const/4 v0, 0x1
                    return v0
                    :noremovewatermark
                    nop
                """,
                )
            },
            // Change the download path patch.
            DownloadUriFingerprint to {
                val firstIndex = indexOfFirstInstructionOrThrow {
                    getReference<MethodReference>()?.name == "<init>"
                }
                val secondIndex = indexOfFirstInstructionOrThrow {
                    getReference<MethodReference>()?.returnType?.contains("Uri") == true
                }

                addInstructions(
                    secondIndex,
                    """
                        invoke-static {}, Lapp/revanced/integrations/tiktok/download/DownloadsPatch;->getDownloadPath()Ljava/lang/String;
                        move-result-object v0
                    """,
                )

                addInstructions(
                    firstIndex,
                    """
                        invoke-static {}, Lapp/revanced/integrations/tiktok/download/DownloadsPatch;->getDownloadPath()Ljava/lang/String;
                        move-result-object v0
                    """,
                )
            },
            SettingsStatusLoadFingerprint to {
                addInstruction(
                    0,
                    "invoke-static {}, Lapp/revanced/integrations/tiktok/settings/SettingsStatus;->enableDownload()V",
                )
            },
        ).forEach { (fingerprint, patch) ->
            fingerprint.getMethod().patch()
        }
    }
}
