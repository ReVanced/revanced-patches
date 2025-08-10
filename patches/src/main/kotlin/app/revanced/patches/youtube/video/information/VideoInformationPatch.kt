package app.revanced.patches.youtube.video.information

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.shared.videoQualityChangedFingerprint
import app.revanced.patches.youtube.video.playerresponse.Hook
import app.revanced.patches.youtube.video.playerresponse.addPlayerResponseMethodHook
import app.revanced.patches.youtube.video.playerresponse.playerResponseMethodHookPatch
import app.revanced.patches.youtube.video.videoid.hookBackgroundPlayVideoId
import app.revanced.patches.youtube.video.videoid.hookPlayerResponseVideoId
import app.revanced.patches.youtube.video.videoid.hookVideoId
import app.revanced.patches.youtube.video.videoid.videoIdPatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.addStaticFieldToExtension
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.util.MethodUtil

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/VideoInformation;"
private const val EXTENSION_PLAYER_INTERFACE =
    "Lapp/revanced/extension/youtube/patches/VideoInformation\$PlaybackController;"
private const val EXTENSION_VIDEO_QUALITY_MENU_INTERFACE =
    "Lapp/revanced/extension/youtube/patches/VideoInformation\$VideoQualityMenuInterface;"

private lateinit var playerInitMethod: MutableMethod
private var playerInitInsertIndex = -1
private var playerInitInsertRegister = -1

private lateinit var mdxInitMethod: MutableMethod
private var mdxInitInsertIndex = -1
private var mdxInitInsertRegister = -1

private lateinit var timeMethod: MutableMethod
private var timeInitInsertIndex = 2

// Old speed menu, where speeds are entries in a list.  Method is also used by the player speed button.
private lateinit var legacySpeedSelectionInsertMethod: MutableMethod
private var legacySpeedSelectionInsertIndex = -1
private var legacySpeedSelectionValueRegister = -1

// New speed menu, with preset buttons and 0.05x fine adjustments buttons.
private lateinit var speedSelectionInsertMethod: MutableMethod
private var speedSelectionInsertIndex = -1
private var speedSelectionValueRegister = -1

// Change playback speed method.
private lateinit var setPlaybackSpeedMethod: MutableMethod
private var setPlaybackSpeedMethodIndex = -1

// Used by other patches.
internal lateinit var setPlaybackSpeedContainerClassFieldReference: FieldReference
    private set
internal lateinit var setPlaybackSpeedClassFieldReference: FieldReference
    private set
internal lateinit var setPlaybackSpeedMethodReference: MethodReference
    private set

