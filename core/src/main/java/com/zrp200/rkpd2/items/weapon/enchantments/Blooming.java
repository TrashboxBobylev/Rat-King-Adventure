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

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Regeneration;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.features.HighGrass;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Blooming extends Weapon.Enchantment {
	
	private static ItemSprite.Glowing DARK_GREEN = new ItemSprite.Glowing( 0x008800 );
	
	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
		int level = Math.max( 0, weapon.buffedLvl() );

		// lvl 0 - 33%
		// lvl 1 - 50%
		// lvl 2 - 60%
		float procChance = (level+1f)/(level+3f) * procChanceMultiplier(attacker);
		if (Random.Float() < procChance) {

			float powerMulti = Math.max(1f, procChance);

			float plants = (1f + 0.1f*level) * powerMulti;
			if (Random.Float() < plants%1){
				plants = (float)Math.ceil(plants);
			} else {
				plants = (float)Math.floor(plants);
			}

			if (plantGrass(defender.pos)){
				plants--;
				if (plants <= 0){
					return damage;
				}
			}
			
			ArrayList<Integer> positions = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8){
				if (defender.pos + i != attacker.pos) {
					positions.add(defender.pos + i);
				}
			}
			Random.shuffle( positions );

			//The attacker's position is always lowest priority
			if (Dungeon.level.adjacent(attacker.pos, defender.pos)){
				positions.add(attacker.pos);
			}

			for (int i : positions){
				if (plantGrass(i)){
					plants--;
					if (plants <= 0) {
						return damage;
					}
				}
			}
			
		}
		
		return damage;
	}

	private boolean plantGrass(int cell){
		if( HighGrass.plant(cell) ) {
            if (!Regeneration.regenOn()) {
                Level.set(cell, Terrain.FURROWED_GRASS);
            }
            HighGrass.playVFX(cell);
			return true;
		}
		return false;
	}
	
	@Override
	public ItemSprite.Glowing glowing() {
		return DARK_GREEN;
	}
}
