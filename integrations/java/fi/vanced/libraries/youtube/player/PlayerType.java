package fi.vanced.libraries.youtube.player;

import fi.vanced.libraries.youtube.sponsors.player.ui.SponsorBlockView;

public class PlayerType {
    public static void playerTypeChanged(String playerType) {
        SponsorBlockView.playerTypeChanged(playerType);
    }
}
