package app.revanced.patches.instagram.profile.copybio

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.profileBioMethod by gettingFirstMethodDeclaratively("profile_bio")
