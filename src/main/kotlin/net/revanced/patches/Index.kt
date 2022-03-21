package net.revanced.patches

import net.revanced.patches.ads.VideoAds

// This object contains all the patches and should be imported when using this library
object Index {
    // Array of patches. New patches should be added to the array
    val patches = arrayOf(
        VideoAds::class
    )
}
