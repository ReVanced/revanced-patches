package app.revanced.patches.tumblr.timelinefilter.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

// This fingerprints the Integration TimelineFilterPatch.filterTimeline method.
// The opcode fingerprint is searching for
//   if ("BLOCKED_OBJECT_DUMMY".equals(elementType)) iterator.remove();
internal val timelineFilterIntegrationFingerprint = methodFingerprint {
    opcodes(
        Opcode.CONST_STRING, // "BLOCKED_OBJECT_DUMMY"
        Opcode.INVOKE_VIRTUAL, // HashSet.add(^)
    )
    strings("BLOCKED_OBJECT_DUMMY")
    custom { _, classDef ->
        classDef.endsWith("/TimelineFilterPatch;")
    }
}
