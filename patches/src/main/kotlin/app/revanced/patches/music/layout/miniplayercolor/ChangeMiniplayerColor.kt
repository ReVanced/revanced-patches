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
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal var colorGrey = -1L
    private set

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/ChangeMiniplayerColorPatch;"

@Suppress("unused")
val changeMiniplayerColor = bytecodePatch(
    name = "Change miniplayer color",
    description = "Adds an option to make the miniplayer match the fullscreen player color."
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
        colorGrey = resourceMappings["color", "ytm_color_grey_12"]

        addResources("music", "layout.miniplayercolor.changeMiniplayerColor")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_music_change_miniplayer_color"),
        )

        val switchToggleColorMatch = switchToggleColorFingerprint.match(miniPlayerConstructorFingerprint.classDef)
        val relativeIndex = switchToggleColorMatch.patternMatch!!.endIndex + 1

        val invokeVirtualIndex = switchToggleColorMatch.method.indexOfFirstInstructionOrThrow(
            relativeIndex, Opcode.INVOKE_VIRTUAL
        )
        val colorMathPlayerInvokeVirtualReference = switchToggleColorMatch.method
            .getInstruction<ReferenceInstruction>(invokeVirtualIndex).reference

        val iGetIndex = switchToggleColorMatch.method.indexOfFirstInstructionOrThrow(
            relativeIndex, Opcode.IGET
        )
        val colorMathPlayerIGetReference = switchToggleColorMatch.method
            .getInstruction<ReferenceInstruction>(iGetIndex).reference

        val colorGreyIndex = miniPlayerConstructorFingerprint.method
            .indexOfFirstLiteralInstructionOrThrow(colorGrey)
        val iPutIndex = miniPlayerConstructorFingerprint.method.indexOfFirstInstructionOrThrow(
            colorGreyIndex, Opcode.IPUT
        )
        val colorMathPlayerIPutReference = miniPlayerConstructorFingerprint.method
            .getInstruction<ReferenceInstruction>(iPutIndex).reference

        miniPlayerConstructorFingerprint.classDef.methods.single {
            it.accessFlags == AccessFlags.PUBLIC.value or AccessFlags.FINAL.value &&
                    it.parameters == switchToggleColorMatch.method.parameters &&
                    it.returnType == "V"
        }.apply {
            val invokeDirectIndex =
                indexOfFirstInstructionReversedOrThrow(Opcode.INVOKE_DIRECT)

            val insertIndex = invokeDirectIndex + 1
            val freeRegister = findFreeRegister(insertIndex)

            addInstructionsAtControlFlowLabel(
                insertIndex,
                """
                    invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->changeMiniplayerColor()Z
                    move-result v$freeRegister
                    if-eqz v$freeRegister, :off
                    invoke-virtual { p1 }, $colorMathPlayerInvokeVirtualReference
                    move-result-object v$freeRegister
                    check-cast v$freeRegister, ${(colorMathPlayerIGetReference as FieldReference).definingClass}
                    iget v$freeRegister, v$freeRegister, $colorMathPlayerIGetReference
                    iput v$freeRegister, p0, $colorMathPlayerIPutReference
                    :off
                    nop
                """
            )
        }
    }
}
