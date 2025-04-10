package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.Gravery;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Corruption;
import com.zrp200.rkpd2.actors.buffs.SoulMark;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.mobs.Wraith;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.effects.particles.ShadowParticle;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class TheReaper extends MeleeWeapon {
    {
        image = ItemSpriteSheet.REAPER;
        tier = 6;

        ACC = 0.7f; //30% penalty to accuracy
    }

    @Override
    public int max(int lvl) {
        return  Math.round(7f*(tier+1)) +    //49 base, up from 35
                lvl*(tier+1);                   //scaling unchanged
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        int wraithAmount = Random.Int(1, 3);

        ArrayList<Integer> respawnPoints = new ArrayList<>();

        for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
            int p = attacker.pos + PathFinder.NEIGHBOURS8[i];
            if (Actor.findChar( p ) == null && !Dungeon.level.solid[p]) {
                respawnPoints.add( p );
            }
        }

        while (wraithAmount > 0 && respawnPoints.size() > 0) {
            int index = Random.index( respawnPoints );

            Wraith w = new Wraith();
            w.adjustStats(Dungeon.scalingDepth());
            w.pos = respawnPoints.get(index);
            w.state = w.HUNTING;
            Buff.affect(w, Corruption.class);
            GameScene.add( w, 1f);
            Dungeon.level.occupyCell(w);

            w.sprite.alpha( 0 );
            w.sprite.parent.add( new AlphaTweener( w.sprite, 1, 0.5f ) );

            w.sprite.emitter().burst( ShadowParticle.CURSE, 5 );
            Sample.INSTANCE.play(Assets.Sounds.CURSED, 1f, Random.Float(0.5f, 1.5f));

            respawnPoints.remove( index );
            wraithAmount--;
        }

        return super.proc(attacker, defender, damage);
    }

    @Override
    public int warriorAttack(int damage, Char enemy) {
        SoulMark.process(enemy, 5, 1.0f, false, true);
        return super.warriorAttack(damage, enemy);
    }

    @Override
    public float warriorMod() {
        return 0.0f;
    }

    @Override
    public float warriorDelay() {
        return 0f;
    }

    @Override
    protected int baseChargeUse(Hero hero, Char target) {
        return super.baseChargeUse(hero, target)*10;
    }

    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        if (target == null) {
            target = hero.pos;
        }

        beforeAbilityUsed(hero, null);
        PathFinder.buildDistanceMap( target, BArray.not( Dungeon.level.solid, null ), 2 );
        for (int i = 0; i < PathFinder.distance.length; i++) {
            if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                if (!Dungeon.level.pit[i]) {
                    int duration = 50;
                    GameScene.add(Blob.seed(i, duration, Gravery.class));
                    CellEmitter.get(i).burst(MagicMissile.YogParticle.FACTORY, 3);
                }
            }
        }
        Sample.INSTANCE.play(Assets.Sounds.BURNING, 1, 0.5f);
        Sample.INSTANCE.play(Assets.Sounds.CURSED, 1, 0.5f);
        hero.sprite.operate(hero.pos);
        hero.spendAndNext(Actor.TICK);
        afterAbilityUsed(hero);
    }
}
