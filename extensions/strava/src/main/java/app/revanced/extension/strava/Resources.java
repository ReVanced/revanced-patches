package app.revanced.extension.strava;

import app.revanced.extension.shared.Utils;

public final class Resources {
    public static final class Colors {
        public static int get(String name) {
            return Utils.getResourceIdentifier(name, "color");
        }

        public static int primary() {
            return get("core_asphalt");
        }

        public static int accent() {
            return get("core_o3");
        }
    }

    public static final class Drawables {
        public static int get(String name) {
            return Utils.getResourceIdentifier(name, "drawable");
        }

        public static int link() {
            return get("actions_link_normal_xsmall");
        }

        public static int linkExternal() {
            return get("actions_link_external_normal_xsmall");
        }

        public static int download() {
            return get("actions_download_normal_xsmall");
        }
    }

    public static final class Strings {
        public static int get(String name) {
            return Utils.getResourceIdentifier(name, "string");
        }

        public static int copyLink() {
            return get("copy_link");
        }

        public static int openLink() {
            // FIXME "fallback_menu_item_open_in_browser" not found
            // Utils.getContext().getResources().getResourceName(0x7f1408b9);
            return get("ellipsis");
        }

        public static int download() {
            return get("download");
        }

        public static int saveImageSuccess() {
            return get("yis_2024_local_save_image_success");
        }

        public static int saveFailure() {
            return get("yis_2024_local_save_failure");
        }
    }
}
