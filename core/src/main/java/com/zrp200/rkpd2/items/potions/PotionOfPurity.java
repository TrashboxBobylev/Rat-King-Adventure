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

package com.zrp200.rkpd2.items.potions;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.buffs.BlobImmunity;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class PotionOfPurity extends Potion {
	
	private static final int DISTANCE	= 3;
	
	private static ArrayList<Class> affectedBlobs;

	{
		icon = ItemSpriteSheet.Icons.POTION_PURITY;
		
		affectedBlobs = new ArrayList<>(new BlobImmunity().immunities());
	}

	@Override
	public void shatter( int cell ) {
		
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), DISTANCE );
		
		ArrayList<Blob> blobs = new ArrayList<>();
		for (Class c : affectedBlobs){
			Blob b = Dungeon.level.blobs.get(c);
			if (b != null && b.volume > 0){
				blobs.add(b);
			}
		}
		
		for (int i=0; i < Dungeon.level.length(); i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				
				for (Blob blob : blobs) {
					blob.clear(i);
				}
				
				if (Dungeon.level.heroFOV[i]) {
					CellEmitter.get( i ).burst( Speck.factory( Speck.DISCOVER ), 2 );
				}
				
			}
		}


		splash( cell );
		if (Dungeon.level.heroFOV[cell]) {
			Sample.INSTANCE.play(Assets.Sounds.SHATTER);

			identify();
			GLog.i(Messages.get(this, "freshness"));
		}
		
	}
	
	@Override
	public void apply( Hero hero ) {
		GLog.w( Messages.get(this, "protected") );
		Buff.prolong( hero, BlobImmunity.class, BlobImmunity.DURATION );
		SpellSprite.show(hero, SpellSprite.PURITY);
		identify();
	}
	
	@Override
	public int value() {
		return isKnown() ? 40 * quantity : super.value();
	}
}
