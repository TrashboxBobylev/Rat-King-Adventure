package com.zrp200.rkpd2.items.quest.nerfEnchants;

import com.watabou.noosa.audio.Sample;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Corruption;
import com.zrp200.rkpd2.actors.mobs.Wraith;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.particles.ShadowParticle;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.ui.TargetHealthIndicator;

public class Necromancy extends Weapon.Enchantment {
    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int level = Math.max(0, weapon.buffedLvl());
        if (Weapon.Enchantment.proc(attacker, level, 1, 9)
                && !defender.properties().contains(Char.Property.BOSS)
                && !defender.properties().contains(Char.Property.MINIBOSS)) {
            int pos = defender.pos;
            Actor.remove( defender );
            defender.sprite.killAndErase();
            Dungeon.level.mobs.remove(defender);
            Wraith w = Wraith.spawnAt(pos);
            if (w != null) {
                Buff.affect(w, Corruption.class);
                CellEmitter.get(pos).burst(ShadowParticle.CURSE, 6);
                Sample.INSTANCE.play(Assets.Sounds.CURSED);
            }
            TargetHealthIndicator.instance.target(null);
            return 0;
        }
        return damage;
    }

    public static ItemSprite.Glowing DARK = new ItemSprite.Glowing( 0x404040 );

    @Override
    public ItemSprite.Glowing glowing() {
        return DARK;
    }
}
