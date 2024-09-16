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

package com.zrp200.rkpd2.items.rings;


import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class RingOfMight extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_MIGHT;
	}

	@Override
	public boolean doEquip(Hero hero) {
		if (super.doEquip(hero)){
			hero.updateHT( false );
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){
			hero.updateHT( false );
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Item upgrade() {
		super.upgrade();
		updateTargetHT();
		return this;
	}

	@Override
	public void level(int value) {
		super.level(value);
		updateTargetHT();
	}
	
	private void updateTargetHT(){
		if (buff != null && buff.target instanceof Hero){
			((Hero) buff.target).updateHT( false );
		}
	}

	@Override
	protected float cap() {
		return Integer.MAX_VALUE;
	}

	public String statsInfo() {
		if (isIdentified()){
			String info = Messages.get(this, "stats",
					soloBonus(), Messages.decimalFormat("#.##", 100f * (Math.pow(1.07, soloBuffedBonus()) - 1f)));
			if (isEquipped(Dungeon.hero) && soloBuffedBonus() != combinedBuffedBonus(Dungeon.hero)){
				info += "\n\n" + Messages.get(this, "combined_stats",
						getBonus(Dungeon.hero, Might.class), Messages.decimalFormat("#.##", 100f * Math.pow(1.07, combinedBuffedBonus(Dungeon.hero)) - 1f));
			}
			return info;
		} else {
			return Messages.get(this, "typical_stats", 1, Messages.decimalFormat("#.##", 3.5f));
		}
	}

	@Override
	protected RingBuff buff( ) {
		return new Might();
	}
	
	public static int strengthBonus( Char target ){
		return getBonus( target, Might.class );
	}
	
	public static float HTMultiplier( Char target ){
		return (float)Math.pow(1.07, getBuffedBonus(target, Might.class));
	}

	public class Might extends RingBuff {
	}
}

