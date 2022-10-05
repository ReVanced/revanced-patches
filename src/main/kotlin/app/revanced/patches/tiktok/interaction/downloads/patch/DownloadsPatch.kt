package app.revanced.patches.tiktok.interaction.downloads.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.data.toMethodWalker
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.tiktok.interaction.downloads.annotations.DownloadsCompatibility
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.ACLCommonShareFingerprint
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.ACLCommonShareFingerprint2
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.ACLCommonShareFingerprint3
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.DownloadPathParentFingerprint
import app.revanced.patches.tiktok.misc.integrations.patch.TikTokIntegrationsPatch
import app.revanced.patches.tiktok.misc.settings.fingerprints.SettingsStatusLoadFingerprint
import app.revanced.patches.tiktok.misc.settings.patch.TikTokSettingsPatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.StringReference

@Patch
@DependsOn([TikTokIntegrationsPatch::class, TikTokSettingsPatch::class])
@Name("tiktok-download")
@Description("Removes download restrictions and changes the default path to download to.")
@DownloadsCompatibility
@Version("0.0.1")
class DownloadsPatch : BytecodePatch(
    listOf(
        ACLCommonShareFingerprint,
        ACLCommonShareFingerprint2,
        ACLCommonShareFingerprint3,
        DownloadPathParentFingerprint,
        SettingsStatusLoadFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val method1 = ACLCommonShareFingerprint.result!!.mutableMethod
        method1.replaceInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        )
        val method2 = ACLCommonShareFingerprint2.result!!.mutableMethod
        method2.replaceInstructions(
            0,
            """
                const/4 v0, 0x2
                return v0
            """
        )
        //Download videos without watermark.
        val method3 = ACLCommonShareFingerprint3.result!!.mutableMethod
        method3.addInstructions(
            0,
            """
                invoke-static {}, Lapp/revanced/tiktok/download/DownloadsPatch;->shouldRemoveWatermark()Z
                move-result v0
                if-eqz v0, :noremovewatermark
                const/4 v0, 0x1
                return v0
                :noremovewatermark
                nop
            """
        )
        //Change the download path patch
        val method4 = DownloadPathParentFingerprint.result!!.mutableMethod
        val implementation4 = method4.implementation
        val instructions = implementation4!!.instructions
        var targetOffset = -1
        //Search for the target method called instruction offset.
        for ((index, instruction) in instructions.withIndex()) {
            if (instruction.opcode != Opcode.CONST_STRING) continue
            val reference = (instruction as ReferenceInstruction).reference as StringReference
            if (reference.string != "video/mp4") continue
            val targetInstruction = instructions[index + 1]
            if (targetInstruction.opcode != Opcode.INVOKE_STATIC) continue
            targetOffset = index + 1
            break
        }
        if (targetOffset == -1) return PatchResultError("Can not find download path uri method.")
        //Change videos' download path.
        val downloadUriMethod = context
            .toMethodWalker(DownloadPathParentFingerprint.result!!.method)
            .nextMethod(targetOffset, true)
            .getMethod() as MutableMethod
        downloadUriMethod.implementation!!.instructions.forEachIndexed { index, instruction ->
            if (instruction.opcode == Opcode.SGET_OBJECT) {
                val overrideRegister = (instruction as OneRegisterInstruction).registerA
                downloadUriMethod.addInstructions(
                    index + 1,
                    """
                        invoke-static {}, Lapp/revanced/tiktok/download/DownloadsPatch;->getDownloadPath()Ljava/lang/String;
                        move-result-object v$overrideRegister
                    """
                )
            }
            if (instruction.opcode == Opcode.CONST_STRING) {
                val string = ((instruction as ReferenceInstruction).reference as StringReference).string
                if (string.contains("/Camera")) {
                    val overrideRegister = (instruction as OneRegisterInstruction).registerA
                    val overrideString = string.replace("/Camera", "")
                    downloadUriMethod.replaceInstruction(
                        index,
                        """
                            const-string v$overrideRegister, "$overrideString"
                        """
                    )
                }
            }
        }
        val method5 = SettingsStatusLoadFingerprint.result!!.mutableMethod
        method5.addInstruction(
            0,
            "invoke-static {}, Lapp/revanced/tiktok/settingsmenu/SettingsStatus;->enableDownload()V"
        )
        return PatchResultSuccess()
    }
}