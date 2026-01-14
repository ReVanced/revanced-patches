package app.revanced.patches.reddit.customclients.infinityforreddit.api

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethod
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.apiUtilsMethod by gettingFirstMutableMethod("native-lib")