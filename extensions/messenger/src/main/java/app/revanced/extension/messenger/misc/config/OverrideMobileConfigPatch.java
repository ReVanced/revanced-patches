package app.revanced.extension.messenger.misc.config;

@SuppressWarnings("unused")
public class OverrideMobileConfigPatch {
    private static final MobileConfigOverrider[] overriders = new MobileConfigOverrider[] {
        new DummyOverrider() // Replaced by patch.
    };

    public static boolean overrideConfigBool(long id, boolean value) {
        for (MobileConfigOverrider overrider : overriders) {
            boolean result = overrider.overrideConfigBool(id, value);
            if (result != value)
                return result;
        }

        return value;
    }
}

/**
 * Placeholder for actual overriders.
 */
final class DummyOverrider extends MobileConfigOverrider { }
