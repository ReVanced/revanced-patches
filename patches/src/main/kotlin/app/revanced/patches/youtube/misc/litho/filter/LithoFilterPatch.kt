@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.youtube.shared.conversionContextToStringMethod
import app.revanced.patches.shared.misc.litho.filter.EXTENSION_CLASS_DESCRIPTOR
import app.revanced.patches.shared.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.shared.misc.litho.filter.protobufBufferReferenceLegacyMethod
import app.revanced.patches.shared.misc.litho.filter.protobufBufferReferenceMethodMatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.*
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.insertLiteralOverride
import com.android.tools.smali.dexlib2.Opcode

val lithoFilterPatch = lithoFilterPatch(
    componentCreateInsertionIndex = {
        indexOfFirstInstructionOrThrow(Opcode.RETURN_OBJECT)
    },
    insertProtobufHook = {
        if (is_20_22_or_greater) {
            // Hook method that bridges between UPB buffer native code and FB Litho.
            // Method is found in 19.25+, but is forcefully turned off for 20.21 and lower.
            protobufBufferReferenceMethodMatch.let {
                // Hook the buffer after the call to jniDecode().
                it.method.addInstruction(
                    it[-1] + 1,
                    "invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer([B)V",
                )
            }
        }

        // Legacy non-native buffer.
        protobufBufferReferenceLegacyMethod.addInstruction(
            0,
            "invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V",
        )
    },
    getConversionContextToStringMethod = BytecodePatchContext::conversionContextToStringMethod::get,
    getExtractIdentifierFromBuffer = { is_20_21_or_greater },
    executeBlock = {
        // region A/B test of new Litho native code.

        // Turn off a feature flag that enables native code of protobuf parsing (Upb protobuf).
        lithoConverterBufferUpbFeatureFlagMethodMatch.let {
            // 20.22 the flag is still enabled in one location, but what it does is not known.
            // Disable it anyway.
            it.method.insertLiteralOverride(
                it[0],
                false,
            )
        }

        // endregion
    }
) {
    dependsOn(sharedExtensionPatch, versionCheckPatch)
}
