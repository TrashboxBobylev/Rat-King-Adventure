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

package com.zrp200.rkpd2.items.weapon.enchantments;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Unstable extends Weapon.Enchantment {

	private static ItemSprite.Glowing GREY = new ItemSprite.Glowing( 0x999999 );

	private static Class<?extends Weapon.Enchantment>[] randomEnchants = new Class[]{
			Blazing.class,
			Blocking.class,
			Blooming.class,
			Chilling.class,
			Kinetic.class,
			Corrupting.class,
			Elastic.class,
			Grim.class,
			Lucky.class,
			//projecting not included, no on-hit effect
			Shocking.class,
			Vampiric.class,
			Explosive.class
	};

	public static Weapon.Enchantment getRandomEnchant(Weapon weapon) {
		Class<?extends Weapon.Enchantment> cls = Random.oneOf(randomEnchants);
		return cls != Explosive.class || weapon instanceof SpiritBow
				? Reflection.newInstance(cls) : getRandomEnchant(weapon);
	}
	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		
		int conservedDamage = 0;
		if (attacker.buff(Kinetic.ConservedDamage.class) != null) {
			conservedDamage = attacker.buff(Kinetic.ConservedDamage.class).damageBonus();
			attacker.buff(Kinetic.ConservedDamage.class).detach();
		}

		return getRandomEnchant(weapon).proc( weapon, attacker, defender, damage ) + conservedDamage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return GREY;
	}
}
