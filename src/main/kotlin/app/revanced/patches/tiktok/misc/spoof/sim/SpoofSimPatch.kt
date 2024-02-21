package app.revanced.patches.tiktok.misc.spoof.sim

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.tiktok.misc.integrations.IntegrationsPatch
import app.revanced.patches.tiktok.misc.settings.SettingsPatch
import app.revanced.patches.tiktok.misc.settings.fingerprints.SettingsStatusLoadFingerprint
import app.revanced.util.findMutableMethodOf
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "SIM spoof",
    description = "Spoofs the information which is retrieved from the SIM card.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage("com.ss.android.ugc.trill"),
        CompatiblePackage("com.zhiliaoapp.musically"),
    ],
    use = false,
)
@Suppress("unused")
object SpoofSimPatch : BytecodePatch(emptySet()) {
    private val replacements = hashMapOf(
        "getSimCountryIso" to "getCountryIso",
        "getNetworkCountryIso" to "getCountryIso",
        "getSimOperator" to "getOperator",
        "getNetworkOperator" to "getOperator",
        "getSimOperatorName" to "getOperatorName",
        "getNetworkOperatorName" to "getOperatorName",
    )

    override fun execute(context: BytecodeContext) {
        // Find all api call to check sim information.
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

        // Enable patch in settings.
        with(SettingsStatusLoadFingerprint.result!!.mutableMethod) {
            addInstruction(
                0,
                "invoke-static {}, Lapp/revanced/integrations/tiktok/settings/SettingsStatus;->enableSimSpoof()V",
            )
        }
    }

    // Patch Android API and return fake sim information.
    private fun MutableMethod.replaceReference(index: Int, replacement: String) {
        val resultReg = getInstruction<OneRegisterInstruction>(index + 1).registerA

        addInstructions(
            index + 2,
            """
                invoke-static {v$resultReg}, Lapp/revanced/integrations/tiktok/spoof/sim/SpoofSimPatch;->$replacement(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$resultReg
            """,
        )
    }
}
