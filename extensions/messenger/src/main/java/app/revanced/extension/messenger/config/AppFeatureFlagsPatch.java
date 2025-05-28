package app.revanced.extension.messenger.config;

@SuppressWarnings("unused")
public class AppFeatureFlagsPatch {
    private static final AppFeatureFlagsOverrider[] overriders = new AppFeatureFlagsOverrider[] {
        new DummyOverrider() // Replaced by patch.
    };

    public static boolean overrideBooleanFlag(long id, boolean value) {
        for (AppFeatureFlagsOverrider overrider : overriders) {
            Boolean result = overrider.overrideBooleanFlag(id, value);
            if (result != null)
                return result;
        }

        return value;
    }
}

/**
 * Placeholder for actual overriders.
 */
final class DummyOverrider implements AppFeatureFlagsOverrider {
    @Override
    public Boolean overrideBooleanFlag(long id, boolean value) {
        return null;
    }
}