val videoInformationPatch = bytecodePatch(
    description = "Hooks YouTube to get information about the current playing video.",
) {
    dependsOn(
        sharedExtensionPatch,
        videoIdPatch,
        playerResponseMethodHookPatch,
    )

    execute {
        playerInitMethod = playerInitFingerprint.classDef.methods.first { MethodUtil.isConstructor(it) }

        // Find the location of the first invoke-direct call and extract the register storing the 'this' object reference.
        val initThisIndex = playerInitMethod.indexOfFirstInstructionOrThrow {
            opcode == Opcode.INVOKE_DIRECT && getReference<MethodReference>()?.name == "<init>"
        }
        playerInitInsertRegister = playerInitMethod.getInstruction<FiveRegisterInstruction>(initThisIndex).registerC
        playerInitInsertIndex = initThisIndex + 1

        val seekFingerprintResultMethod = seekFingerprint.match(playerInitFingerprint.originalClassDef).method
        val seekRelativeFingerprintResultMethod =
            seekRelativeFingerprint.match(playerInitFingerprint.originalClassDef).method

        // Create extension interface methods.
        addSeekInterfaceMethods(
            playerInitFingerprint.classDef,
            seekFingerprintResultMethod,
            seekRelativeFingerprintResultMethod,
        )

        with(mdxPlayerDirectorSetVideoStageFingerprint) {
            mdxInitMethod = classDef.methods.first { MethodUtil.isConstructor(it) }

            val initThisIndex = mdxInitMethod.indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_DIRECT && getReference<MethodReference>()?.name == "<init>"
            }
            mdxInitInsertRegister = mdxInitMethod.getInstruction<FiveRegisterInstruction>(initThisIndex).registerC
            mdxInitInsertIndex = initThisIndex + 1

            // Hook the MDX director for use through the extension.
            onCreateHookMdx(EXTENSION_CLASS_DESCRIPTOR, "initializeMdx")

            val mdxSeekFingerprintResultMethod = mdxSeekFingerprint.match(classDef).method
            val mdxSeekRelativeFingerprintResultMethod = mdxSeekRelativeFingerprint.match(classDef).method

            addSeekInterfaceMethods(classDef, mdxSeekFingerprintResultMethod, mdxSeekRelativeFingerprintResultMethod)
        }

        with(createVideoPlayerSeekbarFingerprint) {
            val videoLengthMethodMatch = videoLengthFingerprint.match(originalClassDef)

            videoLengthMethodMatch.method.apply {
                val videoLengthRegisterIndex = videoLengthMethodMatch.patternMatch!!.endIndex - 2
                val videoLengthRegister = getInstruction<OneRegisterInstruction>(videoLengthRegisterIndex).registerA
                val dummyRegisterForLong = videoLengthRegister + 1 // required for long values since they are wide

                addInstruction(
                    videoLengthMethodMatch.patternMatch!!.endIndex,
                    "invoke-static {v$videoLengthRegister, v$dummyRegisterForLong}, " +
                        "$EXTENSION_CLASS_DESCRIPTOR->setVideoLength(J)V",
                )
            }
        }

        /*
         * Inject call for video ids
         */
        val videoIdMethodDescriptor = "$EXTENSION_CLASS_DESCRIPTOR->setVideoId(Ljava/lang/String;)V"
        hookVideoId(videoIdMethodDescriptor)
        hookBackgroundPlayVideoId(videoIdMethodDescriptor)
        hookPlayerResponseVideoId(
            "$EXTENSION_CLASS_DESCRIPTOR->setPlayerResponseVideoId(Ljava/lang/String;Z)V",
        )
        // Call before any other video id hooks,
        // so they can use VideoInformation and check if the video id is for a Short.
        addPlayerResponseMethodHook(
            Hook.ProtoBufferParameterBeforeVideoId(
                "$EXTENSION_CLASS_DESCRIPTOR->" +
                    "newPlayerResponseSignature(Ljava/lang/String;Ljava/lang/String;Z)Ljava/lang/String;",
            ),
        )

        /*
         * Set the video time method
         */
        timeMethod = navigate(playerControllerSetTimeReferenceFingerprint.originalMethod)
            .to(playerControllerSetTimeReferenceFingerprint.patternMatch!!.startIndex)
            .stop()

        /*
         * Hook the methods which set the time
         */
        videoTimeHook(EXTENSION_CLASS_DESCRIPTOR, "setVideoTime")

        /*
         * Hook the user playback speed selection.
         */
        onPlaybackSpeedItemClickFingerprint.method.apply {
            val speedSelectionValueInstructionIndex = indexOfFirstInstructionOrThrow(Opcode.IGET)

            legacySpeedSelectionInsertMethod = this
            legacySpeedSelectionInsertIndex = speedSelectionValueInstructionIndex + 1
            legacySpeedSelectionValueRegister =
                getInstruction<TwoRegisterInstruction>(speedSelectionValueInstructionIndex).registerA

            setPlaybackSpeedMethodReference =
                getInstruction<ReferenceInstruction>(speedSelectionValueInstructionIndex + 2).reference as MethodReference
            setPlaybackSpeedClassFieldReference =
                getInstruction<ReferenceInstruction>(speedSelectionValueInstructionIndex + 1).reference as FieldReference
            setPlaybackSpeedContainerClassFieldReference =
                getInstruction<ReferenceInstruction>(indexOfFirstInstructionOrThrow(Opcode.IF_EQZ) - 1).reference as FieldReference

            setPlaybackSpeedMethod =
                proxy(classes.first { it.type == setPlaybackSpeedMethodReference.definingClass })
                    .mutableClass.methods.first { it.name == setPlaybackSpeedMethodReference.name }
            setPlaybackSpeedMethodIndex = 0

            // Add override playback speed method.
            onPlaybackSpeedItemClickFingerprint.classDef.methods.add(
                ImmutableMethod(
                    definingClass,
                    "overridePlaybackSpeed",
                    listOf(ImmutableMethodParameter("F", annotations, null)),
                    "V",
                    AccessFlags.PUBLIC.value or AccessFlags.PUBLIC.value,
                    annotations,
                    null,
                    ImmutableMethodImplementation(
                        4,
                        """
                            # Check if the playback speed is not auto (-2.0f)
                            const/4 v0, 0x0
                            cmpg-float v0, v3, v0
                            if-lez v0, :ignore
                            
                            # Get the container class field.
                            iget-object v0, v2, $setPlaybackSpeedContainerClassFieldReference  

                            # For some reason, in YouTube 19.44.39 this value is sometimes null.
                            if-eqz v0, :ignore

                            # Get the field from its class.
                            iget-object v1, v0, $setPlaybackSpeedClassFieldReference
                            
                            # Invoke setPlaybackSpeed on that class.
                            invoke-virtual {v1, v3}, $setPlaybackSpeedMethodReference

                            :ignore
                            return-void
                        """.toInstructions(), null, null
                    )
                ).toMutable()
            )
        }

        playbackSpeedClassFingerprint.method.apply {
            val index = indexOfFirstInstructionOrThrow(Opcode.RETURN_OBJECT)
            val register = getInstruction<OneRegisterInstruction>(index).registerA
            val playbackSpeedClass = this.returnType

            // Set playback speed class.
            addInstructionsAtControlFlowLabel(
                index,
                "sput-object v$register, $EXTENSION_CLASS_DESCRIPTOR->playbackSpeedClass:$playbackSpeedClass"
            )

            val smaliInstructions =
                """
                    if-eqz v0, :ignore
                    invoke-virtual {v0, p0}, $playbackSpeedClass->overridePlaybackSpeed(F)V
                    return-void
                    :ignore
                    nop
                """

            addStaticFieldToExtension(
                EXTENSION_CLASS_DESCRIPTOR,
                "overridePlaybackSpeed",
                "playbackSpeedClass",
                playbackSpeedClass,
                smaliInstructions
            )
        }

        // Handle new playback speed menu.
        playbackSpeedMenuSpeedChangedFingerprint.match(
            videoQualityChangedFingerprint.originalClassDef,
        ).method.apply {
            val index = indexOfFirstInstructionOrThrow(Opcode.IGET)

            speedSelectionInsertMethod = this
            speedSelectionInsertIndex = index + 1
            speedSelectionValueRegister = getInstruction<TwoRegisterInstruction>(index).registerA
        }

        videoQualityFingerprint.let {
            // Fix bad data used by YouTube.
            it.method.addInstructions(
                0,
                """
                    invoke-static { p2, p1 }, $EXTENSION_CLASS_DESCRIPTOR->fixVideoQualityResolution(Ljava/lang/String;I)I    
                    move-result p1
                """
            )

            // Add methods to access obfuscated quality fields.
            it.classDef.apply {
                methods.add(
                    ImmutableMethod(
                        type,
                        "patch_getQualityName",
                        listOf(),
                        "Ljava/lang/String;",
                        AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
                        null,
                        null,
                        MutableMethodImplementation(2),
                    ).toMutable().apply {
                        // Only one string field.
                        val qualityNameField = fields.single { field ->
                            field.type == "Ljava/lang/String;"
                        }

                        addInstructions(
                            0,
                            """
                                iget-object v0, p0, $qualityNameField
                                return-object v0
                            """
                        )
                    }
                )

                methods.add(
                    ImmutableMethod(
                        type,
                        "patch_getResolution",
                        listOf(),
                        "I",
                        AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
                        null,
                        null,
                        MutableMethodImplementation(2),
                    ).toMutable().apply {
                        val resolutionField = fields.single { field ->
                            field.type == "I"
                        }

                        addInstructions(
                            0,
                            """
                                iget v0, p0, $resolutionField
                                return v0
                            """
                        )
                    }
                )
            }
        }

        // Detect video quality changes and override the current quality.
        setVideoQualityFingerprint.match(
            videoQualitySetterFingerprint.originalClassDef
        ).let { match ->
            // This instruction refers to the field with the type that contains the setQuality method.
            val onItemClickListenerClassReference = match.method
                .getInstruction<ReferenceInstruction>(0).reference
            val setQualityFieldReference = match.method
                .getInstruction<ReferenceInstruction>(1).reference as FieldReference

            proxy(
                classes.find { classDef ->
                    classDef.type == setQualityFieldReference.type
                }!!
            ).mutableClass.apply {
                // Add interface and helper methods to allow extension code to call obfuscated methods.
                interfaces.add(EXTENSION_VIDEO_QUALITY_MENU_INTERFACE)

                methods.add(
                    ImmutableMethod(
                        type,
                        "patch_setQuality",
                        listOf(
                            ImmutableMethodParameter(YOUTUBE_VIDEO_QUALITY_CLASS_TYPE, null, null)
                        ),
                        "V",
                        AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
                        null,
                        null,
                        MutableMethodImplementation(2),
                    ).toMutable().apply {
                        val setQualityMenuIndexMethod = methods.single { method ->
                            method.parameterTypes.firstOrNull() == YOUTUBE_VIDEO_QUALITY_CLASS_TYPE
                        }

                        addInstructions(
                            0,
                            """
                                invoke-virtual { p0, p1 }, $setQualityMenuIndexMethod
                                return-void
                            """
                        )
                    }
                )
            }

            videoQualitySetterFingerprint.method.addInstructions(
                0,
                """
                    # Get object instance to invoke setQuality method.
                    iget-object v0, p0, $onItemClickListenerClassReference
                    iget-object v0, v0, $setQualityFieldReference
                    
                    invoke-static { p1, v0, p2 }, $EXTENSION_CLASS_DESCRIPTOR->setVideoQuality([$YOUTUBE_VIDEO_QUALITY_CLASS_TYPE${EXTENSION_VIDEO_QUALITY_MENU_INTERFACE}I)I
                    move-result p2
                """
            )
        }

        onCreateHook(EXTENSION_CLASS_DESCRIPTOR, "initialize")
        videoSpeedChangedHook(EXTENSION_CLASS_DESCRIPTOR, "videoSpeedChanged")
        userSelectedPlaybackSpeedHook(EXTENSION_CLASS_DESCRIPTOR, "userSelectedPlaybackSpeed")
    }
}

