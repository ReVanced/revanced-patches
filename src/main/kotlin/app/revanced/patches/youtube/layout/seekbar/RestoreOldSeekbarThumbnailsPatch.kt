package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.fingerprints.FullscreenSeekbarThumbnailsFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.playservice.YouTubeVersionCheck
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.findOpcodeIndicesReversed
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstWideLiteralInstructionValueOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Restore old seekbar thumbnails",
    description = "Adds an option to restore the old seekbar thumbnails that appear above the seekbar while seeking instead of fullscreen thumbnails.",
    dependencies = [IntegrationsPatch::class, AddResourcesPatch::class, YouTubeVersionCheck::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43",
                "19.12.41",
                "19.13.37",
                "19.14.43",
                "19.15.36",
                "19.16.39",
                "19.34.42",
            ]
        )
    ]
)
@Suppress("unused")
object RestoreOldSeekbarThumbnailsPatch : BytecodePatch(
    setOf(FullscreenSeekbarThumbnailsFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/RestoreOldSeekbarThumbnailsPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_restore_old_seekbar_thumbnails")
        )

        if (false) FullscreenSeekbarThumbnailsFingerprint.resultOrThrow().mutableMethod.apply {
            val moveResultIndex = getInstructions().lastIndex - 1

            addInstruction(
                moveResultIndex,
                "invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->useFullscreenSeekbarThumbnails()Z"
            )
        }

        context.proxy(context.classes.first { it.type == "Laaxp;" }).mutableClass.methods
            .find { it.name == "bX" }!!.apply {
                val index = indexOfFirstWideLiteralInstructionValueOrThrow(45611695L)
                val insertIndex =
                    indexOfFirstInstructionOrThrow(index) { opcode == Opcode.MOVE_RESULT }

                addInstruction(
                    insertIndex,
                    "invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->useFullscreenSeekbarThumbnails()Z"
                )
            }


        context.proxy(context.classes.first { it.type == "Lfwa;" }).mutableClass.methods
            .find { it.name == "N" }!!.apply {
                val index = indexOfFirstWideLiteralInstructionValueOrThrow(45367320L)
                val insertIndex =
                    indexOfFirstInstructionOrThrow(index) { opcode == Opcode.MOVE_RESULT }

                addInstruction(
                    insertIndex,
                    "invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->useFullscreenSeekbarThumbnails()Z"
                )
            }

        context.proxy(context.classes.first { it.type == "Lkrx;" }).mutableClass.methods
            .find { it.name == "mF" }!!.apply {
                val index = indexOfFirstInstructionOrThrow{ opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>()?.name == "mF" }

                removeInstructions(index, getInstructions().size - index - 1)
            }

        context.proxy(context.classes.first { it.type == "Lkvm;" }).mutableClass.methods
            .find { it.name == "ab" }!!.apply {
                val index = indexOfFirstInstructionOrThrow{
                    val reference = getReference<MethodReference>()
                    opcode == Opcode.INVOKE_VIRTUAL && reference?.definingClass == "Lkzh;" && reference.name == "l"
                }

                removeInstruction(index)
            }

        context.proxy(context.classes.first { it.type == "Lbbfq;" }).mutableClass.methods
            .find { it.name == "dx" }!!.apply {
                val insertIndex =
                    indexOfFirstInstructionOrThrow { opcode == Opcode.MOVE_RESULT }

                addInstruction(
                    insertIndex,
                    "invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->useFullscreenSeekbarThumbnails()Z"
                )
            }

        context.proxy(context.classes.first { it.type == "Lkvk;" }).mutableClass.methods
            .find { it.name == "mF" }!!.apply {
                findOpcodeIndicesReversed {
                    val reference = getReference<MethodReference>()
                    opcode == Opcode.INVOKE_VIRTUAL && reference?.definingClass == "Lkzh;" && reference.name == "mF"
                }.forEach { index ->
                    val instruction = getInstruction<FiveRegisterInstruction>(index)
                    val free = instruction.registerC

                    addInstructionsWithLabels(index + 1, """
                        move-object/from16 v$free, p0
                        iget-object v$free, v$free, Lkvk;->b:Lkvm;
                        invoke-virtual { v$free }, Lkvm;->F()V  # Inflate layout
                        iget-object v$free, v$free, Lkvm;->s:Lkvx;
                        iget-object v$free, v$free, Lgym;->d:Lahhj;
                        #
                        # FIXME: this field is always null.  Something else needs to be set for this to work.
                        #
                        if-eqz v$free, :is_null
                        
                        invoke-virtual { v$free }, Lahhj;->j()Z
                        move-result v$free
                        if-eqz v$free, :do_not_show
                        
                        # This duplicate code can be fixed by using a second free register (or adding a helper method).
                        move-object/from16 v$free, p0
                        iget-object v$free, v$free, Lkvk;->b:Lkvm;
                        iget-object v$free, v$free, Lkvm;->s:Lkvx;
                        iget-object v$free, v$free, Lgym;->d:Lahhj;
                        
                        invoke-virtual { v$free, v${instruction.registerD}, v${instruction.registerE}, v${instruction.registerF} }, Lahhj;->mF(IJ)V
                        
                        :is_null
                        :do_not_show
                        nop
                    """)
                    removeInstruction(index)
                }

                findOpcodeIndicesReversed {
                    val reference = getReference<MethodReference>()
                    opcode == Opcode.INVOKE_VIRTUAL && reference?.definingClass == "Lkov;" && reference.name == "j"
                }.forEach { index ->
                    removeInstruction(index)
                }
            }
    }
}
