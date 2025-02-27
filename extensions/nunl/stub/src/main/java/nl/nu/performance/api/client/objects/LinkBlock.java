package nl.nu.performance.api.client.objects;

import android.os.Parcelable;
import nl.nu.performance.api.client.interfaces.Block;

public abstract class LinkBlock extends Block implements Parcelable {
    public final Link getLink() {
        throw new UnsupportedOperationException("Stub");
    }
}
