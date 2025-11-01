package app.revanced.extension.youtube.patches;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ChangeHeaderPatch {

    public enum HeaderLogo {
        DEFAULT(null, null),
        REGULAR("ytWordmarkHeader", "yt_ringo2_wordmark_header"),
        PREMIUM("ytPremiumWordmarkHeader", "yt_ringo2_premium_wordmark_header"),
        ROUNDED("revanced_header_rounded"),
        MINIMAL("revanced_header_minimal"),
        CUSTOM("revanced_header_custom"),

        // Old enum names for data migration. TODO: Eventually delete these.
        @Deprecated
        REVANCED(ROUNDED.attributeName),
        @Deprecated
        REVANCED_MINIMAL(MINIMAL.attributeName);

        @Nullable
        private final String attributeName;
        @Nullable
        private final String drawableName;

        HeaderLogo(String attributeName) {
            this(Objects.requireNonNull(attributeName), Objects.requireNonNull(attributeName));
        }

        HeaderLogo(@Nullable String attributeName, @Nullable String drawableName) {
            this.attributeName = attributeName;
            this.drawableName = drawableName;
        }

        /**
         * @return The attribute id of this header logo, or NULL if the logo should not be replaced.
         */
        @Nullable
        private Integer getAttributeId() {
            if (attributeName == null) {
                return null;
            }

            final int identifier = Utils.getResourceIdentifier(attributeName, "attr");
            if (identifier == 0) {
                // Should never happen.
                Logger.printException(() -> "Could not find attribute: " + drawableName);
                Settings.HEADER_LOGO.resetToDefault();
                return null;
            }

            return identifier;
        }

        @Nullable
        public Drawable getDrawable() {
            if (drawableName == null) {
                return null;
            }

            String drawableFullName = drawableName + (Utils.isDarkModeEnabled()
                    ? "_dark"
                    : "_light");

            final int identifier = Utils.getResourceIdentifier(drawableFullName, "drawable");
            if (identifier != 0) {
                return Utils.getContext().getDrawable(identifier);
            }

            // Should never happen.
            Logger.printException(() -> "Could not find drawable: " + drawableFullName);
            Settings.HEADER_LOGO.resetToDefault();
            return null;
        }
    }

    /**
     * Injection point.
     */
    public static int getHeaderAttributeId(int original) {
        return Objects.requireNonNullElse(Settings.HEADER_LOGO.get().getAttributeId(), original);
    }

    public static Drawable getDrawable(Drawable original) {
        Drawable logo = Settings.HEADER_LOGO.get().getDrawable();
        if (logo != null) {
            return logo;
        }

        // TODO: If 'Hide Doodles' is enabled, this will force the regular logo regardless
        //       what account the user has. This can be improved the next time a Doodle is
        //       active and the attribute id is passed to this method so the correct
        //       regular/premium logo is returned.
        logo = HeaderLogo.REGULAR.getDrawable();
        if (logo != null) {
            return logo;
        }

        // Should never happen.
        Logger.printException(() -> "Could not find regular header logo resource");
        return original;
    }
}
