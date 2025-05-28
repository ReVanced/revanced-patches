package app.revanced.patches.spotify.layout.hide.createbutton

import app.revanced.patcher.fingerprint

internal val playlistCreateButtonPositionExperimentFingerprint = fingerprint {
    parameters("L")
    strings("create_button_position")
}
