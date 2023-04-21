package app.revanced.integrations.sponsorblock.ui;

import static app.revanced.integrations.utils.ReVancedUtils.getResourceColor;
import static app.revanced.integrations.utils.ReVancedUtils.getResourceDimension;
import static app.revanced.integrations.utils.ReVancedUtils.getResourceDimensionPixelSize;
import static app.revanced.integrations.utils.ReVancedUtils.getResourceIdentifier;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Objects;

import app.revanced.integrations.sponsorblock.SegmentPlaybackController;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.utils.LogHelper;

public class SkipSponsorButton extends FrameLayout {
    private static final boolean highContrast = true;
    private final LinearLayout skipSponsorBtnContainer;
    private final TextView skipSponsorTextView;
    private final Paint background;
    private final Paint border;
    private SponsorSegment segment;
    final int defaultBottomMargin;
    final int ctaBottomMargin;

    public SkipSponsorButton(Context context) {
        this(context, null);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, 0);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(getResourceIdentifier(context, "skip_sponsor_button", "layout"), this, true);  // layout:skip_ad_button
        setMinimumHeight(getResourceDimensionPixelSize("ad_skip_ad_button_min_height"));  // dimen:ad_skip_ad_button_min_height
        skipSponsorBtnContainer = Objects.requireNonNull((LinearLayout) findViewById(getResourceIdentifier(context, "sb_skip_sponsor_button_container", "id")));  // id:skip_ad_button_container
        background = new Paint();
        background.setColor(getResourceColor("skip_ad_button_background_color"));  // color:skip_ad_button_background_color);
        background.setStyle(Paint.Style.FILL);
        border = new Paint();
        border.setColor(getResourceColor("skip_ad_button_border_color"));  // color:skip_ad_button_border_color);
        border.setStrokeWidth(getResourceDimension("ad_skip_ad_button_border_width"));  // dimen:ad_skip_ad_button_border_width);
        border.setStyle(Paint.Style.STROKE);
        skipSponsorTextView = Objects.requireNonNull((TextView) findViewById(getResourceIdentifier(context, "sb_skip_sponsor_button_text", "id")));  // id:skip_ad_button_text;
        defaultBottomMargin = getResourceDimensionPixelSize("skip_button_default_bottom_margin");  // dimen:skip_button_default_bottom_margin
        ctaBottomMargin = getResourceDimensionPixelSize("skip_button_cta_bottom_margin");  // dimen:skip_button_cta_bottom_margin

        skipSponsorBtnContainer.setOnClickListener(v -> {
            SegmentPlaybackController.onSkipSegmentClicked(segment);
        });
    }

    @Override  // android.view.ViewGroup
    protected final void dispatchDraw(Canvas canvas) {
        final int left = skipSponsorBtnContainer.getLeft();
        final int top = skipSponsorBtnContainer.getTop();
        final int leftPlusWidth = (left + skipSponsorBtnContainer.getWidth());
        final int topPlusHeight = (top + skipSponsorBtnContainer.getHeight());
        canvas.drawRect(left, top, leftPlusWidth, topPlusHeight, background);
        if (!highContrast) {
            canvas.drawLines(new float[]{
                            leftPlusWidth, top, left, top,
                            left, top, left, topPlusHeight,
                            left, topPlusHeight, leftPlusWidth, topPlusHeight},
                    border);
        }

        super.dispatchDraw(canvas);
    }

    /**
     * @return true, if this button state was changed
     */
    public boolean updateSkipButtonText(@NonNull SponsorSegment segment) {
        this.segment = segment;
        CharSequence newText = segment.getSkipButtonText();
        if (newText.equals(skipSponsorTextView.getText())) {
            return false;
        }
        skipSponsorTextView.setText(newText);
        return true;
    }
}
