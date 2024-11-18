package com.zrp200.rkpd2.items.wands;

import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

import java.util.Arrays;

public class WandOfUnstable2 extends Wand {

    {
        image = ItemSpriteSheet.WAND_UNSTABLE_2;
    }

    public static Class<?extends Wand>[] wands = new Class[]{
            WandOfBlastWave.class,
            WandOfCorrosion.class,
            WandOfCorruption.class,
            WandOfFrost.class,
            WandOfFirebolt.class,
            WandOfLightning.class,
            WandOfLivingEarth.class,
            WandOfDisintegration.class,
            WandOfMagicMissile.class,
            WandOfPrismaticLight.class,
            WandOfTransfusion.class,
            WandOfFireblast.class,
            WandOfRegrowth.class
    };

    private Wand wand;

    @Override
    public int collisionProperties(int target) {
        wand = Reflection.newInstance(Random.element(wands));
        if (wand != null) {
            return wand.collisionProperties(target);
        }
        return super.collisionProperties(target);
    }

    @Override
    public void onZap(Ballistica attack) {
        if (wand != null) {
            wand.onZap(attack);
            wand = null;
        }
    }

    @Override
    public void fx(Ballistica bolt, Callback callback) {
        if (wand != null) {
            if (Random.Int(3) == 0){
                CursedWand.cursedZap(wand,
                        curUser,
                        new Ballistica(curUser.pos, bolt.collisionPos, Ballistica.MAGIC_BOLT),
                        new Callback() {
                            @Override
                            public void call() {
                                WandOfUnstable2.this.wandUsed();
                            }
                        });
            } else {
                wand.level(level()*4);
                wand.fx(bolt, callback);
            }
            Warp.inflict(30, 0.75f);
        }
    }

    @Override
    public void onHit(Weapon staff, Char attacker, Char defender, int damage) {
        wand = Reflection.newInstance(Random.element(wands));
        if (wand != null) {
            if (Random.Int(3) == 0){
                CursedWand.cursedEffect(null, attacker, defender);
            }
            else {
                wand.level(level() * 4);
                wand.onHit(staff, attacker, defender, damage);
            }
        }
    }

    @Override
    public void staffFx(MagesStaff.StaffParticle particle) {
        particle.color(Random.element(Arrays.asList(
                0x00ff54, 0xf6e316, 0xff51c2, 0x4f4573)) );
        particle.am = 0.75f;
        particle.setLifespan(Random.Float(1f, 2.5f));
        particle.speed.set(Random.Int(-4, 4), Random.Int(-4, 4));
        particle.setSize( 1f, 3f);
    }

    public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipeBundled {

        {
            inputs =  new Class[]{WandOfUnstable.class, Kromer.class};
            inQuantity = new int[]{1, 1};

            cost = 20;

            output = WandOfUnstable2.class;
            outQuantity = 1;
        }

    }
}
