package app.revanced.patches.youtube.player.filmstripoverlay

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.player.filmstripoverlay.fingerprints.FilmStripOverlayConfigFingerprint
import app.revanced.patches.youtube.player.filmstripoverlay.fingerprints.FilmStripOverlayInteractionFingerprint
import app.revanced.patches.youtube.player.filmstripoverlay.fingerprints.FilmStripOverlayParentFingerprint
import app.revanced.patches.youtube.player.filmstripoverlay.fingerprints.FilmStripOverlayPreviewFingerprint
import app.revanced.patches.youtube.player.filmstripoverlay.fingerprints.FineScrubbingOverlayFingerprint
import app.revanced.patches.youtube.utils.controlsoverlay.DisableControlsOverlayConfigPatch
import app.revanced.patches.youtube.utils.integrations.Constants.PLAYER
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Hide filmstrip overlay",
    description = "Adds an option to hide filmstrip overlay in the video player.",
    dependencies = [
        DisableControlsOverlayConfigPatch::class,
        SettingsPatch::class,
        SharedResourceIdPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object HideFilmstripOverlayPatch : BytecodePatch(
    setOf(
        FilmStripOverlayParentFingerprint,
        FineScrubbingOverlayFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        FilmStripOverlayParentFingerprint.result?.classDef?.let { classDef ->
            arrayOf(
                FilmStripOverlayConfigFingerprint,
                FilmStripOverlayInteractionFingerprint,
                FilmStripOverlayPreviewFingerprint
            ).forEach { fingerprint ->
                fingerprint.also {
                    it.resolve(
                        context,
                        classDef
                    )
                }.result?.mutableMethod?.injectHook()
                    ?: throw fingerprint.exception
            }
        } ?: throw FilmStripOverlayParentFingerprint.exception

        FineScrubbingOverlayFingerprint.result?.let {
            it.mutableMethod.apply {
                if (SettingsPatch.upward1828) {
                    var insertIndex = it.scanResult.patternScanResult!!.startIndex + 2
                    val jumpIndex = getTargetIndexUpTo(insertIndex, Opcode.GOTO, Opcode.GOTO_16)
                    val initialIndex = jumpIndex - 1

                    if (getInstruction(insertIndex).opcode == Opcode.INVOKE_VIRTUAL)
                        insertIndex++

                    val replaceInstruction = getInstruction<TwoRegisterInstruction>(insertIndex)
                    val replaceReference =
                        getInstruction<ReferenceInstruction>(insertIndex).reference

                    addComponentUpward1828(insertIndex, initialIndex)

                    addInstructionsWithLabels(
                        insertIndex + 1, fixComponent + """
                            invoke-static {}, $PLAYER->hideFilmstripOverlay()Z
                            move-result v${replaceInstruction.registerA}
                            if-nez v${replaceInstruction.registerA}, :hidden
                            iget-object v${replaceInstruction.registerA}, v${replaceInstruction.registerB}, $replaceReference
                            """, ExternalLabel("hidden", getInstruction(jumpIndex))
                    )
                    removeInstruction(insertIndex)
                } else {
                    val setOnClickListenerIndex = getIndex("setOnClickListener")
                    val jumpIndex = setOnClickListenerIndex + 3
                    val initialIndex = setOnClickListenerIndex - 1

                    val insertIndex = getIndex("bringChildToFront") + 1
                    val insertRegister =
                        getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                    addComponentBelow1828(insertIndex, initialIndex)

                    addInstructionsWithLabels(
                        insertIndex, fixComponent + """
                            invoke-static {}, $PLAYER->hideFilmstripOverlay()Z
                            move-result v$insertRegister
                            if-nez v$insertRegister, :hidden
                            """, ExternalLabel("hidden", getInstruction(jumpIndex))
                    )
                }
            }
        } ?: throw FineScrubbingOverlayFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: PLAYER_SETTINGS",
                "SETTINGS: PLAYER_EXPERIMENTAL_FLAGS",
                "SETTINGS: HIDE_FILMSTRIP_OVERLAY"
            )
        )

        SettingsPatch.updatePatchStatus("Hide filmstrip overlay")

    }

    private var fixComponent: String = ""

    private fun MutableMethod.addComponentBelow1828(
        startIndex: Int,
        endIndex: Int
    ) {
        val fixRegister =
            getInstruction<FiveRegisterInstruction>(endIndex).registerE

        for (index in endIndex downTo startIndex) {
            val opcode = getInstruction(index).opcode
            if (opcode != Opcode.CONST_16)
                continue

            val register = getInstruction<OneRegisterInstruction>(index).registerA

            if (register != fixRegister)
                continue

            val fixValue = getInstruction<WideLiteralInstruction>(index).wideLiteral.toInt()

            fixComponent = "const/16 v$fixRegister, $fixValue"

            break
        }
    }

    private fun MutableMethod.addComponentUpward1828(
        startIndex: Int,
        endIndex: Int
    ) {
        for (index in startIndex..endIndex) {
            val opcode = getInstruction(index).opcode
            if (opcode != Opcode.CONST_16 && opcode != Opcode.CONST_4 && opcode != Opcode.CONST)
                continue

            val register = getInstruction<OneRegisterInstruction>(index).registerA
            val value = getInstruction<WideLiteralInstruction>(index).wideLiteral.toInt()

            val line =
                when (opcode) {
                    Opcode.CONST_16 -> """
                            const/16 v$register, $value
                            
                            """.trimIndent()

                    Opcode.CONST_4 -> """
                            const/4 v$register, $value
                            
                            """.trimIndent()

                    Opcode.CONST -> """
                            const v$register, $value
                            
                            """.trimIndent()

                    else -> ""
                }

            fixComponent += line
        }
    }

    private fun MutableMethod.getTargetIndexUpTo(
        startIndex: Int,
        opcode1: Opcode,
        opcode2: Opcode
    ): Int {
        for (index in startIndex until implementation!!.instructions.size) {
            if (getInstruction(index).opcode != opcode1 && getInstruction(index).opcode != opcode2)
                continue

            return index
        }
        throw PatchException("Failed to find hook method")
    }

    private fun MutableMethod.injectHook() {
        addInstructionsWithLabels(
            0, """
                    invoke-static {}, $PLAYER->hideFilmstripOverlay()Z
                    move-result v0
                    if-eqz v0, :shown
                    const/4 v0, 0x0
                    return v0
                    """, ExternalLabel("shown", getInstruction(0))
        )
    }

    private fun MutableMethod.getIndex(methodName: String): Int {
        return implementation!!.instructions.indexOfFirst { instruction ->
            if (instruction.opcode != Opcode.INVOKE_VIRTUAL) return@indexOfFirst false

            return@indexOfFirst ((instruction as Instruction35c).reference as MethodReference).name == methodName
        }
    }
}
