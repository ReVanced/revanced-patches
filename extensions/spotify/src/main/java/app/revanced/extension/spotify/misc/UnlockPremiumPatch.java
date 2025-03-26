package app.revanced.extension.spotify.misc;

import com.spotify.remoteconfig.internal.AccountAttribute;

import java.util.Map;
import java.util.Objects;

/**
 * @noinspection unused
 */
public final class UnlockPremiumPatch {
    private static final Map<String, Object> OVERRIDES = Map.of(
            // Disables player and app ads.
            "ads", false,
            // Works along on-demand, allows playing any song without restriction.
            "player-license", "premium",
            // Disables shuffle being initially enabled when first playing a playlist.
            "shuffle", false,
            // Allows playing any song on-demand, without a shuffled order.
            "on-demand", true,
            // Make sure playing songs is not disabled remotely and playlists show up.
            "streaming", true,
            // Allows adding songs to queue and removes the smart shuffle mode restriction, 
            // allowing to pick any of the other modes.
            "pick-and-shuffle", false,
            // Disables shuffle-mode streaming-rule, which forces songs to be played shuffled 
            // and breaks the player when other patches are applied.
            "streaming-rules", "",
            // Enables premium UI in settings and removes the premium button in the nav-bar.
            "nft-disabled", "1",
            // Set the subscription name.
            "name", "Spotify Premium",
            // Set the user's account type.
            "type", "premium",
            // Changes the playlist type from mobile freetier.
            "catalogue", "premium",
            // Set the app's product state type.
            "financial-product", "pr:premium,tc:0",
            // Enable Cross-Platform Spotify Car Thing.
            "can_use_superbird", true,
            // Disable the client-side subscription checks.
            "com.spotify.madprops.delivered.by.ucs", "false",
            "com.spotify.madprops.use.ucs.product.state", "false"
    );

    public static void overrideAttribute(Map<String, AccountAttribute> attributes) {
        for (var entry : OVERRIDES.entrySet()) {
            var attribute = Objects.requireNonNull(attributes.get(entry.getKey()));
            attribute.value_ = entry.getValue();
        }
    }
}
