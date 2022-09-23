package app.revanced.integrations.patches;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class VideoSpeedPatch {

    public static final float[] videoSpeeds = { 0, 0 }; // Values are useless as they are being overridden by the respective patch
    private static Boolean userChangedSpeed = false;

    public static int getDefaultSpeed(Object[] speeds, int speed, Object qInterface) {
        int speed2;
        Exception e;
        if (!ReVancedUtils.isNewVideoStarted()) {
            return speed;
        }
        ReVancedUtils.setNewVideo(false);
        LogHelper.debug(VideoSpeedPatch.class, "Speed: " + speed);
        float preferredSpeed = SettingsEnum.PREFERRED_VIDEO_SPEED.getFloat();
        LogHelper.debug(VideoSpeedPatch.class, "Preferred speed: " + preferredSpeed);
        if (preferredSpeed == -2.0f) {
            return speed;
        }
        Class<?> floatType = Float.TYPE;
        ArrayList<Float> iStreamSpeeds = new ArrayList<>();
        try {
            for (Object streamSpeed : speeds) {
                Field[] fields = streamSpeed.getClass().getFields();
                for (Field field : fields) {
                    if (field.getType().isAssignableFrom(floatType)) {
                        float value = field.getFloat(streamSpeed);
                        if (field.getName().length() <= 2) {
                            iStreamSpeeds.add(value);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        Iterator<Float> it = iStreamSpeeds.iterator();
        int index = 0;
        while (it.hasNext()) {
            float streamSpeed2 = it.next();
            LogHelper.debug(VideoSpeedPatch.class, "Speed at index " + index + ": " + streamSpeed2);
            index++;
        }
        int speed3 = -1;
        for (float streamSpeed3 : iStreamSpeeds) {
            if (streamSpeed3 <= preferredSpeed) {
                speed3++;
                LogHelper.debug(VideoSpeedPatch.class, "Speed loop at index " + speed3 + ": " + streamSpeed3);
            }
        }
        if (speed3 == -1) {
            LogHelper.debug(VideoSpeedPatch.class, "Speed was not found");
            speed2 = 3;
        } else {
            speed2 = speed3;
        }
        try {
            Method[] declaredMethods = qInterface.getClass().getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.getName().length() <= 2) {
                    LogHelper.debug(VideoSpeedPatch.class, "Method name: " + method.getName());
                    try {
                        try {
                            method.invoke(qInterface, videoSpeeds[speed2]);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
                        } catch (Exception e6) {
                            e = e6;
                            LogHelper.printException(VideoSpeedPatch.class, e.getMessage());
                            return speed2;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception e10) {
            e = e10;
        }
        LogHelper.debug(VideoSpeedPatch.class, "Speed changed to: " + speed2);
        return speed2;
    }

    public static void userChangedSpeed() {
        userChangedSpeed = true;
    }

    public static float getSpeedValue(Object[] speeds, int speed) {
        int i = 0;
        if (!ReVancedUtils.isNewVideoStarted() || userChangedSpeed) {
            if (SettingsEnum.DEBUG.getBoolean() && userChangedSpeed) {
                LogHelper.debug(VideoSpeedPatch.class, "Skipping speed change because user changed it: " + speed);
            }
            userChangedSpeed = false;
            return -1.0f;
        }
        ReVancedUtils.setNewVideo(false);
        LogHelper.debug(VideoSpeedPatch.class, "Speed: " + speed);
        float preferredSpeed = SettingsEnum.PREFERRED_VIDEO_SPEED.getFloat();
        LogHelper.debug(VideoSpeedPatch.class, "Preferred speed: " + preferredSpeed);
        if (preferredSpeed == -2.0f) {
            return -1.0f;
        }
        Class<?> floatType = Float.TYPE;
        ArrayList<Float> iStreamSpeeds = new ArrayList<>();
        try {
            int length = speeds.length;
            int i2 = 0;
            while (i2 < length) {
                Object streamSpeed = speeds[i2];
                Field[] fields = streamSpeed.getClass().getFields();
                int length2 = fields.length;
                while (i < length2) {
                    Field field = fields[i];
                    if (field.getType().isAssignableFrom(floatType)) {
                        float value = field.getFloat(streamSpeed);
                        if (field.getName().length() <= 2) {
                            iStreamSpeeds.add(value);
                        }
                    }
                    i++;
                }
                i2++;
                i = 0;
            }
        } catch (Exception ignored) {
        }
        int index = 0;
        for (Float iStreamSpeed : iStreamSpeeds) {
            float streamSpeed2 = iStreamSpeed;
            LogHelper.debug(VideoSpeedPatch.class, "Speed at index " + index + ": " + streamSpeed2);
            index++;
        }
        int newSpeedIndex = -1;
        for (Float iStreamSpeed : iStreamSpeeds) {
            float streamSpeed3 = iStreamSpeed;
            if (streamSpeed3 <= preferredSpeed) {
                newSpeedIndex++;
                LogHelper.debug(VideoSpeedPatch.class, "Speed loop at index " + newSpeedIndex + ": " + streamSpeed3);
            }
        }
        if (newSpeedIndex == -1) {
            LogHelper.debug(VideoSpeedPatch.class, "Speed was not found");
            newSpeedIndex = 3;
        }
        if (newSpeedIndex == speed) {
            LogHelper.debug(VideoSpeedPatch.class, "Trying to set speed to what it already is, skipping...: " + newSpeedIndex);
            return -1.0f;
        }
        LogHelper.debug(VideoSpeedPatch.class, "Speed changed to: " + newSpeedIndex);
        return getSpeedByIndex(newSpeedIndex);
    }

    private static float getSpeedByIndex(int index) {
        if (index == -2) {
            return 1.0f;
        }
        try {
            return videoSpeeds[index];
        } catch (Exception e) {
            return 1.0f;
        }
    }
}
