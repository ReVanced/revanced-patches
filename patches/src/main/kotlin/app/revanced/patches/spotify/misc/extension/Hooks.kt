package app.revanced.patches.spotify.misc.extension

import app.revanced.patcher.definingClass
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook
import app.revanced.patches.shared.misc.extension.extensionHook
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal val mainActivityOnCreateHook = activityOnCreateExtensionHook(
    "Lcom/spotify/music/SpotifyMainActivity;"
)

private var contextReferenceIndex = -1

internal val loadOrbitLibraryHook = extensionHook(
    getInsertIndex = {
        // Find the last orbit_library_load string usage
        var lastIndex = -1
        instructions.forEachIndexed { index, instruction ->
            instruction.toString().let {
                if (it.contains("orbit_library_load") || it.contains("orbit-jni-spotify")) {
                    lastIndex = index
                }
            }
        }
        lastIndex
    },
    getContextRegister = {
        contextReferenceIndex = indexOfFirstInstruction {
            getReference<FieldReference>()?.type == "Landroid/content/Context;"
        }
        val contextRegister =
            getInstruction<TwoRegisterInstruction>(contextReferenceIndex).registerA

        "v$contextRegister"
    },
) {
    instructions(
        "orbit_library_load"(),
        "orbit-jni-spotify"()
    )
}
