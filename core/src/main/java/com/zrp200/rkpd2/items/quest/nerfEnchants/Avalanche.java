package com.zrp200.rkpd2.items.quest.nerfEnchants;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.traps.RockfallTrap;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.mechanics.ConeAOE;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;

public class Avalanche extends Weapon.Enchantment {

    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int level = Math.max(0, weapon.buffedLvl());
        if (Weapon.Enchantment.proc(attacker, level, 1, 6)) {
            final Ballistica bolt = new Ballistica(attacker.pos, defender.pos, Ballistica.WONT_STOP);

            int maxDist = 6;
            int dist = Math.min(bolt.dist, maxDist);

            final ConeAOE cone = new ConeAOE(bolt, 8, 70, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET | Ballistica.IGNORE_SOFT_SOLID);

            //cast to cells at the tip, rather than all cells, better performance.
            for (Ballistica ray : cone.outerRays){
                ((MagicMissile)attacker.sprite.parent.recycle( MagicMissile.class )).reset(
                        MagicMissile.EARTH_CONE,
                        attacker.sprite,
                        ray.path.get(ray.dist),
                        null
                );
            }
            Sample.INSTANCE.play(Assets.Sounds.ROCKS);

            MagicMissile.boltFromChar(attacker.sprite.parent,
                    MagicMissile.EARTH_CONE,
                    attacker.sprite,
                    bolt.path.get(dist / 2),
                    new Callback() {
                        @Override
                        public void call() {
                            for (int cell : cone.cells){
                                //ignore caster cell
                                if (cell == bolt.sourcePos){
                                    continue;
                                }

                                //knock doors open
                                if (Dungeon.level.map[cell] == Terrain.DOOR){
                                    Level.set(cell, Terrain.OPEN_DOOR);
                                    GameScene.updateMap(cell);
                                }

                                CellEmitter.get( cell ).burst( Speck.factory( Speck.ROCK ), 5 );

                                Char ch = Actor.findChar( cell );
                                if (ch != null) {
                                    float distance = Dungeon.level.trueDistance(attacker.pos, cell);
                                    ch.damage(Random.NormalIntRange(Math.round((4.5f + level/1.33f)*1f*((8f-distance)/8f)),
                                            Math.round((13.5f + level*3)*1f*((8f-distance)/8f))), new RockfallTrap());
                                    Buff.affect(ch, Cripple.class, 12f);
                                    if (Random.Int(3) == 0)
                                        Buff.affect(ch, Paralysis.class, 6f);
                                }
                            }
                        }
                    });
        }
        return damage;
    }

    public static ItemSprite.Glowing BROWN = new ItemSprite.Glowing( 0x736245 );

    @Override
    public ItemSprite.Glowing glowing() {
        return BROWN;
    }
}
