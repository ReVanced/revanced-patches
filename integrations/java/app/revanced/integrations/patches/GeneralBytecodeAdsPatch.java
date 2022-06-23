package app.revanced.integrations.patches;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;

import app.revanced.integrations.adremover.LithoAdRemoval;

public class GeneralBytecodeAdsPatch {

    //Used by app.revanced.patches.youtube.ad.general.bytecode.patch.GeneralBytecodeAdsPatch
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean containsAd(String value, ByteBuffer buffer) {
        return LithoAdRemoval.containsAd(value, buffer);
    }

}
