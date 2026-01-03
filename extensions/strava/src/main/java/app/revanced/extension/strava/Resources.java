package app.revanced.extension.strava;

import app.revanced.extension.shared.Utils;

public final class Resources {
    public static final class Colors {
        public static int id(String name) {
            return Utils.getResourceIdentifier(name, "color");
        }
    }

    public static final class Drawables {
        public static int id(String name) {
            return Utils.getResourceIdentifier(name, "drawable");
        }
    }

    public static final class Strings {
        public static int id(String name) {
            return Utils.getResourceIdentifier(name, "string");
        }

        public static String get(String name, String fallback) {
            int id = id(name);
            return id != 0
                    ? Utils.getResourceString(id)
                    : fallback;
        }
    }
}
