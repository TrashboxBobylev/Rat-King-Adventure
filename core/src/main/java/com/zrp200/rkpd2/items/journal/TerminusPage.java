package com.zrp200.rkpd2.items.journal;

import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.journal.Document;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;

public class TerminusPage extends DocumentPage {

    {
        image = ItemSpriteSheet.TERMINAL_PAG;
    }

    @Override
    public Document document() {
        return Document.TERMINUS;
    }

    @Override
    public boolean doPickUp(Hero hero) {
        boolean done = super.doPickUp(hero);

        if (Document.TERMINUS.allPagesFound()){
            GLog.p(Messages.get(this, "complete"));
        }

        return done;
    }
}
