package app.revanced.patches.tiktok.misc.spoof.sim

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.misc.extension.sharedExtensionPatch
import app.revanced.patches.tiktok.misc.settings.settingsStatusLoadFingerprint
import app.revanced.patches.twitch.misc.settings.settingsPatch
import app.revanced.util.findMutableMethodOf
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val spoofSimPatch = bytecodePatch(
    name = "SIM spoof",
    description = "Spoofs the information which is retrieved from the SIM card.",
    use = false,
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
    )

    compatibleWith(
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically",
    )

    val settingsStatusLoadMatch by settingsStatusLoadFingerprint()

    execute { context ->
        val replacements = hashMapOf(
            "getSimCountryIso" to "getCountryIso",
            "getNetworkCountryIso" to "getCountryIso",
            "getSimOperator" to "getOperator",
            "getNetworkOperator" to "getOperator",
            "getSimOperatorName" to "getOperatorName",
            "getNetworkOperatorName" to "getOperatorName",
        )

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

                            val resultReg = getInstruction<OneRegisterInstruction>(index + 1).registerA

                            // Patch Android API and return fake sim information.
                            addInstructions(
                                index + 2,
                                """
                                    invoke-static {v$resultReg}, Lapp/revanced/extension/tiktok/spoof/sim/SpoofSimPatch;->$replacement(Ljava/lang/String;)Ljava/lang/String;
                                    move-result-object v$resultReg
                                """,
                            )
                        }
                    }
                }
            }
        }

        // Enable patch in settings.
        settingsStatusLoadMatch.mutableMethod.addInstruction(
            0,
            "invoke-static {}, Lapp/revanced/extension/tiktok/settings/SettingsStatus;->enableSimSpoof()V",
        )
    }
}
