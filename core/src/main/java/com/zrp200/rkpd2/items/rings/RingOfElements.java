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

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Electricity;
import com.zrp200.rkpd2.actors.blobs.ToxicGas;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.Chill;
import com.zrp200.rkpd2.actors.buffs.Corrosion;
import com.zrp200.rkpd2.actors.buffs.Frost;
import com.zrp200.rkpd2.actors.buffs.FrostBurn;
import com.zrp200.rkpd2.actors.buffs.Ooze;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.actors.buffs.Poison;
import com.zrp200.rkpd2.items.armor.glyphs.AntiMagic;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

import java.util.HashSet;

public class RingOfElements extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_ELEMENTS;
		buffClass = Resistance.class;
	}

	@Override
	protected float multiplier() {
		return 0.825f;
	}

	public String upgradeStat1(int level){
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return formatBonus(level + 1) + "%";
	}

	@Override
	protected float cap() {
		return 0.66f;
	}

	@Override
	protected RingBuff buff( ) {
		return new Resistance();
	}

	public static final HashSet<Class> RESISTS = new HashSet<>();
	static {
		RESISTS.add( Burning.class );
		RESISTS.add( Chill.class );
		RESISTS.add( Frost.class );
		RESISTS.add( Ooze.class );
		RESISTS.add( Paralysis.class );
		RESISTS.add( Poison.class );
		RESISTS.add( Corrosion.class );
		RESISTS.add( FrostBurn.class);

		RESISTS.add( ToxicGas.class );
		RESISTS.add( Electricity.class );

		RESISTS.addAll( AntiMagic.RESISTS );
	}
	
	public static float resist( Char target, Class effect ){
		if (getBuffedBonus(target, Resistance.class) == 0) return 1f;
		
		for (Class c : RESISTS){
			if (c.isAssignableFrom(effect)){
				return Math.max(0.33f, (float)Math.pow(0.825, getBuffedBonus(target, Resistance.class)));
			}
		}
		
		return 1f;
	}
	
	public class Resistance extends RingBuff {
	
	}
}
