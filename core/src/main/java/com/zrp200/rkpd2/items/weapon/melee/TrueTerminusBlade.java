package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Combo;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.LiquidMetal;
import com.zrp200.rkpd2.items.rings.RingOfForce;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.journal.Document;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;

public class TrueTerminusBlade extends MeleeWeapon implements Talent.SpellbladeForgeryWeapon {

    private static final String AC_ZAP = "ZAP";

    {
        image = ItemSpriteSheet.TRUE_TERMINUS;
        tier = 6;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1f;

        defaultAction = AC_ZAP;
        usesTargeting = true;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public int STRReq(int lvl) {
        if (Dungeon.hero != null){
            return Dungeon.hero.STR();
        }
        return super.STRReq(lvl);
    }

    @Override
    public int min(int lvl) {
        float t;
        if (Dungeon.hero == null) t = 6;
        else {
            t = RingOfForce.tier(STRReq(lvl));
        }
        tier = (int) Math.ceil(t);

        return Math.max( 0, Math.round(
                t +  //base
                        lvl     //level scaling
        ));
    }

    @Override
    public int max(int lvl) {
        float t;
        if (Dungeon.hero == null) t = 6;
        else {
            t = RingOfForce.tier(STRReq(lvl));
        }
        tier = (int) Math.ceil(t);

        return Math.max( 0, Math.round(
                6*(t+1) +    //base
                        lvl*(t+1)    //level scaling
        ));
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_ZAP );

        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {

        super.execute( hero, action );

        if (action.equals( AC_ZAP )) {

            curUser = hero;
            curItem = this;
            GameScene.selectCell( zapper );

        }
    }

    @Override
    protected boolean hasAbility() {
        return false;
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        defender.trueDamage(damage);

        return super.proc(attacker, defender, -1);
    }

    public static boolean isWorthy(){
        boolean isHeroHere = Dungeon.hero != null;
        return (isHeroHere ? !Dungeon.hero.isClassed(HeroClass.RAT_KING) : false) && Document.TERMINUS.allPagesFound();
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        //no ability
    }

    public boolean tryToZap(Hero owner){
        if (!isEquipped(owner)){
            GLog.w( Messages.get(this, "no_equip") );
            return false;
        }

        return true;
    }

    protected static CellSelector.Listener zapper = new  CellSelector.Listener() {

        @Override
        public void onSelect( Integer target ) {

            if (target != null) {

                //FIXME this safety check shouldn't be necessary
                //it would be better to eliminate the curItem static variable.
                final TrueTerminusBlade curBlade;
                if (curItem instanceof TrueTerminusBlade) {
                    curBlade = (TrueTerminusBlade) curItem;
                } else {
                    return;
                }

                final Ballistica shot = new Ballistica( curUser.pos, target, Ballistica.FRIENDLY_PROJECTILE);
                final int cell = shot.collisionPos;

                if (target == curUser.pos || cell == curUser.pos) {
                    GLog.i( Messages.get(Wand.class, "self_target") );
                    return;
                }

                curUser.sprite.zap(cell);

                //attempts to target the cell aimed at if something is there, otherwise targets the collision pos.
                if (Actor.findChar(target) != null)
                    QuickSlotButton.target(Actor.findChar(target));
                else
                    QuickSlotButton.target(Actor.findChar(cell));

                if (curBlade.tryToZap(curUser)) {

                    curUser.busy();
                    Invisibility.dispel();

                    if (curBlade.cursed){
                        if (!curBlade.cursedKnown){
                            GLog.n(Messages.get(Wand.class, "curse_discover", curBlade.name()));
                        }
                    } else {
                        Sample.INSTANCE.play(Assets.Sounds.HIT_SLASH);
                        ((MissileSprite) curUser.sprite.parent.recycle(MissileSprite.class)).
                                reset(curUser.sprite,
                                        cell,
                                        new TerminusMissile(),
                                        new Callback() {
                                            @Override
                                            public void call() {
                                                Char enemy = Actor.findChar( cell );
                                                if (enemy != null && enemy != curUser) {
                                                    if (Char.hit(curUser, enemy, true)) {
                                                        curUser.attack(enemy, 1.0f, 0, 100000);
                                                        Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC);
                                                    } else {
                                                        enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
                                                        Combo combo = curUser.buff(Combo.class);
                                                        if (combo != null) combo.miss( );
                                                    }
                                                } else {
                                                    Dungeon.level.pressCell(cell);
                                                }
                                                Splash.at(cell, 0xFFFFFF, 15);
                                                updateQuickslot();
                                                int slot = Dungeon.quickslot.getSlot(curBlade);
                                                if (slot != -1){
                                                    Dungeon.quickslot.clearSlot(slot);
                                                    updateQuickslot();
                                                    Dungeon.quickslot.setSlot( slot, curBlade );
                                                    updateQuickslot();
                                                }
                                                curUser.spendAndNext(curBlade.delayFactor(curUser)*2);
                                            }
                                        });
                    }
                    curBlade.cursedKnown = true;

                }

            }
        }

        @Override
        public String prompt() {
            return Messages.get(RunicBlade.class, "prompt");
        }
    };

    public static class TerminusMissile extends Item {
        {
            image = ItemSpriteSheet.TRUE_TERMINUS;
        }

        @Override
        public Emitter emitter() {
            Emitter e = new Emitter();
            e.pos(7.5f, 7.5f);
            e.fillTarget = false;
            e.pour(MagicMissile.ForceParticle.FACTORY, 0.005f);
            return e;
        }
    }

    public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipeLocked {

        {
            inputs =  new Class[]{LiquidMetal.class};
            inQuantity = new int[]{250};

            cost = 25;

            output = TrueTerminusBlade.class;
            outQuantity = 1;
        }

        @Override
        public boolean isAvailable() {
            return isWorthy();
        }
    }

    public static class DamageType {}


}
