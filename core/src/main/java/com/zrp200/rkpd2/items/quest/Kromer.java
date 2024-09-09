package com.zrp200.rkpd2.items.quest;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.Ratmogrify;
import com.zrp200.rkpd2.actors.hero.abilities.rat_king.LegacyWrath;
import com.zrp200.rkpd2.actors.hero.abilities.rat_king.MusRexIra;
import com.zrp200.rkpd2.actors.hero.abilities.rat_king.Wrath;
import com.zrp200.rkpd2.effects.Enchanting;
import com.zrp200.rkpd2.effects.Pushing;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.RatKingArmor;
import com.zrp200.rkpd2.items.wands.CursedWand;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.Icons;
import com.zrp200.rkpd2.ui.TalentsPane;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndOptions;

import java.util.ArrayList;

public class Kromer extends Item {

    public static final String AC_USE	= "USE";
    public static final String AC_FOCUS	= "FOCUS";

    {
        image = ItemSpriteSheet.KROMER;
        stackable = true;
        cursed = true;
        cursedKnown = true;
        defaultAction = AC_FOCUS;
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_USE );
        actions.add( AC_FOCUS);
        return actions;
    }

    private static final ItemSprite.Glowing CHAOTIC = new ItemSprite.Glowing( 0.2f  );

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (action.equals(AC_USE)){
            GameScene.show(new WndOptions(Icons.get(Icons.TALENT), Messages.get(Kromer.class, "wnd_title"),
                    Messages.get(Kromer.class, "wnd_message"),
                        Messages.get(TalentsPane.class, "tier", 1), Messages.get(TalentsPane.class, "tier", 2)
                    ){
                @Override
                protected boolean enabled(int index) {
                    return Dungeon.hero.lvl >= Talent.tierLevelThresholds[index+1];
                }

                @Override
                protected void onSelect(int index) {
                    if (Dungeon.hero.talents.get(index).size() > 7) {
                        GLog.n(Messages.get(Kromer.class, "too_many"));
                        return;
                    }
                    HeroClass cls;
                    Talent randomTalent = null;
                    while (randomTalent == null) {
                        do {
                            cls = Random.element(HeroClass.values());
                        } while (cls == Dungeon.hero.heroClass);
                        randomTalent = Random.element(Talent.talentList(cls, index + 1));
                    }
                    Dungeon.hero.talents.get(index).put(randomTalent, 0);
                    Sample.INSTANCE.play( Assets.Sounds.LEVELUP );
                    Dungeon.hero.sprite.emitter().burst(Speck.factory(Speck.STAR), 40);
                    Enchanting.show(Dungeon.hero, Kromer.this);
                    GLog.p(Messages.get(Kromer.class, "new_talent"));
                    detach(Dungeon.hero.belongings.backpack);
                    Warp.inflict(50, 5f);
                }
            });
        } else if (action.equals(AC_FOCUS)){
            GameScene.selectCell(focus);
        }
    }

    private CellSelector.Listener focus = new CellSelector.Listener() {

        private int t = -1;

        @Override
        public void onSelect(Integer cell) {
            if (cell != null) {
                GLog.n(Messages.get(Kromer.class, "no_way_back"));
                Dungeon.hero.sprite.zap(cell, () -> {
                    t = cell;
                    final Ballistica shot = new Ballistica(curUser.pos, cell, Ballistica.PROJECTILE);
                    CursedWand.cursedZap(Kromer.this, Dungeon.hero, shot, this::shoot);
                });
            }
        }

        public void shoot() {
            Ballistica shot = new Ballistica(Dungeon.hero.pos, t, Ballistica.PROJECTILE);
            Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
            Warp.inflict(10, 0.75f);
            Dungeon.hero.HP = Math.min(Dungeon.hero.HT, Dungeon.hero.HP + 2);
            curUser.busy();
            curUser.spendAndNext(1f);
            if (curUser.HP >= curUser.HT*0.5f && Random.Int(4) == 0){
                Dungeon.hero.sprite.zap(t, () -> {
                    CursedWand.cursedZap(Kromer.this, Dungeon.hero, shot, this::shoot);
                });
            } else {
                Dungeon.hero.ready();
            }
        }

        @Override
        public String prompt() {
            return Messages.get(Wand.class, "prompt");
        }
    };

    @Override
    public ItemSprite.Glowing glowing() {
        return CHAOTIC;
    }

    @Override
    protected void onThrow(int cell) {
        RatKingArmor armor = new RatKingArmor();
        armor.charge = 100f;
        Splash.at(cell, 0x0bd74e, 60);
        Actor.addDelayed(new Pushing(Dungeon.hero, Dungeon.hero.pos, Dungeon.hero.pos, () -> {
            GameScene.flash(0xfdfa31);
            Splash.at(cell, 0xfdfa31, 60);
            new LegacyWrath().activate(armor, Dungeon.hero, cell);
            new Wrath().activate(armor, Dungeon.hero, cell);
            new MusRexIra().activate(armor, Dungeon.hero, cell);
            new Ratmogrify().activate(armor, Dungeon.hero, cell);
            Warp.inflict(100, 1f);
        }), -1);
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public int value() {
        return quantity * Random.Int(1, 672);
    }
}


