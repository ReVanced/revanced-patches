package app.revanced.patches.spotify.premium_navbar_tab.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.utils.MethodFingerprintUtils.resolve
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.spotify.premium_navbar_tab.annotations.PremiumNavbarTabCompatibility
import app.revanced.patches.spotify.premium_navbar_tab.fingerprints.AddPremiumNavbarTabFingerprint
import app.revanced.patches.spotify.premium_navbar_tab.fingerprints.DebugMenuActivityFingerprint
import app.revanced.patches.youtube.misc.mapping.patch.ResourceIdMappingProviderResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@Name("hide-premium-nav-bar")
@Description("Removes the premium tab from the navbar.")
@PremiumNavbarTabCompatibility
@Version("0.0.1")
@DependsOn([ResourceIdMappingProviderResourcePatch::class])
class PremiumNavbarTabPatch : BytecodePatch(
    listOf(
        DebugMenuActivityFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val parentResult = DebugMenuActivityFingerprint.result!!
        AddPremiumNavbarTabFingerprint.resolve(data, parentResult.classDef)

        val result = AddPremiumNavbarTabFingerprint.result!!
        val method = result.mutableMethod

        val premiumTabId = ResourceIdMappingProviderResourcePatch.resourceMappings.single{it.type == "id" && it.name == "premium_tab"}.id.toInt()

        val methodInstructions = method.implementation!!.instructions

        for ((i, instruction) in methodInstructions.asReversed().withIndex()) {
            if (instruction.opcode.ordinal != Opcode.CONST.ordinal) continue
            if ((instruction as OneRegisterInstruction).registerA != premiumTabId) continue
            val constIdx = methodInstructions.size - i
            val methodIdx = constIdx + 8
            method.removeInstruction(methodIdx)
            break
        }

        return PatchResultSuccess()
    }
}