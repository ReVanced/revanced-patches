package app.revanced.extension.nunl;

import nl.nu.performance.api.client.interfaces.Block;
import nl.nu.performance.api.client.objects.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ScreenMapperPatch {
    private static final String[] blockedHeaderBlocks = {
            "Aanbiedingen (Adverteerders)",
            "Aangeboden door NUshop"
    };

    // "Rubrieken" menu links to ads
    private static final String[] blockedLinkBlocks = {
            "Van onze adverteerders"
    };

    public static void filterAds(List<Block> blocks) {
        ArrayList<Block> cleanedList = new ArrayList<>();

        boolean skipFullHeader = false;
        boolean skipUntilDivider = false;

        int index = 0;
        while (index < blocks.size()) {
            Block currentBlock = blocks.get(index);

            // because of pagination, we might not see the Divider in front of it
            // just remove it as is and leave potential extra spacing visible on the screen
            if (currentBlock instanceof DpgBannerBlock) {
                index++;
                continue;
            }

            if (index + 1 < blocks.size()) {
                // filter Divider -> DpgMediaBanner -> Divider
                if (currentBlock instanceof DividerBlock
                        && blocks.get(index + 1) instanceof DpgBannerBlock) {
                    index += 2;
                    continue;
                }

                // filter Divider -> LinkBlock (... -> LinkBlock -> LinkBlock-> LinkBlock -> Divider)
                if (currentBlock instanceof DividerBlock
                        && blocks.get(index + 1) instanceof LinkBlock linkBlock) {
                    Link link = linkBlock.getLink();
                    if (link != null && link.getTitle() != null) {
                        for (String blockedLinkBlock : blockedLinkBlocks) {
                            if (blockedLinkBlock.equals(link.getTitle().getText())) {
                                skipUntilDivider = true;
                                break;
                            }
                        }
                        if (skipUntilDivider) {
                            index++;
                            continue;
                        }
                    }
                }
            }

            if (currentBlock instanceof DividerBlock) {
                skipUntilDivider = false;
            }

            // filter HeaderBlock with known ads until next HeaderBlock
            if (currentBlock instanceof HeaderBlock headerBlock) {
                StyledText headerText = headerBlock.component20();
                if (headerText != null) {
                    skipFullHeader = false;
                    for (String blockedHeaderBlock : blockedHeaderBlocks) {
                        if (blockedHeaderBlock.equals(headerText.getText())) {
                            skipFullHeader = true;
                            break;
                        }
                    }
                    if (skipFullHeader) {
                        index++;
                        continue;
                    }
                }
            }

            if (!skipFullHeader && !skipUntilDivider) {
                cleanedList.add(currentBlock);
            }
            index++;
        }

        // replace list in-place to not deal with moving the result to the correct register in smali
        blocks.clear();
        blocks.addAll(cleanedList);
    }
}
