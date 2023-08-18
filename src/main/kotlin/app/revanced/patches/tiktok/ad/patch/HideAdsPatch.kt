package app.revanced.patches.tiktok.ad.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tiktok.ad.annotations.HideAdsCompatibility
import app.revanced.patches.tiktok.ad.fingerprints.ConvertHelpFeedItemListFingerprint
import app.revanced.patches.tiktok.ad.fingerprints.FeedItemListCloneFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch
@Name("Hide ads")
@Description("Removes ads from TikTok.")
@HideAdsCompatibility
class HideAdsPatch : BytecodePatch(
    listOf(
        FeedItemListCloneFingerprint,
        ConvertHelpFeedItemListFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
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
            throw PatchException("Can not find required instruction.")
        }
    }
}
