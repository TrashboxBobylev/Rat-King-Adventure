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

package com.zrp200.rkpd2.items.weapon.missiles.darts;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.MagicalSleep;
import com.zrp200.rkpd2.items.potions.exotic.PotionOfCleansing;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class DreamDart extends TippedDart {
	
	{
		image = ItemSpriteSheet.CLEANSING_DART;
	}
	
	@Override
	public int proc(Char attacker, final Char defender, int damage) {

		if (attacker.alignment == defender.alignment && attacker != defender){
			PotionOfCleansing.cleanse(defender, PotionOfCleansing.Cleanse.DURATION*2f);
			return 0;
		}
		//need to delay this so damage from the dart doesn't break the sleep
		//when processing charged shot, only put enemies to sleep
		if (!processingChargedShot || attacker.alignment != defender.alignment) {
			new FlavourBuff() {
				{
					actPriority = VFX_PRIO;
				}

				public boolean act() {
					Buff.affect(defender, MagicalSleep.class);
					return super.act();
				}
			}.attachTo(defender);
		}

		return super.proc(attacker, defender, damage);
	}
}
