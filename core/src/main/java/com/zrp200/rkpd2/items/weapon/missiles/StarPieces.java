package com.zrp200.rkpd2.items.weapon.missiles;

import com.watabou.utils.Callback;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.LiquidMetal;
import com.zrp200.rkpd2.items.rings.RingOfForce;
import com.zrp200.rkpd2.items.weapon.melee.TrueTerminusBlade;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;
import java.util.HashSet;

public class StarPieces extends MissileWeapon {
    {
        image = ItemSpriteSheet.STAR_PIECES;
        hitSound = Assets.Sounds.HIT_STAB;
        hitSoundPitch = 1.2f;

        tier = 6;
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
                2 * t +                  //base
                (t < 2 ? lvl : 2*lvl)   //level scaling
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
                4 * t +                      //base
                (t < 2 ? 2*lvl : t*lvl)  //level scaling
        ));
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        defender.trueDamage(damage);

        return super.proc(attacker, defender, -1);
    }

    @Override
    public float durabilityPerUse() {
        // cannot be boosted
        return MAX_DURABILITY / 7f;
    }

    private int amountOfBlades(){
        return 5 + buffedLvl()/2;
    }

    //do not detach
    @Override
    protected void rangedHit(Char enemy, int cell) {}

    @Override
    protected void rangedMiss(int cell) {}

    @Override
    public void cast(Hero user, int dst) {
        if (Dungeon.hero.visibleEnemies() == 0){
            GLog.w(Messages.get(this, "no_target"));
            return;
        }

        decrementDurability();

        final ArrayList<Char> targets = new ArrayList<>();

        int amount = 0;

        while (amount < amountOfBlades()){
            for (Mob mob: Dungeon.hero.getVisibleEnemies()){
                targets.add(mob);
                if (++amount >= amountOfBlades()){
                    break;
                }
            }
        }

        final HashSet<Callback> callbacks = new HashSet<>();

        for (Char ch : targets) {
            Item proto = new StarPieces();

            Callback callback = new Callback() {
                @Override
                public void call() {
                    user.shoot( ch, StarPieces.this );
                    callbacks.remove( this );
                    if (callbacks.isEmpty()) {
                        Invisibility.dispel();
                        user.spendAndNext( user.attackDelay() );
                    }
                }
            };

            MissileSprite m = user.sprite.parent.recycle( MissileSprite.class );
            m.reset( user.sprite, ch.pos, proto, callback );

            callbacks.add( callback );
        }

        user.sprite.zap( user.pos );
        user.busy();
    }

    public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipeLocked {

        {
            inputs =  new Class[]{LiquidMetal.class};
            inQuantity = new int[]{250};

            cost = 25;

            output = StarPieces.class;
            outQuantity = 1;
        }

        @Override
        public boolean isAvailable() {
            return TrueTerminusBlade.isWorthy();
        }
    }
}
