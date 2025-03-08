package app.revanced.patches.youtube.playback.speed.custom;

import static app.revanced.patches.shared.StringRef.str;

import android.preference.ListPreference;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import app.revanced.patcher.annotation.Description;
import app.revanced.patcher.annotation.Name;
import app.revanced.patcher.annotation.Version;
import app.revanced.patcher.data.ResourceContext;
import app.revanced.patcher.patch.annotations.DependsOn;
import app.revanced.patcher.patch.annotations.Patch;
import app.revanced.patches.youtube.misc.settings.SettingsPatch;
import app.revanced.shared.annotation.YouTubeCompatibility;

@Patch
@Name("dynamic-playback-speed")
@Description("Adds ability to change playback speed dynamically by holding speed button.")
@DependsOn({SettingsPatch.class})
@YouTubeCompatibility
@Version("0.0.1")
public class CustomPlaybackSpeedPatch {
    private static final String SETTINGS_KEY = "revanced_dynamic_player_speed";
    
    private static final float PLAYBACK_SPEED_AUTO = 1.0f;
    private static final float PLAYBACK_SPEED_MINIMUM = 0.0625f;
    private static final float PLAYBACK_SPEED_MAXIMUM = 8.0f;
    
    private static float currentSpeed = PLAYBACK_SPEED_AUTO;
    private static final float[] AVAILABLE_SPEEDS = {
        0.0625f, 0.125f, 0.25f, 0.5f, 0.75f, 
        1.0f, 
        1.25f, 1.5f, 1.75f, 2.0f, 2.25f, 2.5f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f
    };

    public static boolean isEnabled() {
        return SettingsPatch.getBooleanSetting(SETTINGS_KEY, true);
    }

    public static float getSpeedMultiplier() {
        return SettingsPatch.getFloatSetting("revanced_speed_multiplier", 2.0f);
    }

    public static float getSpeedDivider() {
        return SettingsPatch.getFloatSetting("revanced_speed_divider", 2.0f);
    }

    private static float calculateDynamicSpeed(boolean increase) {
        float multiplier = increase ? getSpeedMultiplier() : (1 / getSpeedDivider());
        float newSpeed = currentSpeed * multiplier;
        
        if (newSpeed < PLAYBACK_SPEED_MINIMUM) return PLAYBACK_SPEED_MINIMUM;
        if (newSpeed > PLAYBACK_SPEED_MAXIMUM) return PLAYBACK_SPEED_MAXIMUM;
        
        return findNearestAvailableSpeed(newSpeed);
    }

    private static float findNearestAvailableSpeed(float targetSpeed) {
        float nearestSpeed = PLAYBACK_SPEED_AUTO;
        float minDiff = Math.abs(targetSpeed - PLAYBACK_SPEED_AUTO);
        
        for (float speed : AVAILABLE_SPEEDS) {
            float diff = Math.abs(targetSpeed - speed);
            if (diff < minDiff) {
                minDiff = diff;
                nearestSpeed = speed;
            }
        }
        return nearestSpeed;
    }

    public static void onSpeedGestureStart(boolean increase) {
        if (!isEnabled()) return;
        
        float newSpeed = calculateDynamicSpeed(increase);
        if (newSpeed != currentSpeed) {
            currentSpeed = newSpeed;
            applyPlaybackSpeed(currentSpeed);
        }
    }

    public static void onSpeedGestureEnd() {
        if (!isEnabled()) return;
        
        if (currentSpeed != PLAYBACK_SPEED_AUTO) {
            currentSpeed = PLAYBACK_SPEED_AUTO;
            applyPlaybackSpeed(PLAYBACK_SPEED_AUTO);
        }
    }

    private static void applyPlaybackSpeed(float speed) {
        try {
            YouTubePlayerController.setPlaybackSpeed(speed);
        } catch (Exception ex) {
            LoggerUtil.printException(() -> "Failed to apply playback speed", ex);
        }
    }
}
