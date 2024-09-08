package com.zrp200.rkpd2.items.potions.elixirs;

import com.watabou.utils.Random;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.HighnessBuff;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.potions.PotionOfStrength;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;

public class KromerPotion extends Elixir{

    {
        image = ItemSpriteSheet.EXOTIC_KRONER;
    }

    @Override
    public void apply(Hero hero) {
        Warp.inflict(50, 3f);
        hero.STR += 7;
        Buff.affect(hero, Effect.class);
        hero.sprite.showStatus( CharSprite.POSITIVE, Messages.get(this, "msg_1") );
        GLog.p( Messages.get(this, "msg_2") );
        HighnessBuff.agreenalineProc();
    }

    private static final ItemSprite.Glowing CHAOTIC = new ItemSprite.Glowing( 0.2f  );

    @Override
    public ItemSprite.Glowing glowing() {
        return CHAOTIC;
    }

    @Override
    public int value() {
        return quantity * Random.Int(6, 1341);
    }

    public static class Effect extends Buff {
        {
            revivePersists = true;
        }
    }

    public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {

        {
            inputs =  new Class[]{PotionOfStrength.class, Kromer.class};
            inQuantity = new int[]{1, 1};

            cost = 12;

            output = KromerPotion.class;
            outQuantity = 1;
        }

    }
}
