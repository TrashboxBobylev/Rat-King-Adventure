package com.zrp200.rkpd2.items.quest.nerfEnchants;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.sprites.ItemSprite;

public class Timetwisting extends Weapon.Enchantment {
    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int level = Math.max(0, weapon.buffedLvl());
        if (Weapon.Enchantment.proc(attacker, level, 1, 6)) {
            SpellSprite.show(defender, SpellSprite.HASTE, 1, 1, 0);
            Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC);
            defender.spend(Random.Int(3, 7));
        }
        return damage;
    }

    public static ItemSprite.Glowing YELLOW = new ItemSprite.Glowing( 0xdfff40 );

    @Override
    public ItemSprite.Glowing glowing() {
        return YELLOW;
    }
}
