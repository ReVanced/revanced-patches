package app.revanced.patches.tiktok.misc.spoof.sim.patch

import app.revanced.extensions.findMutableMethodOf
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.tiktok.misc.integrations.patch.TikTokIntegrationsPatch
import app.revanced.patches.tiktok.misc.settings.fingerprints.SettingsStatusLoadFingerprint
import app.revanced.patches.tiktok.misc.settings.patch.TikTokSettingsPatch
import app.revanced.patches.tiktok.misc.spoof.sim.annotations.SpoofSimCompatibility
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.MethodReference

@Patch(false)
@DependsOn([TikTokIntegrationsPatch::class, TikTokSettingsPatch::class])
@Name("sim-spoof")
@Description("Spoofs the information which is retrieved from the sim-card.")
@SpoofSimCompatibility
@Version("0.0.1")
class SpoofSimPatch : BytecodePatch() {
    private companion object {
        val replacements = hashMapOf(
            "getSimCountryIso" to "getCountryIso",
            "getNetworkCountryIso" to "getCountryIso",
            "getSimOperator" to "getOperator",
            "getNetworkOperator" to "getOperator",
            "getSimOperatorName" to "getOperatorName",
            "getNetworkOperatorName" to "getOperatorName"
        )
    }

    override fun execute(context: BytecodeContext): PatchResult {
        // Find all api call to check sim information
        buildMap {
            context.classes.forEach { classDef ->
                classDef.methods.let { methods ->
                    buildMap methodList@{
                        methods.forEach methods@{ method ->
                            with(method.implementation?.instructions ?: return@methods) {
                                ArrayDeque<Pair<Int, String>>().also { patchIndices ->
                                    this.forEachIndexed { index, instruction ->
                                        if (instruction.opcode != Opcode.INVOKE_VIRTUAL) return@forEachIndexed

                                        val methodRef =
                                            (instruction as Instruction35c).reference as MethodReference
                                        if (methodRef.definingClass != "Landroid/telephony/TelephonyManager;") return@forEachIndexed

                                        replacements[methodRef.name]?.let { replacement ->
                                            patchIndices.add(index to replacement)
                                        }
                                    }
                                }.also { if (it.isEmpty()) return@methods }.let { patches ->
                                    put(method, patches)
                                }
                            }
                        }
                    }
                }.also { if (it.isEmpty()) return@forEach }.let { methodPatches ->
                    put(classDef, methodPatches)
                }
            }
        }.forEach { (classDef, methods) ->
            with(context.proxy(classDef).mutableClass) {
                methods.forEach { (method, patches) ->
                    with(findMutableMethodOf(method)) {
                        while (!patches.isEmpty()) {
                            val (index, replacement) = patches.removeLast()
                            replaceReference(index, replacement)
                        }
                    }
                }
            }
        }

        // Enable patch in settings
        with(SettingsStatusLoadFingerprint.result!!.mutableMethod) {
            addInstruction(
                0,
                "invoke-static {}, Lapp/revanced/tiktok/settingsmenu/SettingsStatus;->enableSimSpoof()V"
            )
        }

        return PatchResultSuccess()
    }

    // Patch Android API and return fake sim information
    private fun MutableMethod.replaceReference(index: Int, replacement: String) {
        val resultReg = (instruction(index + 1) as OneRegisterInstruction).registerA

        addInstructions(
            index + 2,
            """
                     invoke-static {v$resultReg}, Lapp/revanced/tiktok/spoof/sim/SpoofSimPatch;->$replacement(Ljava/lang/String;)Ljava/lang/String;
                     move-result-object v$resultReg
                 """
        )
    }
}