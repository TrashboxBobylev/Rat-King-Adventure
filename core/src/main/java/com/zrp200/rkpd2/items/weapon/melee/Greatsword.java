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

package com.zrp200.rkpd2.items.weapon.melee;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.items.weapon.enchantments.Kinetic;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class Greatsword extends Sword {

	{
		image = ItemSpriteSheet.GREATSWORD;

		tier = 5;
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		int conservedDamage = 0;
		if (Dungeon.hero.buff(Kinetic.ConservedDamage.class) != null) {
			conservedDamage = Dungeon.hero.buff(Kinetic.ConservedDamage.class).damageBonus();
			Dungeon.hero.buff(Kinetic.ConservedDamage.class).detach();
		}

		if (damage > enemy.HP){
			int extraDamage = (damage - enemy.HP)*2;

			Buff.affect(Dungeon.hero, Kinetic.ConservedDamage.class).setBonus(extraDamage);
		}

		return damage + conservedDamage;
	}

	@Override
	public float warriorMod() {
		return 1f;
	}
	//+(7+lvl) damage, roughly +40% base dmg, +30% scaling
}
