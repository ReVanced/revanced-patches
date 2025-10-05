package app.revanced.extension.shared.patches;

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
        /** User provided custom icon. */
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
    private static int numberOfCustomNames() {
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

            BrandingTheme selectedBranding = BaseSettings.CUSTOM_BRANDING_ICON.get();
            final int selectedNameIndex = BaseSettings.CUSTOM_BRANDING_NAME.get();
            final boolean customIconIncluded = customIconIncluded();
            ComponentName componentToEnable = null;

            for (int index = 1, maxIndex = numberOfCustomNames(); index <= maxIndex; index++) {
                for (BrandingTheme theme : BrandingTheme.values()) {
                    if (!customIconIncluded && theme == BrandingTheme.CUSTOM) {
                        continue;
                    }

                    String aliasClass = packageName + '.' + theme.themeAlias + '_' + index;
                    ComponentName component = new ComponentName(packageName, aliasClass);

                    final boolean aliasMatchesSettings = theme == selectedBranding && index == selectedNameIndex;
                    if (aliasMatchesSettings) {
                        componentToEnable = component;
                    }

                    final int desiredState = aliasMatchesSettings
                            ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                    final int currentState = pm.getComponentEnabledSetting(component);
                    if (currentState != desiredState) {
                        // First turn off all aliases, then turn on what is needed.
                        // This is required otherwise the app can shutdown instead of restarting
                        // leaving the aliases in a corrupted and conflicting state.
                        if (desiredState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                            // Don't allow the app to be killed.
                            pm.setComponentEnabledSetting(component, desiredState, PackageManager.DONT_KILL_APP);
                        }
                    }
                }
            }

            if (componentToEnable != null) {
                pm.setComponentEnabledSetting(componentToEnable,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
            } else {
                // Settings are for custom user icons but no user icons are present.
                Utils.showToastLong("Resetting to default custom branding");
                BaseSettings.CUSTOM_BRANDING_ICON.resetToDefault();
                BaseSettings.CUSTOM_BRANDING_NAME.resetToDefault();
                setBrandingIcon();
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setBrandingIcon failure", ex);
        }
    }
}
