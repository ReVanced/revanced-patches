package app.revanced.patches.tiktok.interaction.downloads.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.data.impl.toMethodWalker
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.tiktok.interaction.downloads.annotations.DownloadsCompatibility
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.ACLCommonShareFingerprint
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.ACLCommonShareFingerprint2
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.ACLCommonShareFingerprint3
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.DownloadPathParentFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.StringReference

@Patch
@Name("tiktok-download")
@Description("Removes download restrictions and changes the default path to download to.")
@DownloadsCompatibility
@Version("0.0.1")
class DownloadsPatch : BytecodePatch(
    listOf(
        ACLCommonShareFingerprint,
        ACLCommonShareFingerprint2,
        ACLCommonShareFingerprint3,
        DownloadPathParentFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
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
        method3.replaceInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
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
        val downloadPath = "$downloadPathParent/$downloadPathChild"
        val downloadUriMethod = data
            .toMethodWalker(DownloadPathParentFingerprint.result!!.method)
            .nextMethod(targetOffset, true)
            .getMethod() as MutableMethod
        downloadUriMethod.implementation!!.instructions.forEachIndexed { index, instruction ->
            if (instruction.opcode == Opcode.SGET_OBJECT) {
                val overrideRegister = (instruction as OneRegisterInstruction).registerA
                downloadUriMethod.replaceInstruction(
                    index,
                    """
                        const-string v$overrideRegister, "$downloadPath"
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
        return PatchResultSuccess()
    }

    companion object : OptionsContainer() {
        private var downloadPathParent: String? by option(
            PatchOption.StringListOption(
                key = "mediaFolder",
                default = "DCIM",
                options = listOf(
                    "DCIM", "Movies", "Pictures"
                ),
                title = "Media folder",
                description = "The media root folder to download to.",
                required = true
            )
        )
        private var downloadPathChild: String? by option(
            PatchOption.StringOption(
                key = "downloadPath",
                default = "TikTok",
                title = "Download path",
                description = "Download path relative to the media folder.",
                required = true
            )
        )
    }
}