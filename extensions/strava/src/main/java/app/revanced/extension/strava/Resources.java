package app.revanced.extension.strava;

import app.revanced.extension.shared.Utils;

public final class Resources {
    public static final class Colors {
        public static int get(String name) {
            return Utils.getResourceIdentifier(name, "color");
        }

        public static int coreAsphalt() {
            return get("core_asphalt");
        }
    }

    public static final class Drawables {
        public static int get(String name) {
            return Utils.getResourceIdentifier(name, "drawable");
        }

        public static int actionsLinkNormalXsmall() {
            return get("actions_link_normal_xsmall");
        }
    }

    public static final class Strings {
        public static int get(String name) {
            return Utils.getResourceIdentifier(name, "string");
        }

        public static int copyLink() {
            return get("copy_link");
        }
    }
}
