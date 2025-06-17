/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 *  Shattered Pixel Dungeon
 *  Copyright (C) 2014-2022 Evan Debenham
 *
 * Summoning Pixel Dungeon
 * Copyright (C) 2019-2022 TrashboxBobylev
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
import com.zrp200.rkpd2.scenes.GameScene;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

public class BlinkingManSprite extends MobSprite {

	public BlinkingManSprite() {
		super();
		
		texture( Assets.Sprites.TENGU );
		
		TextureFilm frames = new TextureFilm( texture, 14, 16 );
		
		idle = new Animation( 10, true );
		idle.frames( frames, 0, 0, 0, 1 );
		
		run = new Animation( 30, false );
		run.frames( frames, 2, 3, 4, 5, 0 );
		
		attack = new Animation( 30, false );
		attack.frames( frames, 6, 7, 7, 0 );
		
		zap = attack.clone();
		
		die = new Animation( 12, false );
		die.frames( frames, 8, 9, 10, 10, 10, 10, 10, 10 );

		hardlight(0x8f8f8f);
		
		play( run.clone() );
	}
	
	@Override
	public void idle() {
		isMoving = false;
		super.idle();
	}
	
	@Override
	public void move( int from, int to ) {
		
		place( to );
		
		play( run );
		turnTo( from , to );

		isMoving = true;

		if (Dungeon.level.water[to]) {
			GameScene.ripple( to );
		}

	}
	
	@Override
	public void attack( int cell ) {
		if (!Dungeon.level.adjacent( cell, ch.pos )) {

			((MissileSprite)parent.recycle( MissileSprite.class )).
				reset( this, cell, new TenguJavelin(), new Callback() {
					@Override
					public void call() {
						ch.onAttackComplete();
					}
				} );
			
			play( zap );
			turnTo( ch.pos , cell );
			
		} else {
			
			super.attack( cell );
			
		}
	}
	
	@Override
	public void onComplete( Animation anim ) {
		if (anim == run) {
			synchronized (this){
				isMoving = false;
				idle();

				notifyAll();
			}
		} else {
			super.onComplete( anim );
		}
	}
	
	public static class TenguJavelin extends Item {
		{
			image = ItemSpriteSheet.SPIRIT_ARROW;
		}
	}
}
