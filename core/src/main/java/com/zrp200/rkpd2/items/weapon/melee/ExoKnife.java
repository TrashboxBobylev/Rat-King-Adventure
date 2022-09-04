package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.particles.ExoParticle;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.mechanics.ConeAOE;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.BArray;

import java.util.ArrayList;

import static com.zrp200.rkpd2.Dungeon.hero;
import static com.zrp200.rkpd2.items.wands.WandOfUnstable.wands;

public class ExoKnife extends MeleeWeapon{
    {
        image = ItemSpriteSheet.EXO_KNIFE;
        tier = 6;
        RCH = 12;
    }

    @Override
    public int min(int lvl) {
        return 12 + lvl;
    }

    public static class RunicMissile extends Item {
        {
            image = ItemSpriteSheet.EXO_KNIFE;
        }

        @Override
        public Emitter emitter() {
            Emitter e = new Emitter();
            e.pos(7.5f, 7.5f);
            e.fillTarget = true;
            e.autoKill = false;
            e.pour(ExoParticle.FACTORY, 0.002f);
            return e;
        }
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        PathFinder.buildDistanceMap( defender.pos, BArray.not( Dungeon.level.solid, null ), 2 );
        for (int i = 0; i < PathFinder.distance.length; i++) {
            if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                Char ch = Actor.findChar(i);
                if (ch != null && ch != attacker && ch.alignment != Char.Alignment.ALLY && ch.isAlive()){
                    ch.damage(damageRoll(attacker), hero);
                    if (enchantment != null)
                        enchantment.proc(this, attacker, ch, damage);
                }
                CellEmitter.get(i).burst(ExoParticle.FACTORY, 5);
            }
        }
        return super.proc(attacker, defender, damage);
    }

    @Override
    public int max(int lvl) {
        return  5*(tier-2) +    //20 base, down from 35
                lvl*(tier-1);   //scaling changed to +5
    }

    @Override
    public int warriorAttack(int damage, Char enemy) {
        Ballistica bolt = new Ballistica(hero.pos, enemy.pos, Ballistica.WONT_STOP);
        int maxDist = 10;
        int dist = Math.min(bolt.dist, maxDist);
        curUser = hero;
        ConeAOE cone = new ConeAOE( bolt,
                maxDist,
                90,
                Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID);
                    ArrayList<Char> affectedChars = new ArrayList<>();
                    for( int cell : cone.cells ){

                        //ignore caster cell
                        if (cell == bolt.sourcePos){
                            continue;
                        }

                        //knock doors open
                        if (Dungeon.level.map[cell] == Terrain.DOOR){
                            Level.set(cell, Terrain.OPEN_DOOR);
                            GameScene.updateMap(cell);
                        }

                        Wand wand = Reflection.newInstance(Random.element(wands));
                        if (wand != null) {
                            wand.level(level());
                            wand.fx(new Ballistica(hero.pos, cell, Ballistica.STOP_TARGET), () -> {
                                wand.onZap(new Ballistica(hero.pos, cell, Ballistica.STOP_TARGET));
                            });
                        }

                        Char ch = Actor.findChar( cell );
                        if (ch != null) {
                            affectedChars.add(ch);
                        }
                    }
                    for ( Char ch : affectedChars ){
                        ch.damage(damage, this);
                    }
        Sample.INSTANCE.play( Assets.Sounds.ZAP );
        Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
        return 0;
    }

    @Override
    public float warriorDelay() {
        return 5;
    }
}
