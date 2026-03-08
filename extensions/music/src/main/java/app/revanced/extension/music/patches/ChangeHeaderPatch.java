package app.revanced.extension.music.patches;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.ResourceType;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.music.settings.Settings;

public class ChangeHeaderPatch {
    public enum HeaderLogo {
        DEFAULT(null),
        REVANCED("revanced_header_dark"),
        CUSTOM("revanced_header_custom_dark");

        private final String drawableName;

        HeaderLogo(String drawableName) {
            this.drawableName = drawableName;
        }

        private Integer getDrawableId() {
            if (drawableName == null) {
                return null;
            }

            int id = Utils.getResourceIdentifier(ResourceType.DRAWABLE, drawableName);
            if (id == 0) {
                Logger.printException(() ->
                        "Header drawable not found: " + drawableName
                );
                Settings.HEADER_LOGO.resetToDefault();
                return null;
            }

            return id;
        }
    }
}
