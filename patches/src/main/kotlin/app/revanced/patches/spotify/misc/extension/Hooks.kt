package app.revanced.patches.spotify.misc.extension

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.string
import app.revanced.patcher.strings
import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook
import app.revanced.patches.shared.misc.extension.extensionHook
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal val mainActivityOnCreateHook = activityOnCreateExtensionHook(
    "Lcom/spotify/music/SpotifyMainActivity;"
)

private var contextReferenceIndex = -1

internal val loadOrbitLibraryHook = extensionHook(
    getInsertIndex = {
        indexOfFirstInstruction {
            "orbit-jni-spotify" in (string ?: return@indexOfFirstInstruction false)
        }
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
    strings(
        "orbit_library_load",
        "orbit-jni-spotify"
    )
}
