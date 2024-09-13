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

import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.mobs.SpectreRat;
import com.zrp200.rkpd2.effects.MagicMissile;

public class SpectreRatSprite extends MobSprite {

	public SpectreRatSprite() {
		super();
		
		texture( Assets.Sprites.RAT );

		TextureFilm frames = new TextureFilm( texture, 16, 15 );

		idle = new Animation( 4, true );
		idle.frames( frames, 62, 62, 62, 62, 61, 61, 60, 60, 60, 60, 61, 61, 61 );

		run = new Animation( 10, true );
		run.frames( frames, 59, 60, 61, 62, 61, 60, 59 );

		attack = new Animation( 50, false );
		attack.frames( frames, 50, 51, 52, 53, 48 );

		zap = attack.clone();
		zap.delay *= 2.25f;

		die = new Animation( 10, false );
		die.frames( frames, 59, 60, 61, 62 );

		hardlight(0x8f8f8f);

		play( idle );
	}
	
	public void zap( int cell ) {
		
		turnTo( ch.pos , cell );
		play( zap );

		MagicMissile.boltFromChar( parent,
				MagicMissile.SHADOW,
				this,
				cell,
				new Callback() {
					@Override
					public void call() {
						((SpectreRat)ch).onZapComplete();
					}
				} );
		Sample.INSTANCE.play( Assets.Sounds.ZAP );
	}
	
	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();
		}
		super.onComplete( anim );
	}
}
