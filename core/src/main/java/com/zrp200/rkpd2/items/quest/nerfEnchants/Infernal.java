package com.zrp200.rkpd2.items.quest.nerfEnchants;

import com.watabou.utils.Random;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.particles.FlameParticle;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.sprites.ItemSprite;

public class Infernal extends Weapon.Enchantment {
    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int level = Math.max(0, weapon.buffedLvl());
        if (Weapon.Enchantment.proc(attacker, level, 1, 3)) {
                if (Random.Int(2) == 0) {
                    Buff.affect(defender, Burning.class).reignite(defender);
                }
                if (!defender.isImmune(getClass())) defender.damage(
                        (int) (Random.Int(3, level + 6) * (1 + Dungeon.hero.pointsInTalent(Talent.PYROMANIAC) * 0.085f)), this);

                defender.sprite.emitter().burst(FlameParticle.FACTORY, level + 1);

        }
        return damage;
    }

    public static ItemSprite.Glowing ORANGE = new ItemSprite.Glowing( 0xFF4400 );

    @Override
    public ItemSprite.Glowing glowing() {
        return ORANGE;
    }
}
