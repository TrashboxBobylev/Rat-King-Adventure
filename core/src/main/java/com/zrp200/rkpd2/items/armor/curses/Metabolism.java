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

package com.zrp200.rkpd2.items.armor.curses;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Hunger;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.items.armor.Armor.Glyph;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

public class Metabolism extends Glyph {

	private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );
	
	@Override
	public int proc( Armor armor, Char attacker, Char defender, int damage) {
		float procChance = 1/6f * procChanceMultiplier(defender);
		if ( Random.Float() < procChance && defender instanceof Hero) {

			//assumes using up 10% of starving, and healing of 1 hp per 10 turns;
			int healing = Math.min((int)Hunger.STARVING/100, defender.HT - defender.HP);

			if (healing > 0) {
				
				Hunger hunger = Buff.affect(defender, Hunger.class);
				
				if (!hunger.isStarving()) {
					
					hunger.affectHunger( healing * -10 );
					
					defender.HP += healing;
					defender.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( healing ), FloatingText.HEALING);
				}
			}

		}
		
		return damage;
	}

	@Override
	public Glowing glowing() {
		return BLACK;
	}

	@Override
	public boolean curse() {
		return true;
	}
}
