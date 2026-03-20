package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.gettingFirstImmutableMethod
import app.revanced.patcher.patch.BytecodePatchContext


internal val BytecodePatchContext.httpClientBuilderMethod by gettingFirstImmutableMethod("client == null", "scheduler == null")