private fun addSeekInterfaceMethods(targetClass: MutableClass, seekToMethod: Method, seekToRelativeMethod: Method) {
    // Add the interface and methods that extension calls.
    targetClass.interfaces.add(EXTENSION_PLAYER_INTERFACE)

    arrayOf(
        Triple(seekToMethod, "patch_seekTo", true),
        Triple(seekToRelativeMethod, "patch_seekToRelative", false),
    ).forEach { (method, name, returnsBoolean) ->
        // Add interface method.
        // Get enum type for the seek helper method.
        val seekSourceEnumType = method.parameterTypes[1].toString()

        val interfaceImplementation = ImmutableMethod(
            targetClass.type,
            name,
            listOf(ImmutableMethodParameter("J", null, "time")),
            if (returnsBoolean) "Z" else "V",
            AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
            null,
            null,
            MutableMethodImplementation(4),
        ).toMutable()

        var instructions = """
            # First enum (field a) is SEEK_SOURCE_UNKNOWN.
            sget-object v0, $seekSourceEnumType->a:$seekSourceEnumType
            invoke-virtual { p0, p1, p2, v0 }, $method
        """

        instructions += if (returnsBoolean) {
            """
                move-result p1
                return p1                
            """
        } else {
            "return-void"
        }

        // Insert helper method instructions.
        interfaceImplementation.addInstructions(
            0,
            instructions,
        )

        targetClass.methods.add(interfaceImplementation)
    }
}

