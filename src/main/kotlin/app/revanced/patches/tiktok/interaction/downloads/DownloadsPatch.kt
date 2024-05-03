package app.revanced.patches.tiktok.interaction.downloads

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.aclCommonShareFingerprint
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.aclCommonShareFingerprint2
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.aclCommonShareFingerprint3
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.downloadPathParentFingerprint
import app.revanced.patches.tiktok.misc.settings.fingerprints.settingsStatusLoadFingerprint
import app.revanced.patches.tiktok.misc.settings.settingsPatch
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val downloadsPatch = bytecodePatch(
    name = "Downloads",
    description = "Removes download restrictions and changes the default path to download to.",
) {
    dependsOn(integrationsPatch, settingsPatch)

    compatibleWith("com.ss.android.ugc.trill"("32.5.3"), "com.zhiliaoapp.musically"("32.5.3"))

    val aclCommonShareResult by aclCommonShareFingerprint
    val aclCommonShareResult2 by aclCommonShareFingerprint2
    val aclCommonShareResult3 by aclCommonShareFingerprint3
    val downloadPathParentResult by downloadPathParentFingerprint
    val settingsStatusLoadResult by settingsStatusLoadFingerprint

    execute { context ->
        aclCommonShareResult.mutableMethod.replaceInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        aclCommonShareResult2.mutableMethod.replaceInstructions(
            0,
            """
                const/4 v0, 0x2
                return v0
            """,
        )

        // Download videos without watermark.
        aclCommonShareResult3.mutableMethod.addInstructionsWithLabels(
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

        // Change the download path patch.
        downloadPathParentResult.mutableMethod.apply {
            val downloadUriMethod = context
                .navigator(this)
                .at(indexOfFirstInstruction { opcode == Opcode.INVOKE_STATIC })
                .mutable()

            val firstIndex = downloadUriMethod.indexOfFirstInstruction {
                opcode == Opcode.INVOKE_DIRECT && ((this as Instruction35c).reference as MethodReference).name == "<init>"
            }
            val secondIndex = downloadUriMethod.indexOfFirstInstruction {
                opcode == Opcode.INVOKE_STATIC && ((this as Instruction35c).reference as MethodReference).returnType.contains(
                    "Uri",
                )
            }

            downloadUriMethod.addInstructions(
                secondIndex,
                """
                    invoke-static {}, Lapp/revanced/integrations/tiktok/download/DownloadsPatch;->getDownloadPath()Ljava/lang/String;
                    move-result-object v0
                """,
            )

            downloadUriMethod.addInstructions(
                firstIndex,
                """
                    invoke-static {}, Lapp/revanced/integrations/tiktok/download/DownloadsPatch;->getDownloadPath()Ljava/lang/String;
                    move-result-object v0
                """,
            )
        }

        settingsStatusLoadResult.mutableMethod.addInstruction(
            0,
            "invoke-static {}, Lapp/revanced/integrations/tiktok/settings/SettingsStatus;->enableDownload()V",
        )
    }
}
