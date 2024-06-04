package app.revanced.patches.tumblr.timelinefilter.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

// This is the constructor of the Timeline class.
// It receives the List<TimelineObject> as an argument with a @Json annotation, so this should be the first time
// that the List<TimelineObject> is exposed in non-library code.
internal val timelineConstructorFingerprint = methodFingerprint {
    strings("timelineObjectsList")
    custom { methodDef, classDef ->
        classDef.endsWith("/Timeline;") && methodDef.parameters[0].type == "Ljava/util/List;"
    }
}
