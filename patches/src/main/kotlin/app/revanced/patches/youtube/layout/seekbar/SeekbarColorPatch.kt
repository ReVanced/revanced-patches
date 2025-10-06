package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.shared.layout.theme.lithoColorHookPatch
import app.revanced.patches.shared.layout.theme.lithoColorOverrideHook
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_34_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_46_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_49_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint
import app.revanced.util.copyXmlNode
import app.revanced.util.findElementByAttributeValueOrThrow
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.inputStreamFromBundledResource
import app.revanced.util.insertLiteralOverride
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import kotlin.use

internal var reelTimeBarPlayedColorId = -1L
    private set
internal var inlineTimeBarColorizedBarPlayedColorDarkId = -1L
    private set
internal var inlineTimeBarPlayedNotHighlightedColorId = -1L
    private set
internal var ytYoutubeMagentaColorId = -1L
    private set
internal var ytStaticBrandRedId = -1L
    private set
internal var ytTextSecondaryId = -1L
    private set
internal var inlineTimeBarLiveSeekableRangeId = -1L
    private set

internal const val splashSeekbarColorAttributeName = "splash_custom_seekbar_color"

private val seekbarColorResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        versionCheckPatch,
    )

    execute {
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
        ytStaticBrandRedId = resourceMappings[
            "attr",
            "ytStaticBrandRed"
        ]
        ytTextSecondaryId = resourceMappings[
            "attr",
            "ytTextSecondary"
        ]
        inlineTimeBarLiveSeekableRangeId = resourceMappings[
            "color",
            "inline_time_bar_live_seekable_range"
        ]

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

        ytYoutubeMagentaColorId = resourceMappings[
            "color",
            "yt_youtube_magenta",
        ]
        ytStaticBrandRedId = resourceMappings[
            "attr",
            "ytStaticBrandRed",
        ]

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
                    val childNodes = document.childNodes

                    resourceNames.forEach { elementId ->
                        val element = childNodes.findElementByAttributeValueOrThrow(
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
        versionCheckPatch
    )

    execute {
        fun MutableMethod.addColorChangeInstructions(resourceId: Long) {
            insertLiteralOverride(
                resourceId,
                "$EXTENSION_CLASS_DESCRIPTOR->getVideoPlayerSeekbarColor(I)I"
            )
        }

        playerSeekbarColorFingerprint.method.apply {
            addColorChangeInstructions(inlineTimeBarColorizedBarPlayedColorDarkId)
            addColorChangeInstructions(inlineTimeBarPlayedNotHighlightedColorId)
        }

        shortsSeekbarColorFingerprint.method.apply {
            addColorChangeInstructions(reelTimeBarPlayedColorId)
        }

        setSeekbarClickedColorFingerprint.originalMethod.let {
            val setColorMethodIndex = setSeekbarClickedColorFingerprint.patternMatch!!.startIndex + 1

            navigate(it).to(setColorMethodIndex).stop().apply {
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

        // 19.25+ changes

        arrayOf(
            playerSeekbarHandle1ColorFingerprint,
            playerSeekbarHandle2ColorFingerprint
        ).forEach {
            it.method.addColorChangeInstructions(ytStaticBrandRedId)
        }

        // If hiding feed seekbar thumbnails, then turn off the cairo gradient
        // of the watch history menu items as they use the same gradient as the
        // player and there is no easy way to distinguish which to use a transparent color.
        if (is_19_34_or_greater) {
            watchHistoryMenuUseProgressDrawableFingerprint.method.apply {
                val progressIndex = indexOfFirstInstructionOrThrow {
                    val reference = getReference<MethodReference>()
                    reference?.definingClass == "Landroid/widget/ProgressBar;" && reference.name == "setMax"
                }
                val index = indexOfFirstInstructionOrThrow(progressIndex, Opcode.MOVE_RESULT)
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

        lithoLinearGradientFingerprint.method.addInstructions(
            0,
            """
                invoke-static/range { p4 .. p5 },  $EXTENSION_CLASS_DESCRIPTOR->getLithoLinearGradient([I[F)[I
                move-result-object p4   
            """
        )

        val playerFingerprint: Fingerprint
        val checkGradientCoordinates: Boolean
        if (is_19_49_or_greater) {
            playerFingerprint = playerLinearGradientFingerprint
            checkGradientCoordinates = true
        } else {
            playerFingerprint = playerLinearGradientLegacyFingerprint
            checkGradientCoordinates = false
        }

        playerFingerprint.let {
            it.method.apply {
                val index = it.patternMatch!!.endIndex
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstructions(
                    index + 1,
                    if (checkGradientCoordinates) {
                        """
                           invoke-static { v$register, p0, p1 }, $EXTENSION_CLASS_DESCRIPTOR->getPlayerLinearGradient([III)[I
                           move-result-object v$register
                        """
                    } else {
                        """
                           invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getPlayerLinearGradient([I)[I
                           move-result-object v$register
                        """
                    }
                )
            }
        }

        // region apply seekbar custom color to splash screen animation.

        if (!is_19_34_or_greater) {
            return@execute // 19.25 does not have a cairo launch animation.
        }

        // Add development hook to force old drawable splash animation.
        arrayOf(
            launchScreenLayoutTypeFingerprint,
            mainActivityOnCreateFingerprint
        ).forEach { fingerprint ->
            fingerprint.method.insertLiteralOverride(
                launchScreenLayoutTypeLotteFeatureFlag,
                "$EXTENSION_CLASS_DESCRIPTOR->useLotteLaunchSplashScreen(Z)Z"
            )
        }

        // Hook the splash animation to set the a seekbar color.
        mainActivityOnCreateFingerprint.method.apply {
            val drawableIndex = indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == "Landroid/widget/ImageView;"
                        && reference.name == "getDrawable"
            }
            val checkCastIndex = indexOfFirstInstructionOrThrow(drawableIndex, Opcode.CHECK_CAST)
            val drawableRegister = getInstruction<OneRegisterInstruction>(checkCastIndex).registerA

            addInstruction(
                checkCastIndex + 1,
                "invoke-static { v$drawableRegister }, $EXTENSION_CLASS_DESCRIPTOR->" +
                        "setSplashAnimationDrawableTheme(Landroid/graphics/drawable/AnimatedVectorDrawable;)V"
            )

            // Replace the Lottie animation view setAnimation(int) call.
            val setAnimationIntMethodName = lottieAnimationViewSetAnimationIntFingerprint.originalMethod.name

            findInstructionIndicesReversedOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == "Lcom/airbnb/lottie/LottieAnimationView;"
                        && reference.name == setAnimationIntMethodName
            }.forEach { index ->
                val instruction = getInstruction<FiveRegisterInstruction>(index)

                replaceInstruction(
                    index,
                    "invoke-static { v${instruction.registerC}, v${instruction.registerD} }, " +
                            "$EXTENSION_CLASS_DESCRIPTOR->setSplashAnimationLottie" +
                            "(Lcom/airbnb/lottie/LottieAnimationView;I)V"
                )
            }
        }


        // Add non obfuscated method aliases for `setAnimation(int)`
        // and `setAnimation(InputStream, String)` so extension code can call them.
        lottieAnimationViewSetAnimationIntFingerprint.classDef.methods.apply {
            val addedMethodName = "patch_setAnimation"
            val setAnimationIntName = lottieAnimationViewSetAnimationIntFingerprint.originalMethod.name

            add(ImmutableMethod(
                LOTTIE_ANIMATION_VIEW_CLASS_TYPE,
                addedMethodName,
                listOf(ImmutableMethodParameter("I", null, null)),
                "V",
                AccessFlags.PUBLIC.value,
                null,
                null,
                MutableMethodImplementation(2),
            ).toMutable().apply {
                addInstructions(
                    """
                        invoke-virtual { p0, p1 }, Lcom/airbnb/lottie/LottieAnimationView;->$setAnimationIntName(I)V
                        return-void
                    """
                )
            })

            val factoryStreamClass : CharSequence
            val factoryStreamName : CharSequence
            val factoryStreamReturnType : CharSequence
            lottieCompositionFactoryFromJsonInputStreamFingerprint.match(
                lottieCompositionFactoryZipFingerprint.originalClassDef
            ).originalMethod.apply {
                factoryStreamClass = definingClass
                factoryStreamName = name
                factoryStreamReturnType = returnType
            }

            val setAnimationStreamName = lottieAnimationViewSetAnimationStreamFingerprint
                .originalMethod.name

            add(ImmutableMethod(
                LOTTIE_ANIMATION_VIEW_CLASS_TYPE,
                addedMethodName,
                listOf(
                    ImmutableMethodParameter("Ljava/io/InputStream;", null, null),
                    ImmutableMethodParameter("Ljava/lang/String;", null, null)
                ),
                "V",
                AccessFlags.PUBLIC.value,
                null,
                null,
                MutableMethodImplementation(4),
            ).toMutable().apply {
                addInstructions(
                    """
                        invoke-static { p1, p2 }, $factoryStreamClass->$factoryStreamName(Ljava/io/InputStream;Ljava/lang/String;)$factoryStreamReturnType
                        move-result-object v0
                        invoke-virtual { p0, v0}, Lcom/airbnb/lottie/LottieAnimationView;->$setAnimationStreamName($factoryStreamReturnType)V
                        return-void
                    """
                )
            })
        }

        // endregion
    }
}
