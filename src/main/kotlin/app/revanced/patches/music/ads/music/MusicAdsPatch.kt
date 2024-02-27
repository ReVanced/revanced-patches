package app.revanced.patches.music.ads.music

import app.revanced.patches.music.utils.integrations.Constants.ADS_PATH
import app.revanced.patches.shared.patch.ads.AbstractAdsPatch

object MusicAdsPatch : AbstractAdsPatch(
    "$ADS_PATH/MusicAdsPatch;->hideMusicAds()Z"
)
