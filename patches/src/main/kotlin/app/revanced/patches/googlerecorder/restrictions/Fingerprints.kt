package app.revanced.patches.googlerecorder.restrictions

import app.revanced.patcher.*

internal val onApplicationCreateMethodMatch = firstMethodComposite {
    name("onCreate")
    definingClass("RecorderApplication"::endsWith)
    instructions("com.google.android.feature.PIXEL_2017_EXPERIENCE"())
}
