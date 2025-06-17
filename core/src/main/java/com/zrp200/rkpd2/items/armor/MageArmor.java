/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.zrp200.rkpd2.items.armor;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.mage.ElementalBlast;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.effects.particles.ElmoParticle;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import static com.zrp200.rkpd2.sprites.ItemSpriteSheet.ARMOR_MAGE;

public class MageArmor extends ClassArmor {
	{
		image = ARMOR_MAGE;
	}

	public static boolean doMoltenEarth() {
		boolean success = false;

		if (Dungeon.hero.hasTalent(Talent.AVALON_POWER_UP))
			ElementalBlast.activate(Dungeon.hero, () -> {});

		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (Dungeon.level.heroFOV[mob.pos]
					&& mob.alignment != Char.Alignment.ALLY && Dungeon.level.distance(Dungeon.hero.pos, mob.pos) <= 6 + Dungeon.hero.pointsInTalent(Talent.QUANTUM_POSITION)*3) {
				success = true;
				if (Dungeon.hero.canHaveTalent(Talent.AURIC_TESLA) &&
						Random.Int(4) < (Dungeon.hero.pointsInTalent(Talent.AURIC_TESLA) - 1)){
					SpiritBow bow = Dungeon.hero.belongings.getItem(SpiritBow.class);
					MagesStaff staff = Dungeon.hero.belongings.getItem(MagesStaff.class);
					if (staff == null && Dungeon.hero.belongings.weapon instanceof MagesStaff){
						staff = (MagesStaff) Dungeon.hero.belongings.weapon;
					}
					if (staff != null) staff.wand().onHit(staff, Dungeon.hero, mob, staff.damageRoll(Dungeon.hero));
					if (bow != null) bow.proc( Dungeon.hero, mob, bow.damageRoll(Dungeon.hero) );
				}
			}
		}
		return success;
	}
	public static void playMoltenEarthFX() {
		curUser.busy();

		curUser.sprite.emitter().start( ElmoParticle.FACTORY, 0.025f, 20 );
		Sample.INSTANCE.play( Assets.Sounds.BURNING );
		Sample.INSTANCE.play( Assets.Sounds.BURNING );
		Sample.INSTANCE.play( Assets.Sounds.BURNING );
	}
}