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

package com.zrp200.rkpd2.actors.mobs;

import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.items.food.MysteryMeat;
import com.zrp200.rkpd2.items.food.Pasty;
import com.zrp200.rkpd2.sprites.SeniorSprite;
import com.watabou.utils.Random;

public class Senior extends Monk {

	{
		spriteClass = SeniorSprite.class;

		if (Dungeon.isChallenged(Challenges.NO_VEGAN))
			loot = new MysteryMeat();
		else
			loot = new Pasty();
		lootChance = 1f;
	}
	
	@Override
	public void move( int step, boolean travelling) {
		// on top of the existing move bonus, senior monks get a further 1.66 cooldown reduction
		// for a total of 3.33, double the normal 1.67 for regular monks
		if (travelling) focusCooldown -= 1.66f;
		super.move( step, travelling);
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 16, 25 );
	}
	
}
