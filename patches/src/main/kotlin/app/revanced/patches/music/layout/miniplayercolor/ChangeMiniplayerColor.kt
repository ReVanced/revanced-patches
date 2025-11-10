@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.music.layout.miniplayercolor

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal var mpp_player_bottom_sheet = -1L
    private set

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/ChangeMiniplayerColorPatch;"

@Suppress("unused")
val changeMiniplayerColor = bytecodePatch(
    name = "Change miniplayer color",
    description = "Adds an option to change the miniplayer background color to match the fullscreen player."
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        resourceMappingPatch
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52"
        )
    )

    execute {
        mpp_player_bottom_sheet = resourceMappings["id", "mpp_player_bottom_sheet"]

        addResources("music", "layout.miniplayercolor.changeMiniplayerColor")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_music_change_miniplayer_color"),
        )

        switchToggleColorFingerprint.match(miniPlayerConstructorFingerprint.classDef).let {
            val relativeIndex = it.patternMatch!!.endIndex + 1

            val invokeVirtualIndex = it.method.indexOfFirstInstructionOrThrow(
                relativeIndex, Opcode.INVOKE_VIRTUAL
            )
            val colorMathPlayerInvokeVirtualReference = it.method
                .getInstruction<ReferenceInstruction>(invokeVirtualIndex).reference

            val iGetIndex = it.method.indexOfFirstInstructionOrThrow(
                relativeIndex, Opcode.IGET
            )
            val colorMathPlayerIGetReference = it.method
                .getInstruction<ReferenceInstruction>(iGetIndex).reference as FieldReference

            val colorGreyIndex = miniPlayerConstructorFingerprint.method.indexOfFirstInstructionReversedOrThrow {
                getReference<MethodReference>()?.name == "getColor"
            }
            val iPutIndex = miniPlayerConstructorFingerprint.method.indexOfFirstInstructionOrThrow(
                colorGreyIndex, Opcode.IPUT
            )
            val colorMathPlayerIPutReference = miniPlayerConstructorFingerprint.method
                .getInstruction<ReferenceInstruction>(iPutIndex).reference

            miniPlayerConstructorFingerprint.classDef.methods.single { method ->
                method.accessFlags == AccessFlags.PUBLIC.value or AccessFlags.FINAL.value &&
                        method.returnType == "V" &&
                        method.parameters == it.originalMethod.parameters
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
                    """
                )
            }
        }
    }
}
