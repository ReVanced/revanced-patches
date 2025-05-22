package app.revanced.patches.messenger.navbar

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.messenger.metaai.removeMetaAIPatch

@Deprecated("Superseded by removeMetaAIPatch", ReplaceWith("removeMetaAIPatch"))
@Suppress("unused")
val removeMetaAITabPatch = bytecodePatch(
    description = "Removes the 'Meta AI' tab from the navbar.",
) {
    dependsOn(removeMetaAIPatch)
}
