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

    public enum CustomBrandingTheme {
        ORIGINAL("revanced_original"),
        ROUNDED("revanced_rounded"),
        MINIMAL("revanced_minimal"),
        SCALED("revanced_scaled");

        public final String themeAlias;

        CustomBrandingTheme(String themeAlias) {
            this.themeAlias = themeAlias;
        }
    }

//    private static final String[] BRANDING_ALIAS_NAMES = new String[] {
//            ".revanced_default",
//            ".revanced_rounded",
//            ".revanced_minimal",
//            ".revanced_scaled"
//    };
//
//    public static void setAppIcon(Context context, String aliasName) {
//        PackageManager pm = context.getPackageManager();
//        String packageName = context.getPackageName();
//
//        // Disable all aliases first
//        for (String alias : BRANDING_ALIAS_NAMES) {
//            pm.setComponentEnabledSetting(
//                    new ComponentName(packageName, packageName + alias),
//                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                    PackageManager.DONT_KILL_APP
//            );
//        }
//
//        // Enable the one you want
//        pm.setComponentEnabledSetting(
//                new ComponentName(packageName, packageName + "." + aliasName),
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                PackageManager.DONT_KILL_APP
//        );
//    }

    /**
     * Injection point.
     */
    public static void setBrandingIcon() {
        try {
            Context context = Utils.getContext();
            CustomBrandingTheme selectedTheme = BaseSettings.CUSTOM_BRANDING_THEME.get();

            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();

            for (CustomBrandingTheme theme : CustomBrandingTheme.values()) {
                final int enabledDisabledState = theme == selectedTheme
                        ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

                pm.setComponentEnabledSetting(
                        new ComponentName(packageName, packageName + '.' + theme.themeAlias),
                        enabledDisabledState, //PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                );
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setBrandingIcon failure", ex);
        }
    }
}
