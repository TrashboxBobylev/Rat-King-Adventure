package com.zrp200.rkpd2.items.quest.nerfEnchants;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.effects.Lightning;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;

import java.util.ArrayList;

public class Galvanizing extends Weapon.Enchantment {
    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int level = Math.max(0, weapon.buffedLvl());
        if (Weapon.Enchantment.proc(attacker, level, 1, 5)) {
            defender.damage(Random.NormalIntRange(6 + Dungeon.scalingDepth() / 3, 12 + Dungeon.scalingDepth() / 3), this);
            Buff.affect(defender, Paralysis.class, 3f);

            CharSprite s = defender.sprite;
            if (s != null && s.parent != null) {
                ArrayList<Lightning.Arc> arcs = new ArrayList<>();
                arcs.add(new Lightning.Arc(new PointF(s.x, s.y + s.height / 2), new PointF(s.x + s.width, s.y + s.height / 2)));
                arcs.add(new Lightning.Arc(new PointF(s.x + s.width / 2, s.y), new PointF(s.x + s.width / 2, s.y + s.height)));
                s.parent.add(new Lightning(arcs, null));
                Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
            }
        }

        return damage;
    }

    private static ItemSprite.Glowing WHITE = new ItemSprite.Glowing( 0x5a86b3, 0.4f );

    @Override
    public ItemSprite.Glowing glowing() {
        return WHITE;
    }
}
