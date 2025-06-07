package app.revanced.extension.messenger.config;

@SuppressWarnings("unused")
public class AppFeatureFlagsPatch {
    /**
     * Placeholder for actual overriders.
     */
    private static final class DummyOverrider implements AppFeatureFlagsOverrider {
        @Override
        public Boolean overrideBooleanFlag(long id, boolean value) {
            return null;
        }
    }

    private static final AppFeatureFlagsOverrider[] overriders = new AppFeatureFlagsOverrider[] {
            new DummyOverrider() // Replaced by patch.
    };

    /**
     * Injection point.
     */
    public static boolean overrideBooleanFlag(long id, boolean value) {
        for (AppFeatureFlagsOverrider overrider : overriders) {
            Boolean result = overrider.overrideBooleanFlag(id, value);
            if (result != null)
                return result;
        }

        return value;
    }
}
