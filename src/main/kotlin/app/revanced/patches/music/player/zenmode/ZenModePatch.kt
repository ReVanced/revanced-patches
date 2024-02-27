package app.revanced.patches.music.player.zenmode

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.player.zenmode.fingerprints.ZenModeFingerprint
import app.revanced.patches.music.utils.fingerprints.PlayerColorFingerprint
import app.revanced.patches.music.utils.integrations.Constants.PLAYER
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Patch(
    name = "Enable zen mode",
    description = "Adds an option to change the player background to light grey to reduce eye strain. Deprecated on YT Music 6.34.51+.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.apps.youtube.music",
            [
                "6.21.52",
                "6.22.52",
                "6.23.56",
                "6.25.53",
                "6.26.51",
                "6.27.54",
                "6.28.53",
                "6.29.58",
                "6.31.55",
                "6.33.52"
            ]
        )
    ],
    use = false
)
@Suppress("unused")
object ZenModePatch : BytecodePatch(
    setOf(PlayerColorFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        PlayerColorFingerprint.result?.let { parentResult ->
            ZenModeFingerprint.also { it.resolve(context, parentResult.classDef) }.result?.let {
                it.mutableMethod.apply {
                    val startIndex = it.scanResult.patternScanResult!!.startIndex

                    val firstRegister = getInstruction<OneRegisterInstruction>(startIndex).registerA
                    val secondRegister =
                        getInstruction<OneRegisterInstruction>(startIndex + 2).registerA
                    val dummyRegister = secondRegister + 1

                    val replaceReferenceIndex = it.scanResult.patternScanResult!!.endIndex + 1
                    val replaceReference =
                        getInstruction<ReferenceInstruction>(replaceReferenceIndex).reference

                    val insertIndex = replaceReferenceIndex + 1

                    addInstructionsWithLabels(
                        insertIndex, """
                            invoke-static {}, $PLAYER->enableZenMode()Z
                            move-result v$dummyRegister
                            if-eqz v$dummyRegister, :off
                            const v$dummyRegister, -0xfcfcfd
                            if-ne v$firstRegister, v$dummyRegister, :off
                            const v$firstRegister, -0xbfbfc0
                            const v$secondRegister, -0xbfbfc0
                            :off
                            sget-object v0, $replaceReference
                            """
                    )
                    removeInstruction(replaceReferenceIndex)
                }
            } ?: throw ZenModeFingerprint.exception
        } ?: throw PatchException("This version is not supported. Please use YT Music 6.33.52 or earlier.")

        SettingsPatch.addMusicPreference(
            CategoryType.PLAYER,
            "revanced_enable_zen_mode",
            "false"
        )

    }
}