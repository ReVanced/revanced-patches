package app.revanced.extension.spotify.misc;

import com.spotify.remoteconfig.internal.AccountAttribute;

import java.util.HashMap;
import java.util.Map;

import app.revanced.extension.shared.Logger;

/**
 * @noinspection unused
 */
public final class UnlockPremiumPatch {

    private static final Map<String, Object> OVERRIDES = new HashMap<>() {{
        // Disables player and app ads.
        put("ads", false);
        // Works along on-demand, allows playing any song without restriction.
        put("player-license", "premium");
        // Disables shuffle being initially enabled when first playing a playlist.
        put("shuffle", false);
        // Allows playing any song on-demand, without a shuffled order.
        put("on-demand", true);
        // Make sure playing songs is not disabled remotely and playlists show up.
        put("streaming", true);
        // Allows adding songs to queue and removes the smart shuffle mode restriction,
        // allowing to pick any of the other modes.
        put("pick-and-shuffle", false);
        // Disables shuffle-mode streaming-rule, which forces songs to be played shuffled
        // and breaks the player when other patches are applied.
        put("streaming-rules", "");
        // Enables premium UI in settings and removes the premium button in the nav-bar.
        put("nft-disabled", "1");
        // Enable Cross-Platform Spotify Car Thing.
        put("can_use_superbird", true);
        // Removes the premium button in the nav-bar for tablet users.
        put("tablet-free", false);
    }};

    /**
     * Injection point.
     */
    public static void overrideAttribute(Map<String, AccountAttribute> attributes) {
        try {
            for (var entry : OVERRIDES.entrySet()) {
                var key = entry.getKey();
                var attribute = attributes.get(key);
                if (attribute == null) {
                    Logger.printException(() -> "Account attribute not found: " + key);
                } else {
                    attribute.value_ = entry.getValue();
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "overrideAttribute failure", ex);
        }
    }
}
