package app.revanced.patches.youtube.utils.litho

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.patch.litho.ComponentParserPatch
import app.revanced.patches.shared.patch.litho.ComponentParserPatch.generalHook
import app.revanced.patches.youtube.utils.integrations.Constants.COMPONENTS_PATH
import app.revanced.patches.youtube.utils.litho.fingerprints.GeneralByteBufferFingerprint
import app.revanced.patches.youtube.utils.litho.fingerprints.LithoFilterFingerprint
import app.revanced.util.exception
import java.io.Closeable

@Patch(dependencies = [ComponentParserPatch::class])
@Suppress("unused")
object LithoFilterPatch : BytecodePatch(
    setOf(
        GeneralByteBufferFingerprint,
        LithoFilterFingerprint
    )
), Closeable {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$COMPONENTS_PATH/LithoFilterPatch;"

    internal lateinit var addFilter: (String) -> Unit
        private set

    private var filterCount = 0
    override fun execute(context: BytecodeContext) {

        // ByteBuffer is set after checking for non-null
        GeneralByteBufferFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstruction(
                    2,
                    "invoke-static { p2 }, $INTEGRATIONS_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V"
                )
            }
        } ?: throw GeneralByteBufferFingerprint.exception

        generalHook("$INTEGRATIONS_CLASS_DESCRIPTOR->filter")

        LithoFilterFingerprint.result?.mutableMethod?.apply {
            removeInstructions(0, 6)

            addFilter = { classDescriptor ->
                addInstructions(
                    0, """
                        new-instance v1, $classDescriptor
                        invoke-direct {v1}, $classDescriptor-><init>()V
                        const/16 v2, ${filterCount++}
                        aput-object v1, v0, v2
                        """
                )
            }
        } ?: throw LithoFilterFingerprint.exception

    }

    override fun close() = LithoFilterFingerprint.result!!
        .mutableMethod.addInstructions(
            0, """
                const/16 v0, $filterCount
                new-array v0, v0, [$COMPONENTS_PATH/Filter;
                """
        )
}
