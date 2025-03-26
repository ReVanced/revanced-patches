package app.revanced.patches.spotify.misc

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Spotify Premium",
    description = "Unlock Spotify Premium features. Server-sided features like downloading songs are still locked.",
) {
    compatibleWith("com.spotify.music")

    extendWith("extensions/spotify.rve")

    execute {
        // Make _value accessible so that it can be overridden in the extension.
        accountAttributeFingerprint.classDef.fields.first { it.name == "value_" }.apply {
            accessFlags = accessFlags.or(AccessFlags.PUBLIC.value).and(AccessFlags.PRIVATE.value.inv())
        }

        // Override the attributes map in the getter method.
        val attributesMapRegister = 0
        val instantiateUnmodifiableMapIndex = 1
        productStateProtoFingerprint.method.addInstruction(
            instantiateUnmodifiableMapIndex,
            "invoke-static { v$attributesMapRegister }," +
                "Lapp/revanced/extension/spotify/misc/UnlockPremiumPatch;->overrideAttribute(Ljava/util/Map;)V",
        )

        // Add the query parameter trackRows to show popular tracks in the artist page.
        val addQueryParameterIndex = buildQueryParametersFingerprint.stringMatches!!.first().index - 1
        buildQueryParametersFingerprint.method.replaceInstruction(addQueryParameterIndex, "nop")
    }
}
