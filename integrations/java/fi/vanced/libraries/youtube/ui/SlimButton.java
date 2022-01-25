package fi.vanced.libraries.youtube.ui;

import static fi.razerman.youtube.XGlobals.debug;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import fi.vanced.utils.VancedUtils;

public abstract class SlimButton implements View.OnClickListener {
    private static final String TAG = "VI - Slim - Button";
    public static int SLIM_METADATA_BUTTON_ID;
    public final View view;
    public final Context context;
    private final ViewGroup container;
    protected final ImageView button_icon;
    protected final TextView button_text;
    private boolean viewAdded = false;

    static {
        SLIM_METADATA_BUTTON_ID = VancedUtils.getIdentifier("slim_metadata_button", "layout");
    }

    public SlimButton(Context context, ViewGroup container, int id, boolean visible) {
        if (debug) {
            Log.d(TAG, "Adding button with id " + id + " and visibility of " + visible);
        }
        this.context = context;
        this.container = container;
        view = LayoutInflater.from(context).inflate(id, container, false);
        button_icon = (ImageView)view.findViewById(VancedUtils.getIdentifier("button_icon", "id"));
        button_text = (TextView)view.findViewById(VancedUtils.getIdentifier("button_text", "id"));

        view.setOnClickListener(this);

        setVisible(visible);
    }

    public void setVisible(boolean visible) {
        try {
            if (!viewAdded && visible) {
                container.addView(view);
                viewAdded = true;
            }
            else if (viewAdded && !visible) {
                container.removeView(view);
                viewAdded = false;
            }
            setContainerVisibility();
        }
        catch (Exception ex) {
            Log.e(TAG, "Error while changing button visibility", ex);
        }
    }

    private void setContainerVisibility() {
        if (container == null) return;

        for (int i = 0; i < container.getChildCount(); i++) {
            if (container.getChildAt(i).getVisibility() == View.VISIBLE) {
                container.setVisibility(View.VISIBLE);
                return;
            }
        }

        container.setVisibility(View.GONE);
    }
}