private fun MutableMethod.insert(insertIndex: Int, register: String, descriptor: String) =
    addInstruction(insertIndex, "invoke-static { $register }, $descriptor")

private fun MutableMethod.insertTimeHook(insertIndex: Int, descriptor: String) =
    insert(insertIndex, "p1, p2", descriptor)

/**
 * Hook the player controller.  Called when a video is opened or the current video is changed.
 *
 * Note: This hook is called very early and is called before the video id, video time, video length,
 * and many other data fields are set.
 *
 * @param targetMethodClass The descriptor for the class to invoke when the player controller is created.
 * @param targetMethodName The name of the static method to invoke when the player controller is created.
 */
internal fun onCreateHook(targetMethodClass: String, targetMethodName: String) =
    playerInitMethod.insert(
        playerInitInsertIndex++,
        "v$playerInitInsertRegister",
        "$targetMethodClass->$targetMethodName($EXTENSION_PLAYER_INTERFACE)V",
    )

/**
 * Hook the MDX player director. Called when playing videos while casting to a big screen device.
 *
 * @param targetMethodClass The descriptor for the class to invoke when the player controller is created.
 * @param targetMethodName The name of the static method to invoke when the player controller is created.
 */
internal fun onCreateHookMdx(targetMethodClass: String, targetMethodName: String) =
    mdxInitMethod.insert(
        mdxInitInsertIndex++,
        "v$mdxInitInsertRegister",
        "$targetMethodClass->$targetMethodName($EXTENSION_PLAYER_INTERFACE)V",
    )

