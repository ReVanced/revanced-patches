package app.revanced.patches.youtube.misc.audiofocus

import app.revanced.patcher.gettingFirstMethod
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.audioFocusChangeListenerMethod by gettingFirstMethod(
    "AudioFocus DUCK",
    "AudioFocus loss; Will lower volume",
)

internal val BytecodePatchContext.audioFocusRequestBuilderMethod by gettingFirstMethod(
    "Can't build an AudioFocusRequestCompat instance without a listener",
)
