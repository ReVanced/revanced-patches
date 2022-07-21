package app.revanced.patches.youtube.misc.forcevp9.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.extensions.removeInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.fingerprint.method.utils.MethodFingerprintUtils.resolve
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.forcevp9.annotations.ForceVP9Compatibility
import app.revanced.patches.youtube.misc.forcevp9.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference

@Patch(include = false)
@Dependencies([IntegrationsPatch::class])
@Name("force-vp9-codec")
@Description("Forces the VP9 codec for videos.")
@ForceVP9Compatibility
@Version("0.0.1")
class ForceVP9CodecPatch : BytecodePatch(
    listOf(
        ForceVP9ParentFingerprint, ReplaceDeviceInfoParentFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val classDef = ForceVP9ParentFingerprint.result!!.classDef
        ForceVP9CodecFingerprint.resolve(data, classDef)
        ForceVP9CodecFingerprintTwo.resolve(data, classDef)

        replaceInstructions(ForceVP9CodecFingerprint.result!!)
        replaceInstructions(ForceVP9CodecFingerprintTwo.result!!)

        ReplaceDeviceInfoFingerprint.resolve(data, ReplaceDeviceInfoParentFingerprint.result!!.classDef)
        var method = ReplaceDeviceInfoFingerprint.result!!.mutableMethod
        replaceDeviceInfos("Manufacturer", method)
        replaceDeviceInfos("Model", method)

        return PatchResultSuccess()
    }

    private fun replaceInstructions(result: MethodFingerprintResult) {
        val method = result.mutableMethod
        method.removeInstructions(0, method.implementation!!.instructions.size - 1)
        method.addInstructions(
            0, """
            invoke-static {}, Lapp/revanced/integrations/patches/ForceCodecPatch;->shouldForceVP9()Z
            move-result v0
            return v0
        """
        )
    }

    private fun replaceDeviceInfos(name: String, method: MutableMethod) {
        var impl = method.implementation!!
        //find target instruction for Build.name.uppercase() and replace that with our method
        impl.instructions.filter {
            ((it as? ReferenceInstruction)?.reference as? FieldReference)?.let { field ->
                //sget-object v1, Landroid/os/Build;->MANUFACTURER:Ljava/lang/String;
                //sget-object v1, Landroid/os/Build;->MODEL:Ljava/lang/String;
                field.definingClass == "Landroid/os/Build;" && field.name == name.uppercase()
            } == true
        }.forEach { instruction ->
            val index = method.implementation!!.instructions.indexOf(instruction)
            val register = (instruction as OneRegisterInstruction).registerA

            // inject the call to
            method.removeInstruction(index)
            method.addInstructions(
                index, """
                invoke-static {}, Lapp/revanced/integrations/patches/ForceCodecPatch;->get$name()Ljava/lang/String;
                move-result-object v$register
            """
            )
        }
    }
}
