package app.revanced.patches.youtube.misc.audiofocus

import app.revanced.patcher.gettingFirstMutableMethod
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.audioFocusChangeListenerMethod by gettingFirstMutableMethod(
    "AudioFocus DUCK",
    "AudioFocus loss; Will lower volume",
)

internal val BytecodePatchContext.audioFocusRequestBuilderMethod by gettingFirstMutableMethod(
    "Can't build an AudioFocusRequestCompat instance without a listener",
)
