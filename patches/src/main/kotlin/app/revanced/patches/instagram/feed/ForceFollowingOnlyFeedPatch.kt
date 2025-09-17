package app.revanced.patches.instagram.feed

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.instagram.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/instagram/feed/ForceFollowingOnlyFeedPatch;"

@Suppress("unused")
val setFollowingOnlyHomePatch = bytecodePatch(
    name = "Force following only feed",
    description = "The home feed will contains only posts/reels of people you are following.",
) {
    compatibleWith("com.instagram.android")

    dependsOn(sharedExtensionPatch)

    execute {
        /**
         * Since the header field is obfuscated and there is no easy way to identify it among all the class fields,
         * an additional method is fingerprinted.
         * This method uses the map, so we can get the field name of the map field using this.
         */
        val mainFeedRequestHeaderFieldName =
            mainFeedHeaderMapFinderFingerprint.method.instructions
                .asSequence()
                .mapNotNull { it.getReference<FieldReference>() }
                .firstOrNull { ref ->
                    ref.type == "Ljava/util/Map;" &&
                            ref.definingClass == mainFeedRequestClassFingerprint.classDef.toString()
                }
                ?.name

        initMainFeedRequestFingerprint.method.apply {
            // Finds the instruction where the map is being initialized in the constructor
            val getHeaderIndex = indexOfFirstInstructionOrThrow {
                getReference<FieldReference>().let {
                    it?.name == mainFeedRequestHeaderFieldName
                }
            }

            val paramHeaderRegister = getInstruction<TwoRegisterInstruction>(getHeaderIndex).registerA

            // Replace the `pagination_source` header value with `following` in the feed/timeline request.
            addInstructions(
                getHeaderIndex,
                """
                    invoke-static { v$paramHeaderRegister }, $EXTENSION_CLASS_DESCRIPTOR->setFollowingHeader(Ljava/util/Map;)Ljava/util/Map;
                    move-result-object v$paramHeaderRegister
                """
            )
        }
    }
}
