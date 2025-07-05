package app.revanced.extension.youtube.patches;

import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ChangeHeaderPatch {

    public enum HeaderLogo {
        DEFAULT(null),
        REGULAR("ytWordmarkHeader"),
        PREMIUM("ytPremiumWordmarkHeader"),
        REVANCED("revanced_header_logo"),
        REVANCED_MINIMAL("revanced_header_logo_minimal"),
        CUSTOM("custom_header");

        @Nullable
        private final String resourceName;

        HeaderLogo(@Nullable String resourceName) {
            this.resourceName = resourceName;
        }

        /**
         * @return The attribute id of this header logo, or NULL if the logo should not be replaced.
         */
        @Nullable
        private Integer getAttributeId() {
            if (resourceName == null) {
                return null;
            }

            return Utils.getResourceIdentifier(resourceName, "attr");
        }
    }

    @Nullable
    private static final Integer headerLogoResource = Settings.HEADER_LOGO.get().getAttributeId();

    /**
     * Injection point.
     */
    public static int getHeaderAttributeId(int original) {
        return Objects.requireNonNullElse(headerLogoResource, original);
    }
}
