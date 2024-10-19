package app.revanced.patches.youtube.layout.startupshortsreset

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.startupshortsreset.fingerprints.UserWasInShortsConfigFingerprint
import app.revanced.patches.youtube.layout.startupshortsreset.fingerprints.UserWasInShortsConfigFingerprint.indexOfOptionalInstruction
import app.revanced.patches.youtube.layout.startupshortsreset.fingerprints.UserWasInShortsFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Disable resuming Shorts on startup",
    description = "Adds an option to disable the Shorts player from resuming on app startup when Shorts were last being watched.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ]
        )
    ]
)
@Suppress("unused")
object DisableResumingShortsOnStartupPatch : BytecodePatch(
    setOf(UserWasInShortsConfigFingerprint, UserWasInShortsFingerprint)
) {

    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/DisableResumingStartupShortsPlayerPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.SHORTS.addPreferences(
            SwitchPreference("revanced_disable_resuming_shorts_player")
        )

        UserWasInShortsConfigFingerprint.resultOrThrow().mutableMethod.apply {
            val startIndex = indexOfOptionalInstruction(this)
            val walkerIndex = indexOfFirstInstructionOrThrow(startIndex) {
                val reference = getReference<MethodReference>()
                opcode == Opcode.INVOKE_VIRTUAL
                        && reference?.returnType == "Z"
                        && reference.definingClass != "Lj${'$'}/util/Optional;"
                        && reference.parameterTypes.size == 0
            }

            val walkerMethod = context.toMethodWalker(this)
                .nextMethod(walkerIndex, true)
                .getMethod() as MutableMethod

            // Presumably a method that processes the ProtoDataStore value (boolean) for the 'user_was_in_shorts' key.
            walkerMethod.addInstructionsWithLabels(
                0,
                """
                    invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->disableResumingStartupShortsPlayer()Z
                    move-result v0
                    if-eqz v0, :show
                    const/4 v0, 0x0
                    return v0
                    :show
                    nop
                """
            )
        }

        UserWasInShortsFingerprint.resultOrThrow().mutableMethod.apply {
            val listenableInstructionIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_INTERFACE &&
                        getReference<MethodReference>()?.definingClass == "Lcom/google/common/util/concurrent/ListenableFuture;" &&
                        getReference<MethodReference>()?.name == "isDone"
            }
            val freeRegister = getInstruction<OneRegisterInstruction>(listenableInstructionIndex + 1).registerA

            addInstructionsAtControlFlowLabel(
                listenableInstructionIndex,
                """
                    invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->disableResumingStartupShortsPlayer()Z
                    move-result v$freeRegister
                    if-eqz v$freeRegister, :show_startup_shorts_player
                    return-void
                    :show_startup_shorts_player
                    nop
                """
            )
        }
    }
}
