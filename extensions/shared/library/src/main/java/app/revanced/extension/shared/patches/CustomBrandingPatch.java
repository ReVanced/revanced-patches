package app.revanced.extension.shared.patches;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceStringArray;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;

/**
 * Patch shared by YouTube and YT Music.
 */
@SuppressWarnings("unused")
public class CustomBrandingPatch {

    public enum BrandingTheme {
        ORIGINAL("revanced_original"),
        ROUNDED("revanced_rounded"),
        MINIMAL("revanced_minimal"),
        SCALED("revanced_scaled"),
        /** User provided custom icon */
        CUSTOM("revanced_custom");

        public final String themeAlias;

        BrandingTheme(String themeAlias) {
            this.themeAlias = themeAlias;
        }
    }

    /**
     * Injection point.
     */
    private static boolean customIconIncluded() {
        // Modified during patching.
        throw new IllegalStateException();
    }

    /**
     * Injection point.
     */
    public static void setBrandingIcon() {
        try {
            Context context = Utils.getContext();
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();

            BrandingTheme selectedTheme = BaseSettings.CUSTOM_BRANDING_ICON.get();
            String nameIndex = BaseSettings.CUSTOM_BRANDING_NAME.get().toString();
            String[] nameIndexValues = getResourceStringArray(
                    "revanced_custom_branding_name_entry_values");

            final boolean customIconIncluded = customIconIncluded();
            boolean toastShown = false;

            for (String nameValue : nameIndexValues) {
                for (BrandingTheme theme : BrandingTheme.values()) {
                    if (!customIconIncluded && theme == BrandingTheme.CUSTOM) {
                        continue;
                    }

                    ComponentName component = new ComponentName(packageName,
                            packageName + '.' + theme.themeAlias + '_' + nameValue);

                    // Check if the state is different, and show a toast if so.
                    // Changing the active alias causes the app to restart,
                    // which can be mistaken for a crash so show a toast to be clear.
                    final int currentState = pm.getComponentEnabledSetting(component);
                    final int desiredState = (theme == selectedTheme && nameValue.equals(nameIndex))
                            ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

                    if (currentState != desiredState) {
                        if (!toastShown) {
                            toastShown = true;
                            Utils.showToastShort(str("revanced_custom_branding_name_toast"));
                        }

                        pm.setComponentEnabledSetting(component, desiredState, 0);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setBrandingIcon failure", ex);
        }
    }
}
