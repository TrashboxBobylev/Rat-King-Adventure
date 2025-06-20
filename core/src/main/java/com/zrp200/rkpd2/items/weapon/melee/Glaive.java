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
import com.zrp200.rkpd2.items.stones.StoneOfAggression;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class Glaive extends Spear {

	{
		image = ItemSpriteSheet.GLAIVE;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.8f;

		tier = 5;
	}

	//40 base, up from 30
	//+8 per level, up from +6

	@Override
	public int abilityStat(int level) {
		//+(12+2.5*lvl) damage, roughly +55% base damage, +55% scaling
		return augment.damageFactor(12 + Math.round(2.5f*level));
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		Buff.affect(enemy, StoneOfAggression.Aggression.class, StoneOfAggression.Aggression.DURATION / 5);
		return super.warriorAttack(damage, enemy);
	}

}
