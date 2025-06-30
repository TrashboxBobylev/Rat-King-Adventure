package com.zrp200.rkpd2.items.journal;

import com.zrp200.rkpd2.journal.Document;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class SpecialSeedPage extends DocumentPage {

    {
        image = ItemSpriteSheet.SEED_PAGE;
    }

    @Override
    public Document document() {
        return Document.SPECIAL_SEEDS;
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", document().pageTitle(page()));
    }
}
