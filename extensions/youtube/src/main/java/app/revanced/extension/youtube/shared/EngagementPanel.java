package app.revanced.extension.youtube.shared;

import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracks the currently open engagement panel, such as the video description panel.
 */
public final class EngagementPanel {

    private static final AtomicReference<String> engagementPanelId = new AtomicReference<>("");

    private EngagementPanel() {
        // Utility class; prevent instantiation.
    }

    /**
     * Sets the engagement panel ID when a panel is opened.
     *
     * @param panelId The ID of the panel that was opened.
     */
    public static void setId(@Nullable String panelId) {
        if (panelId != null && !panelId.isEmpty()) {
            Logger.printDebug(() -> "engagementPanel open\npanelId: " + panelId);
            engagementPanelId.set(panelId);
        }
    }

    /**
     * Clears the engagement panel ID when the panel is closed.
     */
    public static void hide() {
        String id = getId();
        if (!id.isEmpty()) {
            Logger.printDebug(() -> "engagementPanel closed\npanelId: " + id);
            engagementPanelId.set("");
        }
    }

    /**
     * Returns true if any engagement panel is currently open.
     */
    public static boolean isOpen() {
        return !getId().isEmpty();
    }

    /**
     * Returns true if the video description panel is currently open.
     */
    public static boolean isDescription() {
        return getId().equals("video-description-ep-identifier");
    }

    /**
     * Gets the current engagement panel ID.
     */
    public static String getId() {
        return engagementPanelId.get();
    }
}
