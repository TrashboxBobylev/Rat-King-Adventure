/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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

import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class Flail extends MeleeWeapon {

	{
		image = ItemSpriteSheet.FLAIL;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 0.8f;

		tier = 4;
		ACC = 0.8f; //0.8x accuracy
		//also cannot surprise attack, see Hero.canSurpriseAttack
	}

	@Override
	public int max(int lvl) {
		return  Math.round(7*(tier+1)) +        //35 base, up from 25
				lvl*Math.round(1.6f*(tier+1));  //+8 per level, up from +5
	}

	public static int slamDamageRoll(int level){
		return Random.NormalIntRange(
				Math.round(10 + 2.5f * level), //10 min, from 4
				Math.round(87.5f + 10 * level) //87.5 max, from 25
		);
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		int dmg = slamDamageRoll(buffedLvl());
		for (int i = 1; i < 4; i++) {
			int dmgReroll = slamDamageRoll(buffedLvl());
			if (dmgReroll > dmg) dmg = dmgReroll;
		}
		return dmg;
	}

	@Override
	public float warriorDelay() {
		return 4f;
	}
}
