package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.shared.layout.theme.lithoColorHookPatch
import app.revanced.patches.shared.layout.theme.lithoColorOverrideHook
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_34_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_49_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_30_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_34_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.insertLiteralOverride
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/theme/SeekbarColorPatch;"

val seekbarColorPatch = bytecodePatch(
    description = "Hide or set a custom seekbar color",
) {
    dependsOn(
        sharedExtensionPatch,
        lithoColorHookPatch,
        resourceMappingPatch,
        versionCheckPatch
    )

    execute {
        fun MutableMethod.addColorChangeInstructions(index: Int) {
            insertLiteralOverride(
                index,
                "$EXTENSION_CLASS_DESCRIPTOR->getVideoPlayerSeekbarColor(I)I"
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

        // 19.25+ changes

        var handleBarColorFingerprints = mutableListOf(playerSeekbarHandle1ColorFingerprint)
        if (!is_20_34_or_greater) {
            handleBarColorFingerprints += playerSeekbarHandle2ColorFingerprint
        }
        handleBarColorFingerprints.forEach {
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
                val index = it.instructionMatches.last().index
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
                if (is_20_30_or_greater) {
                    launchScreenLayoutTypeLotteFeatureFlag
                } else {
                    launchScreenLayoutTypeLotteFeatureLegacyFlag
                },
                "$EXTENSION_CLASS_DESCRIPTOR->useLotteLaunchSplashScreen(Z)Z"
            )
        }

        // Hook the splash animation to set the a seekbar color.
        mainActivityOnCreateFingerprint.method.apply {
            val setAnimationIntMethodName =
                lottieAnimationViewSetAnimationIntFingerprint.originalMethod.name

            findInstructionIndicesReversedOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == LOTTIE_ANIMATION_VIEW_CLASS_TYPE
                        && reference.name == setAnimationIntMethodName
            }.forEach { index ->
                val instruction = getInstruction<FiveRegisterInstruction>(index)

                replaceInstruction(
                    index,
                    "invoke-static { v${instruction.registerC}, v${instruction.registerD} }, " +
                        "$EXTENSION_CLASS_DESCRIPTOR->setSplashAnimationLottie(Lcom/airbnb/lottie/LottieAnimationView;I)V"
                )
            }
        }

        // Add non obfuscated method aliases for `setAnimation(int)`
        // and `setAnimation(InputStream, String)` so extension code can call them.
        lottieAnimationViewSetAnimationIntFingerprint.classDef.methods.apply {
            val addedMethodName = "patch_setAnimation"
            val setAnimationIntName = lottieAnimationViewSetAnimationIntFingerprint
                .originalMethod.name

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

            val factoryStreamClass: CharSequence
            val factoryStreamName: CharSequence
            val factoryStreamReturnType: CharSequence
            lottieCompositionFactoryFromJsonInputStreamFingerprint.match(
                lottieCompositionFactoryZipFingerprint.originalClassDef
            ).originalMethod.apply {
                factoryStreamClass = definingClass
                factoryStreamName = name
                factoryStreamReturnType = returnType
            }

            val lottieAnimationViewSetAnimationStreamFingerprint by fingerprint {
                accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
                parameters(factoryStreamReturnType.toString())
                returns("V")
                custom { _, classDef ->
                    classDef.type == lottieAnimationViewSetAnimationIntFingerprint.originalClassDef.type
                }
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
