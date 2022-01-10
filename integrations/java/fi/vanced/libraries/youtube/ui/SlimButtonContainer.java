package fi.vanced.libraries.youtube.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import com.google.android.apps.youtube.app.ui.SlimMetadataScrollableButtonContainerLayout;

import fi.vanced.utils.VancedUtils;

public class SlimButtonContainer extends SlimMetadataScrollableButtonContainerLayout {
    private static final String TAG = "VI - Slim - Container";
    private ViewGroup container;
    private CopyButton copyButton;
    private CopyWithTimestamp copyWithTimestampButton;
    public static AdBlock adBlockButton;

    public SlimButtonContainer(Context context) {
        super(context);
        this.initialize(context);
    }

    public SlimButtonContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize(context);
    }

    public SlimButtonContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize(context);
    }

    public void initialize(Context context) {
        try {
            container = this.findViewById(VancedUtils.getIdentifier("button_container_vanced", "id"));
            if (container == null) throw new Exception("Unable to initialize the button container because the button_container_vanced couldn't be found");

            copyButton = new CopyButton(context, this);
            copyWithTimestampButton = new CopyWithTimestamp(context, this);
            adBlockButton = new AdBlock(context, this);
            new SponsorBlock(context, this);
            new SponsorBlockVoting(context, this);
        }
        catch (Exception ex) {
            Log.e(TAG, "Unable to initialize the button container", ex);
        }
    }
}
