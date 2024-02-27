package app.revanced.patches.youtube.seekbar.color

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.patch.litho.LithoThemePatch
import app.revanced.patches.youtube.seekbar.color.fingerprints.ControlsOverlayStyleFingerprint
import app.revanced.patches.youtube.seekbar.color.fingerprints.ShortsSeekbarColorFingerprint
import app.revanced.patches.youtube.utils.fingerprints.PlayerSeekbarColorFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.SEEKBAR
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.InlineTimeBarColorizedBarPlayedColorDark
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.InlineTimeBarPlayedNotHighlightedColor
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ReelTimeBarPlayedColor
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch.contexts
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import org.w3c.dom.Element

@Patch(
    name = "Custom seekbar color",
    description = "Adds an option to customize seekbar colors in video players and video thumbnails.",
    dependencies = [
        LithoThemePatch::class,
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
object SeekbarColorPatch : BytecodePatch(
    setOf(
        ControlsOverlayStyleFingerprint,
        PlayerSeekbarColorFingerprint,
        ShortsSeekbarColorFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        PlayerSeekbarColorFingerprint.result?.mutableMethod?.apply {
            hook(getWideLiteralInstructionIndex(InlineTimeBarColorizedBarPlayedColorDark) + 2)
            hook(getWideLiteralInstructionIndex(InlineTimeBarPlayedNotHighlightedColor) + 2)
        } ?: throw PlayerSeekbarColorFingerprint.exception

        ShortsSeekbarColorFingerprint.result?.mutableMethod?.apply {
            hook(getWideLiteralInstructionIndex(ReelTimeBarPlayedColor) + 2)
        } ?: throw ShortsSeekbarColorFingerprint.exception

        ControlsOverlayStyleFingerprint.result?.let {
            with(
                context
                    .toMethodWalker(it.method)
                    .nextMethod(it.scanResult.patternScanResult!!.startIndex + 1, true)
                    .getMethod() as MutableMethod
            ) {
                val colorRegister = getInstruction<TwoRegisterInstruction>(0).registerA

                addInstructions(
                    0, """
                    invoke-static {v$colorRegister}, $SEEKBAR->getSeekbarClickedColorValue(I)I
                    move-result v$colorRegister
                    """
                )
            }
        } ?: throw ControlsOverlayStyleFingerprint.exception

        LithoThemePatch.injectCall("$SEEKBAR->getLithoColor(I)I")

        contexts.xmlEditor["res/drawable/resume_playback_progressbar_drawable.xml"].use {
            val layerList = it.file.getElementsByTagName("layer-list").item(0) as Element
            val progressNode = layerList.getElementsByTagName("item").item(1) as Element
            if (!progressNode.getAttributeNode("android:id").value.endsWith("progress")) {
                throw PatchException("Could not find progress bar")
            }
            val scaleNode = progressNode.getElementsByTagName("scale").item(0) as Element
            val shapeNode = scaleNode.getElementsByTagName("shape").item(0) as Element
            val replacementNode = it.file.createElement(
                "app.revanced.integrations.youtube.patches.utils.ProgressBarDrawable"
            )
            scaleNode.replaceChild(replacementNode, shapeNode)
        }

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: SEEKBAR_SETTINGS",
                "SETTINGS: CUSTOM_SEEKBAR_COLOR"
            )
        )

        SettingsPatch.updatePatchStatus("Custom seekbar color")

    }

    private fun MutableMethod.hook(insertIndex: Int) {
        val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

        addInstructions(
            insertIndex + 1, """
                invoke-static {v$insertRegister}, $SEEKBAR->overrideSeekbarColor(I)I
                move-result v$insertRegister
                """
        )
    }
}
