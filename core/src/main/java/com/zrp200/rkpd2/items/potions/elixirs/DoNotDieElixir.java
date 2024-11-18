package com.zrp200.rkpd2.items.potions.elixirs;

import com.watabou.utils.Random;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.HighnessBuff;
import com.zrp200.rkpd2.actors.buffs.NoDeath;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.potions.AlchemicalCatalyst;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class DoNotDieElixir extends Elixir{

    {
        image = ItemSpriteSheet.POTION_JADE;
    }

    @Override
    public void apply(Hero hero) {
        Warp.inflict(50, 1f);
        Buff.prolong(hero, NoDeath.class, 250f);
        HighnessBuff.agreenalineProc();
    }

    @Override
    public int value() {
        return quantity * Random.Int(5, 890);
    }

    private static final ItemSprite.Glowing CHAOTIC = new ItemSprite.Glowing( 0.2f  );

    @Override
    public ItemSprite.Glowing glowing() {
        return CHAOTIC;
    }

    public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {

        {
            inputs =  new Class[]{Kromer.class, AlchemicalCatalyst.class};
            inQuantity = new int[]{1, 1};

            cost = 12;

            output = DoNotDieElixir.class;
            outQuantity = 1;
        }

    }
}
