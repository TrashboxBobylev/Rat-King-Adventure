package com.zrp200.rkpd2.items.quest.nerfEnchants;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.CorrosiveGas;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;

public class Venomous extends Weapon.Enchantment {
    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int level = Math.max(0, weapon.buffedLvl());
        if (Weapon.Enchantment.proc(attacker, level, 1, 4)) {
            int centerVolume = 22;
            Sample.INSTANCE.play( Assets.Sounds.GAS );
            for (int i : PathFinder.NEIGHBOURS4){
                if (!Dungeon.level.solid[defender.pos+i]){
                    GameScene.add( Blob.seed( defender.pos+i, 5, CorrosiveGas.class ).setStrength( 3 + level/3));
                } else {
                    centerVolume += 7;
                }
            }

            GameScene.add( Blob.seed( defender.pos, centerVolume, CorrosiveGas.class ).setStrength( 3 + level/3));
        }
        return damage;
    }

    public static ItemSprite.Glowing VIOLET = new ItemSprite.Glowing( 0x6800b3 );

    @Override
    public ItemSprite.Glowing glowing() {
        return VIOLET;
    }
}
