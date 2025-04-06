package com.zrp200.rkpd2.items.artifacts;

import com.watabou.utils.Random;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.items.scrolls.Scroll;
import com.zrp200.rkpd2.items.scrolls.ScrollOfIdentify;
import com.zrp200.rkpd2.items.scrolls.ScrollOfMagicMapping;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRemoveCurse;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTransmutation;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;

public class BookOfWonder extends UnstableSpellbook {

    {
        image = ItemSpriteSheet.ARTIFACT_WONDER;
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new bookRecharge();
    }

    @Override
    public void execute(Hero hero, String action ) {

        super.execute( hero, action );

        if (hero.buff(MagicImmune.class) != null) return;

        if (action.equals( AC_READ )) {

            if (hero.buff( Blindness.class ) != null) GLog.w( Messages.get(this, "blinded") );
            else if (!isEquipped( hero ))             GLog.i( Messages.get(Artifact.class, "need_to_equip") );
            else if (charge <= 0)                     GLog.i( Messages.get(this, "no_charge") );
            else if (cursed)                          GLog.i( Messages.get(this, "cursed") );
            else {
                charge--;

                Scroll scroll;
                do {
                    scroll = (Scroll) Generator.randomUsingDefaults(Generator.Category.SCROLL);
                } while (scroll == null
                        //reduce the frequency of these scrolls by half
                        ||((scroll instanceof ScrollOfIdentify ||
                        scroll instanceof ScrollOfRemoveCurse ||
                        scroll instanceof ScrollOfMagicMapping) && Random.Int(2) == 0)
                        //cannot roll transmutation
                        || (scroll instanceof ScrollOfTransmutation));

                scroll.anonymize();
                curItem = scroll;
                curUser = hero;

                //if there are charges left and the scroll has been given to the book
                if (charge > 0 && !scrolls.contains(scroll.getClass())) {
                    scroll.empoweredRead();
                } else {
                    scroll.doRead();
                }
                Warp.inflict(20, 0.75f);
                Talent.onArtifactUsed(Dungeon.hero);
                updateQuickslot();
            }

        } else if (action.equals( AC_ADD )) {
            GameScene.selectItem(itemSelector);
        }
    }

    @Override
    protected float chargeMod() {
        return 1.5f;
    }

    public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipeBundled {

        {
            inputs =  new Class[]{UnstableSpellbook.class, Kromer.class};
            inQuantity = new int[]{1, 1};

            cost = 20;

            output = BookOfWonder.class;
            outQuantity = 1;
        }

    }
}
