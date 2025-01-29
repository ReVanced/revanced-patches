package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.youtube.layout.theme.lithoColorHookPatch
import app.revanced.patches.youtube.layout.theme.lithoColorOverrideHook
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_34_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_46_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_49_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.util.copyXmlNode
import app.revanced.util.findElementByAttributeValueOrThrow
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.inputStreamFromBundledResource
import app.revanced.util.insertFeatureFlagBooleanOverride
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import kotlin.use

private const val splashSeekbarColorAttributeName = "splash_custom_seekbar_color"

private val seekbarColorResourcePatch = resourcePatch {
    dependsOn(
        resourceMappingPatch,
        versionCheckPatch,
    )

    execute {
        // Modify the resume playback drawable and replace the progress bar with a custom drawable.
        document("res/drawable/resume_playback_progressbar_drawable.xml").use { document ->
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


        if (!is_19_25_or_greater) {
            return@execute
        }

        // Add attribute and styles for splash screen custom color.
        // Using a style is the only way to selectively change just the seekbar fill color.
        //
        // Because the style colors must be hard coded for all color possibilities,
        // instead of allowing 24 bit color the style is restricted to 9-bit (3 bits per color channel)
        // and the style color closest to the users custom color is used for the splash screen.
        arrayOf(
            inputStreamFromBundledResource("seekbar/values", "attrs.xml")!! to "res/values/attrs.xml",
            ByteArrayInputStream(create9BitSeekbarColorStyles().toByteArray()) to "res/values/styles.xml"
        ).forEach { (source, destination) ->
            "resources".copyXmlNode(
                document(source),
                document(destination),
            ).close()
        }

        fun setSplashDrawablePathFillColor(xmlFileNames: Iterable<String>, vararg resourceNames: String) {
            xmlFileNames.forEach { xmlFileName ->
                document(xmlFileName).use { document ->
                    resourceNames.forEach { elementId ->
                        val element = document.childNodes.findElementByAttributeValueOrThrow(
                            "android:name",
                            elementId
                        )

                        val attribute = "android:fillColor"
                        if (!element.hasAttribute(attribute)) {
                            throw PatchException("Could not find $attribute for $elementId")
                        }

                        element.setAttribute(attribute, "?attr/$splashSeekbarColorAttributeName")
                    }
                }
            }
        }

        setSplashDrawablePathFillColor(
            listOf(
                "res/drawable/\$startup_animation_light__0.xml",
                "res/drawable/\$startup_animation_dark__0.xml"
            ),
            "_R_G_L_10_G_D_0_P_0"
        )

        if (!is_19_46_or_greater) {
            // Resources removed in 19.46+
            setSplashDrawablePathFillColor(
                listOf(
                    "res/drawable/\$buenos_aires_animation_light__0.xml",
                    "res/drawable/\$buenos_aires_animation_dark__0.xml"
                ),
                "_R_G_L_8_G_D_0_P_0"
            )
        }
    }
}

/**
 * Generate a style xml with all combinations of 9-bit colors.
 */
private fun create9BitSeekbarColorStyles(): String = StringBuilder().apply {
    append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
    append("<resources>\n")

    for (red in 0..7) {
        for (green in 0..7) {
            for (blue in 0..7) {
                val name = "${red}_${green}_${blue}"

                fun roundTo3BitHex(channel8Bits: Int) =
                    (channel8Bits * 255 / 7).toString(16).padStart(2, '0')
                val r = roundTo3BitHex(red)
                val g = roundTo3BitHex(green)
                val b = roundTo3BitHex(blue)
                val color = "#ff$r$g$b"

                append(
                    """
                        <style name="splash_seekbar_color_style_$name">
                            <item name="$splashSeekbarColorAttributeName">$color</item>
                        </style>
                    """
                )
            }
        }
    }

    append("</resources>")
}.toString()

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/theme/SeekbarColorPatch;"

val seekbarColorPatch = bytecodePatch(
    description = "Hide or set a custom seekbar color",
) {
    dependsOn(
        sharedExtensionPatch,
        lithoColorHookPatch,
        seekbarColorResourcePatch,
        resourceMappingPatch,
        versionCheckPatch
    )

    execute {
        fun MutableMethod.addColorChangeInstructions(resourceIndex: Int) {
            val moveResultIndex = indexOfFirstInstructionOrThrow(resourceIndex, Opcode.MOVE_RESULT)
            val register = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

            addInstructions(
                moveResultIndex + 1,
                """
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getVideoPlayerSeekbarColor(I)I
                    move-result v$register
                """
            )
        }

        playerSeekbarColorFingerprint.let {
            it.method.apply {
                addColorChangeInstructions(it.instructionMatches.last().index)
                addColorChangeInstructions(it.instructionMatches.first().index)
            }
        }

        shortsSeekbarColorFingerprint.let {
            it.method.addColorChangeInstructions(it.instructionMatches.first().index)
        }

        setSeekbarClickedColorFingerprint.originalMethod.let {
            val setColorMethodIndex = setSeekbarClickedColorFingerprint.instructionMatches.first().index + 1

            navigate(it).to(setColorMethodIndex).stop().apply {
                val colorRegister = getInstruction<TwoRegisterInstruction>(0).registerA
                addInstructions(
                    0,
                    """
                        invoke-static { v$colorRegister }, $EXTENSION_CLASS_DESCRIPTOR->getVideoPlayerSeekbarClickedColor(I)I
                        move-result v$colorRegister
                    """
                )
            }
        }

        lithoColorOverrideHook(EXTENSION_CLASS_DESCRIPTOR, "getLithoColor")

        if (!is_19_25_or_greater) {
            return@execute
        }

        // 19.25+ changes

        arrayOf(
            playerSeekbarHandle1ColorFingerprint,
            playerSeekbarHandle2ColorFingerprint
        ).forEach {
            it.method.addColorChangeInstructions(it.instructionMatches.last().index)
        }

        // If hiding feed seekbar thumbnails, then turn off the cairo gradient
        // of the watch history menu items as they use the same gradient as the
        // player and there is no easy way to distinguish which to use a transparent color.
        if (is_19_34_or_greater) {
            watchHistoryMenuUseProgressDrawableFingerprint.let {
                it.method.apply {
                    val index = it.instructionMatches[1].index
                    val register = getInstruction<OneRegisterInstruction>(index).registerA

                    addInstructions(
                        index + 1,
                        """
                            invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->showWatchHistoryProgressDrawable(Z)Z
                            move-result v$register            
                        """
                    )
                }
            }
        }

        lithoLinearGradientFingerprint.method.addInstructions(
            0,
            """
                invoke-static/range { p4 .. p5 },  $EXTENSION_CLASS_DESCRIPTOR->getLithoLinearGradient([I[F)[I
                move-result-object p4   
            """
        )

        val playerFingerprint =
            if (is_19_49_or_greater) {
                playerLinearGradientFingerprint
            } else if (is_19_46_or_greater) {
                playerLinearGradientLegacy1946Fingerprint
            } else {
                playerLinearGradientLegacy1925Fingerprint
            }

        playerFingerprint.let {
            it.method.apply {
                val index = it.instructionMatches.last().index
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstructions(
                    index + 1,
                    """
                       invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getPlayerLinearGradient([I)[I
                       move-result-object v$register
                    """
                )
            }
        }

        // region apply seekbar custom color to splash screen animation.

        // Hook the splash animation drawable to set the a seekbar color theme.
        mainActivityOnCreateSplashScreenImageViewFingerprint.let {
            it.method.apply {
                val checkCastIndex = it.instructionMatches.last().index
                val drawableRegister = getInstruction<OneRegisterInstruction>(checkCastIndex).registerA

                addInstruction(
                    checkCastIndex + 1,
                    "invoke-static { v$drawableRegister }, $EXTENSION_CLASS_DESCRIPTOR->" +
                            "setSplashAnimationDrawableTheme(Landroid/graphics/drawable/AnimatedVectorDrawable;)V"
                )
            }
        }

        // Don't use the lotte splash screen layout if using custom seekbar.
        arrayOf(
            launchScreenLayoutTypeFingerprint,
            mainActivityOnCreateSplashScreenImageViewFingerprint
        ).forEach { fingerprint ->
            fingerprint.method.insertFeatureFlagBooleanOverride(
                launchScreenLayoutTypeLotteFeatureFlag,
                "$EXTENSION_CLASS_DESCRIPTOR->useLotteLaunchSplashScreen(Z)Z"
            )
        }

        // endregion
    }
}
