package app.revanced.patches.music.misc.litho.filter

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.misc.litho.filter.EXTENSION_CLASS_DESCRIPTOR
import app.revanced.patches.shared.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.shared.misc.litho.filter.protobufBufferReferenceMethod

val lithoFilterPatch = lithoFilterPatch(
    insertLegacyProtobufHook = {
        protobufBufferReferenceMethod.addInstruction(
            0,
            "invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->setProtobufBuffer(Ljava/nio/ByteBuffer;)V",
        )
    },
) {
    dependsOn(sharedExtensionPatch)
}
