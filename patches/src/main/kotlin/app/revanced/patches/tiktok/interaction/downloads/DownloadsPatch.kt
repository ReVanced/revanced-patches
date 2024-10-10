package app.revanced.patches.tiktok.interaction.downloads

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.misc.extension.sharedExtensionPatch
import app.revanced.patches.tiktok.misc.settings.settingsPatch
import app.revanced.patches.tiktok.misc.settings.settingsStatusLoadFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val downloadsPatch = bytecodePatch(
    name = "Downloads",
    description = "Removes download restrictions and changes the default path to download to.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
    )

    compatibleWith(
        "com.ss.android.ugc.trill"("36.5.4"),
        "com.zhiliaoapp.musically"("36.5.4"),
    )

    val aclCommonShareMatch by aclCommonShareFingerprint()
    val aclCommonShare2Match by aclCommonShare2Fingerprint()
    val aclCommonShare3Match by aclCommonShare3Fingerprint()
    val downloadUriMatch by downloadUriFingerprint()
    val settingsStatusLoadMatch by settingsStatusLoadFingerprint()

    execute { context ->
        aclCommonShareMatch.mutableMethod.replaceInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        aclCommonShare2Match.mutableMethod.replaceInstructions(
            0,
            """
                const/4 v0, 0x2
                return v0
            """,
        )

        // Download videos without watermark.
        aclCommonShare3Match.mutableMethod.addInstructionsWithLabels(
            0,
            """
                    invoke-static {}, Lapp/revanced/extension/tiktok/download/DownloadsPatch;->shouldRemoveWatermark()Z
                    move-result v0
                    if-eqz v0, :noremovewatermark
                    const/4 v0, 0x1
                    return v0
                    :noremovewatermark
                    nop
                """,
        )

        // Change the download path patch.
        downloadUriMatch.mutableMethod.apply {
            val firstIndex = indexOfFirstInstructionOrThrow {
                getReference<MethodReference>()?.name == "<init>"
            }
            val secondIndex = indexOfFirstInstructionOrThrow {
                getReference<MethodReference>()?.returnType?.contains("Uri") == true
            }

            addInstructions(
                secondIndex,
                """
                    invoke-static {}, Lapp/revanced/extension/tiktok/download/DownloadsPatch;->getDownloadPath()Ljava/lang/String;
                    move-result-object v0
                """,
            )

            addInstructions(
                firstIndex,
                """
                    invoke-static {}, Lapp/revanced/extension/tiktok/download/DownloadsPatch;->getDownloadPath()Ljava/lang/String;
                    move-result-object v0
                """,
            )
        }

        settingsStatusLoadMatch.mutableMethod.addInstruction(
            0,
            "invoke-static {}, Lapp/revanced/extension/tiktok/settings/SettingsStatus;->enableDownload()V",
        )
    }
}
