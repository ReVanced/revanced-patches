package app.revanced.patches.twitch.ad.audio

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.audioAdsPresenterPlayMethod by gettingFirstMutableMethodDeclaratively {
    name("playAd")
    definingClass { endsWith("AudioAdsPlayerPresenter;") }
}
