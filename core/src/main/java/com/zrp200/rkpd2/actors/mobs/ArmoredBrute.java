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

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.AscensionChallenge;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.PlateArmor;
import com.zrp200.rkpd2.items.armor.ScaleArmor;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ShieldedSprite;
import com.watabou.utils.Random;

public class ArmoredBrute extends Brute {

	{
		spriteClass = ShieldedSprite.class;
		
		//see rollToDropLoot
		loot = Generator.Category.ARMOR;
		lootChance = 1f;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + 4; //4-12 DR total
	}
	
	@Override
	protected void triggerEnrage () {
		Buff.affect(this, ArmoredRage.class).setShield(HT/2 + 1);
		if (Dungeon.level.heroFOV[pos]) {
			sprite.showStatus( CharSprite.WARNING, Messages.get(this, "enraged") );
		}
		spend( TICK );
		hasRaged = true;
	}
	
	@Override
	public Item createLoot() {
		if (Random.Int( 4 ) == 0) {
			return new PlateArmor().random();
		}
		return new ScaleArmor().random();
	}
	
	//similar to regular brute rate, but deteriorates much slower. 60 turns to death total.
	public static class ArmoredRage extends Brute.BruteRage {
		
		@Override
		public boolean act() {
			
			if (target.HP > 0){
				detach();
				return true;
			}
			
			absorbDamage( Math.round(AscensionChallenge.statModifier(target)) );
			
			if (shielding() <= 0){
				target.die(null);
			}
			
			spend( 3*TICK );
			
			return true;
		}
		
	}
}
