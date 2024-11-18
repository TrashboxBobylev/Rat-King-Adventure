package com.zrp200.rkpd2.items.spells;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Scam;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;

public class ScammingSpell extends Spell{

    {
        image = ItemSpriteSheet.ALCHEMIZE;
    }

    @Override
    protected void onCast(Hero hero) {
        Warp.inflict(50, 1.5f);
        Buff.prolong(hero, Scam.class, 35f);
        hero.sprite.operate(hero.pos);
        Sample.INSTANCE.play(Assets.Sounds.CURSED, 1f, 0.3f);
        hero.sprite.emitter().burst( Speck.factory( Speck.STENCH ), 40);
        GLog.p(Messages.get(this, "apply"));

        detach( curUser.belongings.backpack );
        updateQuickslot();
        hero.spendAndNext( 1f );
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
            inputs =  new Class[]{Kromer.class, ArcaneCatalyst.class};
            inQuantity = new int[]{1, 1};

            cost = 22;

            output = ScammingSpell.class;
            outQuantity = 11;
        }

    }
}
