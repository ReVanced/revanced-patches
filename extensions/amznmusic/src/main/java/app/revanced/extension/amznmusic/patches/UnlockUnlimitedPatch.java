package app.revanced.extension.amznmusic.patches;

import com.amazon.music.account.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class UnlockUnlimitedPatch {
    public static Set<User.Benefit> createBenefitSet() {
        return new HashSet<>(Arrays.asList(User.Benefit.values()));
    }
}