package app.revanced.patches.googlerecorder.restrictions

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.onApplicationCreateMethodMatch by composingFirstMethod {
    name("onCreate")
    definingClass("RecorderApplication;")
    instructions("com.google.android.feature.PIXEL_2017_EXPERIENCE"())
}
