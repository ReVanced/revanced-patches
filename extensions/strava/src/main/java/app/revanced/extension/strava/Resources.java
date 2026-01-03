package app.revanced.extension.strava;

import app.revanced.extension.shared.Utils;

public final class Resources {
    public static final class Colors {
        public static int get(String name) {
            return Utils.getResourceIdentifier(name, "color");
        }
    }

    public static final class Drawables {
        public static int get(String name) {
            return Utils.getResourceIdentifier(name, "drawable");
        }
    }

    public static final class Strings {
        public static int get(String name) {
            return Utils.getResourceIdentifier(name, "string");
        }

        public static int saveImageSuccess() {
            return get("yis_2024_local_save_image_success");
        }

        public static int saveFailure() {
            return get("yis_2024_local_save_failure");
        }
    }
}
