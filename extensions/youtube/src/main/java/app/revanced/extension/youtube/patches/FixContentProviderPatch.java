package app.revanced.extension.youtube.patches;

import java.util.Map;

import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public class FixContentProviderPatch {

    /**
     * Injection point.
     */
    public static void removeNullMapEntries(Map<?, ?> map) {
        map.entrySet().removeIf(entry -> {
            Object value = entry.getValue();
            if (value == null) {
                Logger.printDebug(() -> "Removing content provider key with null value: " + entry.getKey());
                return true;
            }
            return false;
        });
    }
}

