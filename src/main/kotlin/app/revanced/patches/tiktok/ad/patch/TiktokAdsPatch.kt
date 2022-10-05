package app.revanced.patches.tiktok.ad.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tiktok.ad.annotations.TiktokAdsCompatibility
import app.revanced.patches.tiktok.ad.fingerprints.ConvertHelpFeedItemListFingerprint
import app.revanced.patches.tiktok.ad.fingerprints.FeedItemListCloneFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction
import org.jf.dexlib2.iface.reference.FieldReference

@Patch
@Name("tiktok-ads")
@Description("Removes ads from TikTok.")
@TiktokAdsCompatibility
@Version("0.0.1")
class TiktokAdsPatch : BytecodePatch(
    listOf(
        FeedItemListCloneFingerprint,
        ConvertHelpFeedItemListFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        listOf(
            FeedItemListCloneFingerprint,
            ConvertHelpFeedItemListFingerprint
        ).forEach { fingerprint ->
            val method = fingerprint.result!!.mutableMethod
            // iterate all instructions in the clone method
            for ((index, instruction) in method.implementation!!.instructions.withIndex()) {
                // conditions for the instruction we need
                if (instruction.opcode.ordinal != Opcode.IPUT_OBJECT.ordinal) continue
                val preloadAdsFieldInstruction = (instruction as? ReferenceInstruction)
                if ((preloadAdsFieldInstruction?.reference as? FieldReference)?.name != "preloadAds") continue

                // set null instead of the field "preloadAds"
                val overrideRegister = (preloadAdsFieldInstruction as TwoRegisterInstruction).registerA
                method.addInstruction(
                    index,
                    "const/4 v$overrideRegister, 0x0"
                )
                return@forEach
            }
            return PatchResultError("Can not find required instruction.")
        }
        return PatchResultSuccess()
    }
}