/**
 * Hook the video time.
 * The hook is usually called once per second.
 *
 * @param targetMethodClass The descriptor for the static method to invoke when the player controller is created.
 * @param targetMethodName The name of the static method to invoke when the player controller is created.
 */
fun videoTimeHook(targetMethodClass: String, targetMethodName: String) =
    timeMethod.insertTimeHook(
        timeInitInsertIndex++,
        "$targetMethodClass->$targetMethodName(J)V",
    )

/**
 * Hook when the video speed is changed for any reason _except when the user manually selects a new speed_.
 */
fun videoSpeedChangedHook(targetMethodClass: String, targetMethodName: String) =
    setPlaybackSpeedMethod.addInstruction(
        setPlaybackSpeedMethodIndex++,
        "invoke-static { p1 }, $targetMethodClass->$targetMethodName(F)V"
    )

/**
 * Hook the video speed selected by the user.
 */
fun userSelectedPlaybackSpeedHook(targetMethodClass: String, targetMethodName: String) {
    legacySpeedSelectionInsertMethod.addInstruction(
        legacySpeedSelectionInsertIndex++,
        "invoke-static { v$legacySpeedSelectionValueRegister }, $targetMethodClass->$targetMethodName(F)V"
    )

    speedSelectionInsertMethod.addInstruction(
        speedSelectionInsertIndex++,
        "invoke-static { v$speedSelectionValueRegister }, $targetMethodClass->$targetMethodName(F)V",
    )
}