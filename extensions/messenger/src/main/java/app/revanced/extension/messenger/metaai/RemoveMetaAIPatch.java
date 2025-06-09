package app.revanced.extension.messenger.metaai;

import java.util.HashSet;
import java.util.Set;

import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public class RemoveMetaAIPatch {
    private static final Set<Long> loggedIDs = new HashSet<>();

    public static boolean overrideBooleanFlag(long id, boolean value) {
        if (Long.toString(id).startsWith("REPLACED_BY_PATCH")) {
            synchronized (loggedIDs) {
                if (loggedIDs.add(id)) {
                    Logger.printInfo(() -> "Overriding " + id + " from " + value + " to false");
                }
            }
            return false;
        }

        return value;
    }
}
