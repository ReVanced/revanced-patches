package app.revanced.patches.youtube.layout.oldqualitylayout.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.utils.MethodFingerprintUtils.resolve
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.layout.oldqualitylayout.annotations.OldQualityLayoutCompatibility
import app.revanced.patches.youtube.layout.oldqualitylayout.fingerprints.OldQualityFingerprint
import app.revanced.patches.youtube.layout.oldqualitylayout.fingerprints.OldQualityParentFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t

@Patch
@Dependencies(dependencies = [IntegrationsPatch::class])
@Name("old-quality-layout")
@Description("Enable the original quality flyout menu.")
@OldQualityLayoutCompatibility
@Version("0.0.1")
class OldQualityLayoutPatch : BytecodePatch(
    listOf(
        OldQualityParentFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        OldQualityFingerprint.resolve(data, OldQualityParentFingerprint.result!!.classDef)
        val result = OldQualityFingerprint.result
            ?: return PatchResultError("Required parent method could not be found.")

        val implementation = result.mutableMethod.implementation!!

        // if useOldStyleQualitySettings == true, jump over all instructions
        val jmpInstruction = BuilderInstruction21t(
            Opcode.IF_NEZ, 0, implementation.instructions[result.patternScanResult!!.endIndex].location.labels.first()
        )
        implementation.addInstruction(5, jmpInstruction)
        result.mutableMethod.addInstructions(
            0, """
                invoke-static { }, Lapp/revanced/integrations/patches/OldStyleQualityPatch;->useOldStyleQualitySettings()Z
                move-result v0
            """
        )

        return PatchResultSuccess()
    }
}