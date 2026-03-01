package app.revanced.patches.reddit.customclients.infinityforreddit.api

import app.revanced.patcher.gettingFirstMethod
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.apiUtilsMethod by gettingFirstMethod("native-lib")