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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.items.armor.glyphs.Viscosity;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class WarHammer extends Mace {

	{
		image = ItemSpriteSheet.WAR_HAMMER;
		hitSound = Assets.Sounds.HIT_CRUSH;

		tier = 5;
		ACC = 1.20f; //20% boost to accuracy

		//24 base, down from 30
		//scaling unchanged

		//+(6+1.5*lvl) damage, roughly +40% base dmg, +45% scaling
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		Buff.affect(enemy, Viscosity.DeferedDamage.class).postpone(damage*2);
		Buff.affect(enemy, Paralysis.class, 3.5f);
		return super.warriorAttack(damage, enemy);
	}
}
