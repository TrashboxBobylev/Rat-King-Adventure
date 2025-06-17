/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
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
import com.zrp200.rkpd2.actors.buffs.RobotTransform;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class EchSprite extends MobSprite {

	private int cellToAttack;

	public EchSprite() {
		super();
		
		texture( Assets.Sprites.ECH );
		
		TextureFilm frames = new TextureFilm( texture, 12, 14 );
		
		idle = new Animation( 6, true );
		idle.frames( frames, 0, 0, 0, 0, 0, 0, 1 );
		
		run = new Animation( 10, true );
		run.frames( frames, 0, 1 );
		
		attack = new Animation( 16, false );
		attack.frames( frames, 2, 3, 4, 5, 3, 2, 0 );

		zap = attack.clone();
		
		die = new Animation( 10, false );
		die.frames( frames, 0 );
		
		play( idle );
	}

	@Override
	public void attack( int cell ) {
		if (!Dungeon.level.adjacent( cell, ch.pos )) {

			cellToAttack = cell;
			turnTo( ch.pos , cell );
			play( zap );
			Sample.INSTANCE.play(Assets.Sounds.ZAP);

		} else {

			super.attack( cell );

		}
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();

			parent.recycle( MissileSprite.class ).
					reset( this, cellToAttack, new RobotTransform.RunicMissile(), new Callback() {
						@Override
						public void call() {
							ch.onAttackComplete();
						}
					} );
		} else {
			super.onComplete( anim );
		}
	}
}
