package com.zrp200.rkpd2.items.quest;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Roots;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.items.weapon.enchantments.Grim;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;

public class FlexTape extends Item {
    {
        image = ItemSpriteSheet.FLEX_TAPE;
        usesTargeting = true;
        defaultAction = AC_THROW;
        stackable = true;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    public static final String AC_USE = "USE";

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_USE );
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_USE)){
            curUser = hero;
            curItem = this;
            GameScene.selectCell( zapper );
        }
    }

    @Override
    public Item random() {
        quantity = Random.IntRange( 1, 5 );
        return this;
    }

    @Override
    public int value() {
        return 22 * quantity;
    }

    @Override
    protected void onThrow(int cell) {
        Char ch = Actor.findChar(cell);
        if (ch != null){
            Sample.INSTANCE.play(Assets.Sounds.ATK_CROSSBOW);
            ch.sprite.burst(0x000000, 25);
            if (ch.alignment == Char.Alignment.ENEMY) {
                if (ch.resist(Grim.class) > 0.5f) {
                    ch.die(new Grim());
                } else {
                    ch.damage(ch.HT / 4, new Grim());
                }
            } else {
                int healAmt = ch.HT - ch.HP;
                ch.HP += healAmt;
                ch.sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 1 );
                ch.sprite.showStatus( CharSprite.POSITIVE, Integer.toString( healAmt ) );
            }
            Warp.inflict(30, 3);
        } else {
            super.onThrow(cell);
        }
    }

    protected static CellSelector.Listener zapper = new  CellSelector.Listener() {

        @Override
        public void onSelect( Integer target ) {

            if (target != null) {

                if (target == curUser.pos) {
                    GLog.i( Messages.get(Wand.class, "self_target") );
                    return;
                }

                Char ch = Actor.findChar(target);

                if (ch != null){
                    Buff.affect(ch, Roots.class, 77);
                    Warp.inflict(25, 2.5f);
                    Sample.INSTANCE.play(Assets.Sounds.ATK_CROSSBOW);
                    ch.sprite.burst(0x000000, 25);

                    curUser.sprite.zap(target);

                    curItem.detach(curUser.belongings.backpack);
                }

            }
        }

        @Override
        public String prompt() {
            return Messages.get(FlexTape.class, "prompt");
        }
    };
}
