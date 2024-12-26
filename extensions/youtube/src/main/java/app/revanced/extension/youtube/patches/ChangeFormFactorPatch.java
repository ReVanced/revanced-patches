package app.revanced.extension.youtube.patches;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ChangeFormFactorPatch {

    public enum FormFactor {
        /**
         * Unmodified, and same as un-patched.
         */
        DEFAULT(null),
        /**
         * <pre>
         * Some changes include:
         * - Explore tab is present.
         * - watch history is missing.
         * - feed thumbnails fade in.
         */
        UNKNOWN(0),
        SMALL(1),
        LARGE(2),
        /**
         * Cars with 'Google built-in'.
         * Layout seems identical to {@link #UNKNOWN}
         * even when using an Android Automotive device.
         */
        AUTOMOTIVE(3),
        WEARABLE(4);

        @Nullable
        final Integer formFactorType;

        FormFactor(@Nullable Integer formFactorType) {
            this.formFactorType = formFactorType;
        }
    }

    @Nullable
    private static final Integer FORM_FACTOR_TYPE = Settings.CHANGE_FORM_FACTOR.get().formFactorType;

    /**
     * Injection point.
     */
    public static int getFormFactor(int original) {
        return FORM_FACTOR_TYPE == null
                ? original
                : FORM_FACTOR_TYPE;
    }

}