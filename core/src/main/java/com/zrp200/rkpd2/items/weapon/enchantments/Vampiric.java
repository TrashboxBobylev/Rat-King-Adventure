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
import com.zrp200.rkpd2.actors.mobs.Mimic;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

public class Vampiric extends Weapon.Enchantment {

	private static ItemSprite.Glowing RED = new ItemSprite.Glowing( 0x660022 );
	
	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		
		//chance to heal scales from 5%-30% based on missing HP
		float missingPercent = (attacker.HT - attacker.HP) / (float)attacker.HT;
		float healChance = 0.05f + .25f*missingPercent;

		healChance *= procChanceMultiplier(attacker);
		
		if (Random.Float() < healChance
				&& attacker.alignment != defender.alignment
				&& (defender.alignment != Char.Alignment.NEUTRAL || defender instanceof Mimic)){

			float powerMulti = Math.max(1f, healChance);
			
			//heals for 50% of damage dealt
			int healAmt = Math.round(damage * 0.5f * powerMulti);
			healAmt = Math.min( healAmt, attacker.HT - attacker.HP );
			
			if (healAmt > 0 && attacker.isAlive()) {
				
				attacker.HP += healAmt;
				attacker.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( healAmt ), FloatingText.HEALING );
				
			}
		}

		return damage;
	}
	
	@Override
	public Glowing glowing() {
		return RED;
	}
}
