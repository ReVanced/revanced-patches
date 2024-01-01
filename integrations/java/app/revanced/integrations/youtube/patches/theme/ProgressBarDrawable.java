package app.revanced.integrations.youtube.patches.theme;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.patches.HideSeekbarPatch;
import app.revanced.integrations.youtube.settings.Settings;

/**
 * Used by {@link SeekbarColorPatch} change the color of the seekbar.
 * and {@link HideSeekbarPatch} to hide the seekbar of the feed and watch history.
 */
@SuppressWarnings("unused")
public class ProgressBarDrawable extends Drawable {

    private final Paint paint = new Paint();

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (Settings.HIDE_SEEKBAR_THUMBNAIL.get()) {
            return;
        }
        paint.setColor(SeekbarColorPatch.getSeekbarColor());
        canvas.drawRect(getBounds(), paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

}
