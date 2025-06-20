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

package com.zrp200.rkpd2.sprites;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.items.Item;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

public class ScorpioSprite extends MobSprite {

	private int cellToAttack;
	public final ScorpioShot shot = new ScorpioShot();
	
	public ScorpioSprite() {
		super();
		
		texture( Assets.Sprites.SCORPIO );
		
		TextureFilm frames = new TextureFilm( texture, 17, 17 );
		
		idle = new Animation( 12, true );
		idle.frames( frames, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 2, 1, 2 );
		
		run = new Animation( 8, true );
		run.frames( frames, 5, 5, 6, 6 );
		
		attack = new Animation( 15, false );
		attack.frames( frames, 0, 3, 4 );
		
		zap = attack.clone();
		
		die = new Animation( 12, false );
		die.frames( frames, 0, 7, 8, 9, 10 );
		
		play( idle );
	}
	
	@Override
	public int blood() {
		return 0xFF44FF22;
	}
	
	@Override
	public void attack( int cell ) {
		if (!Dungeon.level.adjacent( cell, ch.pos )) {
			
			cellToAttack = cell;
			zap(cell);
			
		} else {
			
			super.attack( cell );
			
		}
	}
	
	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();
			shot.shoot(this, cellToAttack, ch::onAttackComplete);
		} else {
			super.onComplete( anim );
		}
	}
	
	public class ScorpioShot extends Item {
		{
			image = ItemSpriteSheet.FISHING_SPEAR;
		}

		public void shoot(CharSprite src, int cellToAttack, Callback callback) {
			parent.recycle(MissileSprite.class).
					reset(src, cellToAttack, this, callback);
		}
	}
}
