@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.music.layout.miniplayercolor

import app.revanced.patcher.accessFlags
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.firstMutableMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.returnType
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.util.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/ChangeMiniplayerColorPatch;"

@Suppress("unused")
val changeMiniplayerColorPatch = bytecodePatch(
    name = "Change miniplayer color",
    description = "Adds an option to change the miniplayer background color to match the fullscreen player.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        resourceMappingPatch,
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52",
        ),
    )

    apply {
        addResources("music", "layout.miniplayercolor.changeMiniplayerColor")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_music_change_miniplayer_color"),
        )

        switchToggleColorMethodMatch.match(miniPlayerConstructorMethodMatch.immutableClassDef).let {
            val relativeIndex = it.indices.last() + 1

            val invokeVirtualIndex = it.method.indexOfFirstInstructionOrThrow(
                relativeIndex,
                Opcode.INVOKE_VIRTUAL,
            )
            val colorMathPlayerInvokeVirtualReference = it.method
                .getInstruction<ReferenceInstruction>(invokeVirtualIndex).reference

            val iGetIndex = it.method.indexOfFirstInstructionOrThrow(
                relativeIndex,
                Opcode.IGET,
            )
            val colorMathPlayerIGetReference = it.method
                .getInstruction<ReferenceInstruction>(iGetIndex).reference as FieldReference

            val colorGreyIndex =
                miniPlayerConstructorMethodMatch.immutableMethod.indexOfFirstInstructionReversedOrThrow {
                    getReference<MethodReference>()?.name == "getColor"
                }
            val iPutIndex = miniPlayerConstructorMethodMatch.immutableMethod.indexOfFirstInstructionOrThrow(
                colorGreyIndex,
                Opcode.IPUT,
            )
            val colorMathPlayerIPutReference = miniPlayerConstructorMethodMatch.immutableMethod
                .getInstruction<ReferenceInstruction>(iPutIndex).reference

            miniPlayerConstructorMethodMatch.immutableClassDef.firstMutableMethodDeclaratively {
                accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
                returnType("V")
                parameterTypes(
                    parameterTypePrefixes = it.method.parameterTypes.map { type -> type.toString() }
                        .toTypedArray(),
                )
            }.apply {
                val insertIndex = indexOfFirstInstructionReversedOrThrow(Opcode.INVOKE_DIRECT)
                val freeRegister = findFreeRegister(insertIndex)

                addInstructionsAtControlFlowLabel(
                    insertIndex,
                    """
                        invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->changeMiniplayerColor()Z
                        move-result v$freeRegister
                        if-eqz v$freeRegister, :off
                        invoke-virtual { p1 }, $colorMathPlayerInvokeVirtualReference
                        move-result-object v$freeRegister
                        check-cast v$freeRegister, ${colorMathPlayerIGetReference.definingClass}
                        iget v$freeRegister, v$freeRegister, $colorMathPlayerIGetReference
                        iput v$freeRegister, p0, $colorMathPlayerIPutReference
                        :off
                        nop
                    """,
                )
            }
        }
    }
}
