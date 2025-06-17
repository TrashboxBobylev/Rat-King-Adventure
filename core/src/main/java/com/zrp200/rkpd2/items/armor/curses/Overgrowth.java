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
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.particles.LeafParticle;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.plants.Plant;
import com.zrp200.rkpd2.plants.Starflower;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Overgrowth extends Armor.Glyph {
	
	private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );

	{
		beneficial = false;
	}

	@Override
	public int proc(Armor armor, Char attacker, Char defender, int damage) {

		float procChance = 1/20f * procChanceMultiplier(defender);
		if ( Random.Float() < procChance ) {

			Plant.Seed s;
			do{
				s = (Plant.Seed) Generator.randomUsingDefaults(Generator.Category.SEED);
			} while (s instanceof Starflower.Seed);

			Plant p = s.couch(defender.pos, null);
			
			//momentarily revoke warden benefits, otherwise this curse would be incredibly powerful
			if (defender instanceof Hero && ((Hero) defender).subClass.is(HeroSubClass.WARDEN)) {
				HeroSubClass subclass = ((Hero)defender).subClass;
				((Hero) defender).subClass = HeroSubClass.NONE;
				p.activate( defender );
				((Hero) defender).subClass = subclass;
			} else {
				p.activate( defender );
			}
			
			
			CellEmitter.get( defender.pos ).burst( LeafParticle.LEVEL_SPECIFIC, 10 );
			
		}
		
		return damage;
	}
	
	@Override
	public ItemSprite.Glowing glowing() {
		return BLACK;
	}
	
	@Override
	public boolean curse() {
		return true;
	}
}
