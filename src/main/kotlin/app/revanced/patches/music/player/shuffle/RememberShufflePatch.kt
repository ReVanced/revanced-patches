package app.revanced.patches.music.player.shuffle

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.music.player.shuffle.fingerprints.MusicPlaybackControlsFingerprint
import app.revanced.patches.music.player.shuffle.fingerprints.ShuffleClassReferenceFingerprint
import app.revanced.patches.music.utils.integrations.Constants.PLAYER
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.transformFields
import app.revanced.util.traverseClassHierarchy
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.Reference
import com.android.tools.smali.dexlib2.immutable.ImmutableField
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.util.MethodUtil

@Patch(
    name = "Remember shuffle state",
    description = "Adds an option to remember the state of the shuffle toggle.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object RememberShufflePatch : BytecodePatch(
    setOf(
        MusicPlaybackControlsFingerprint,
        ShuffleClassReferenceFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        ShuffleClassReferenceFingerprint.result?.let {
            it.mutableMethod.apply {
                val startIndex = it.scanResult.patternScanResult!!.startIndex
                val endIndex = it.scanResult.patternScanResult!!.endIndex
                val imageViewIndex = implementation!!.instructions.indexOfFirst { instruction ->
                    ((instruction as? ReferenceInstruction)?.reference as? FieldReference)?.type == "Landroid/widget/ImageView;"
                }

                SHUFFLE_CLASS = it.classDef.type

                val shuffleReference1 = descriptor(startIndex)
                val shuffleReference2 = descriptor(startIndex + 1)
                val shuffleReference3 = descriptor(endIndex)
                val shuffleFieldReference = shuffleReference3 as FieldReference
                imageViewReference = descriptor(imageViewIndex)

                shuffleStateLabel = """
                    iget-object v1, v0, $shuffleReference1
                    invoke-interface {v1}, $shuffleReference2
                    move-result-object v1
                    check-cast v1, ${shuffleFieldReference.definingClass}
                    iget-object v1, v1, $shuffleReference3
                    invoke-virtual {v1}, ${shuffleFieldReference.type}->ordinal()I
                    move-result v1
                    """
            }

            val constructorMethod =
                it.mutableClass.methods.first { method -> MethodUtil.isConstructor(method) }
            val onClickMethod = it.mutableClass.methods.first { method -> method.name == "onClick" }

            constructorMethod.apply {
                addInstruction(
                    implementation!!.instructions.size - 1,
                    "sput-object p0, $PLAYBACK_CONTROLS_CLASS_DESCRIPTOR->shuffleClass:$SHUFFLE_CLASS"
                )
            }

            onClickMethod.apply {
                addInstructions(
                    0, """
                        move-object v0, p0
                        """ + shuffleStateLabel + """
                        invoke-static {v1}, $PLAYER->setShuffleState(I)V
                        """
                )
            }

            context.traverseClassHierarchy(it.mutableClass) {
                accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL
                transformFields {
                    ImmutableField(
                        definingClass,
                        name,
                        type,
                        AccessFlags.PUBLIC or AccessFlags.PUBLIC,
                        null,
                        annotations,
                        null
                    ).toMutable()
                }
            }
        } ?: throw ShuffleClassReferenceFingerprint.exception

        MusicPlaybackControlsFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstruction(
                    0,
                    "invoke-virtual {v0}, $PLAYBACK_CONTROLS_CLASS_DESCRIPTOR->rememberShuffleState()V"
                )

                val shuffleField = ImmutableField(
                    definingClass,
                    "shuffleClass",
                    SHUFFLE_CLASS,
                    AccessFlags.PUBLIC or AccessFlags.STATIC,
                    null,
                    annotations,
                    null
                ).toMutable()

                val shuffleMethod = ImmutableMethod(
                    definingClass,
                    "rememberShuffleState",
                    emptyList(),
                    "V",
                    AccessFlags.PUBLIC or AccessFlags.FINAL,
                    annotations, null,
                    MutableMethodImplementation(5)
                ).toMutable()

                shuffleMethod.addInstructionsWithLabels(
                    0, """
                            invoke-static {}, $PLAYER->getShuffleState()I
                            move-result v2
                            if-nez v2, :dont_shuffle
                            sget-object v0, $PLAYBACK_CONTROLS_CLASS_DESCRIPTOR->shuffleClass:$SHUFFLE_CLASS
                            """ + shuffleStateLabel + """
                            iget-object v3, v0, $imageViewReference
                            invoke-virtual {v3}, Landroid/widget/ImageView;->performClick()Z
                            if-eqz v1, :dont_shuffle
                            invoke-virtual {v3}, Landroid/widget/ImageView;->performClick()Z
                            :dont_shuffle
                            return-void
                            """
                )

                it.mutableClass.methods.add(shuffleMethod)
                it.mutableClass.staticFields.add(shuffleField)
            }
        } ?: throw MusicPlaybackControlsFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.PLAYER,
            "revanced_remember_shuffle_state",
            "true"
        )

    }

    private const val PLAYBACK_CONTROLS_CLASS_DESCRIPTOR =
        "Lcom/google/android/apps/youtube/music/watchpage/MusicPlaybackControls;"

    private lateinit var SHUFFLE_CLASS: String
    private lateinit var imageViewReference: Reference
    private lateinit var shuffleStateLabel: String

    private fun MutableMethod.descriptor(index: Int): Reference {
        return getInstruction<ReferenceInstruction>(index).reference
    }
}
