package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.youtube.layout.theme.lithoColorHookPatch
import app.revanced.patches.youtube.layout.theme.lithoColorOverrideHook
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.indexOfFirstWideLiteralInstructionValueOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import org.w3c.dom.Element

internal var reelTimeBarPlayedColorId = -1L
    private set
internal var inlineTimeBarColorizedBarPlayedColorDarkId = -1L
    private set
internal var inlineTimeBarPlayedNotHighlightedColorId = -1L
    private set

private val seekbarColorResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
    )

    execute { context ->
        reelTimeBarPlayedColorId = resourceMappings[
            "color",
            "reel_time_bar_played_color",
        ]
        inlineTimeBarColorizedBarPlayedColorDarkId = resourceMappings[
            "color",
            "inline_time_bar_colorized_bar_played_color_dark",
        ]
        inlineTimeBarPlayedNotHighlightedColorId = resourceMappings[
            "color",
            "inline_time_bar_played_not_highlighted_color",
        ]

        // Edit the resume playback drawable and replace the progress bar with a custom drawable
        context.document["res/drawable/resume_playback_progressbar_drawable.xml"].use { document ->

            val layerList = document.getElementsByTagName("layer-list").item(0) as Element
            val progressNode = layerList.getElementsByTagName("item").item(1) as Element
            if (!progressNode.getAttributeNode("android:id").value.endsWith("progress")) {
                throw PatchException("Could not find progress bar")
            }
            val scaleNode = progressNode.getElementsByTagName("scale").item(0) as Element
            val shapeNode = scaleNode.getElementsByTagName("shape").item(0) as Element
            val replacementNode = document.createElement(
                "app.revanced.extension.youtube.patches.theme.ProgressBarDrawable",
            )
            scaleNode.replaceChild(replacementNode, shapeNode)
        }
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/theme/SeekbarColorPatch;"

val seekbarColorPatch = bytecodePatch(
    description = "Hide or set a custom seekbar color",
) {
    dependsOn(
        sharedExtensionPatch,
        lithoColorHookPatch,
        seekbarColorResourcePatch,
    )

    val playerSeekbarColorMatch by playerSeekbarColorFingerprint()
    val shortsSeekbarColorMatch by shortsSeekbarColorFingerprint()
    val setSeekbarClickedColorMatch by setSeekbarClickedColorFingerprint()

    execute { context ->
        fun MutableMethod.addColorChangeInstructions(resourceId: Long) {
            val registerIndex = indexOfFirstWideLiteralInstructionValueOrThrow(resourceId) + 2
            val colorRegister = getInstruction<OneRegisterInstruction>(registerIndex).registerA
            addInstructions(
                registerIndex + 1,
                """
                    invoke-static { v$colorRegister }, $EXTENSION_CLASS_DESCRIPTOR->getVideoPlayerSeekbarColor(I)I
                    move-result v$colorRegister
                """,
            )
        }

        playerSeekbarColorMatch.mutableMethod.apply {
            addColorChangeInstructions(inlineTimeBarColorizedBarPlayedColorDarkId)
            addColorChangeInstructions(inlineTimeBarPlayedNotHighlightedColorId)
        }

        shortsSeekbarColorMatch.mutableMethod.apply {
            addColorChangeInstructions(reelTimeBarPlayedColorId)
        }

        setSeekbarClickedColorMatch.mutableMethod.let {
            val setColorMethodIndex = setSeekbarClickedColorMatch.patternMatch!!.startIndex + 1
            val method = context.navigate(it).at(setColorMethodIndex).mutable()

            method.apply {
                val colorRegister = getInstruction<TwoRegisterInstruction>(0).registerA
                addInstructions(
                    0,
                    """
                        invoke-static { v$colorRegister }, $EXTENSION_CLASS_DESCRIPTOR->getVideoPlayerSeekbarClickedColor(I)I
                        move-result v$colorRegister
                    """,
                )
            }
        }

        lithoColorOverrideHook(EXTENSION_CLASS_DESCRIPTOR, "getLithoColor")
    }
}
