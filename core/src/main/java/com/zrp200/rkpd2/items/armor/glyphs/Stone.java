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

package com.zrp200.rkpd2.items.armor.glyphs;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.watabou.utils.GameMath;

public class Stone extends Armor.Glyph {

	private static ItemSprite.Glowing GREY = new ItemSprite.Glowing( 0x222222 );

	{
		beneficial = false;
	}

	@Override
	public int proc(Armor armor, Char attacker, Char defender, int damage) {
		
		testing = true;
		float evasion = defender.defenseSkill(attacker);
		float accuracy = attacker.attackSkill(defender);
		testing = false;

		evasion *= genericProcChanceMultiplier(defender);
		
		float hitChance;
		if (evasion >= accuracy){
			hitChance = (accuracy/evasion)/2f;
		} else {
			hitChance = 1f - (evasion/accuracy)/2f;
		}
		
		//75% of dodge chance is applied as damage reduction
		// we clamp in case accuracy or evasion were negative
		hitChance = GameMath.gate(0.25f, (1f + 3f*hitChance)/4f, 1f);
		
		damage = (int)Math.ceil(damage * hitChance);
		
		return damage;
	}
	
	private static boolean testing = false;
	
	public static boolean testingEvasion(){
		return testing;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return GREY;
	}

}
