package app.revanced.patches.music.utils.litho

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.utils.integrations.Constants.COMPONENTS_PATH
import app.revanced.patches.music.utils.litho.fingerprints.LithoFilterFingerprint
import app.revanced.patches.shared.patch.litho.ComponentParserPatch
import app.revanced.patches.shared.patch.litho.ComponentParserPatch.pathBuilderHook
import app.revanced.util.exception
import java.io.Closeable

@Patch(dependencies = [ComponentParserPatch::class])
object LithoFilterPatch : BytecodePatch(
    setOf(LithoFilterFingerprint)
), Closeable {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$COMPONENTS_PATH/LithoFilterPatch;"

    internal lateinit var addFilter: (String) -> Unit
        private set

    private var filterCount = 0

    override fun execute(context: BytecodeContext) {
        pathBuilderHook("$INTEGRATIONS_CLASS_DESCRIPTOR->filter")

        LithoFilterFingerprint.result?.let {
            it.mutableMethod.apply {
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
