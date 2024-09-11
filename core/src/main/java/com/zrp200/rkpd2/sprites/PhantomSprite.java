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
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.mobs.Phantom;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.effects.Speck;

public class PhantomSprite extends MobSprite {

	private Emitter cloud;

	public PhantomSprite() {
		super();

		texture( Assets.Sprites.DM200 );

		TextureFilm frames = new TextureFilm( texture, 21, 18 );

		idle = new Animation( 10, true );
		idle.frames( frames, 14);

		run = new Animation( 110, true );
		run.frames( frames, 14, 14);

		attack = new Animation( 15, false );
		attack.frames( frames, 14, 14, 14 );

		zap = new Animation( 15, false );
		zap.frames( frames, 14, 14, 14);

		die = new Animation( 8, false );
		die.frames( frames, 14, 14, 14);

		play( idle );
	}

	public void zap( int cell ) {

		turnTo( ch.pos , cell );
		play( zap );

		MagicMissile.boltFromChar( parent,
				MagicMissile.INVISI,
				this,
				cell,
				new Callback() {
					@Override
					public void call() {
						((Phantom)ch).onZapComplete();
					}
				} );
		Sample.INSTANCE.play( Assets.Sounds.ATK_CROSSBOW );
	}

	@Override
	public void place(int cell) {
		if (parent != null) parent.bringToFront(this);
		super.place(cell);
	}

	@Override
	public void die() {
		emitter().burst( Speck.factory( Speck.WOOL ), 8 );
		super.die();
		if (cloud != null) {
			cloud.on = false;
		}
	}

	@Override
	public void update() {

		super.update();

		if (cloud != null) {
			cloud.visible = visible;
		}
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();
		}
		super.onComplete( anim );
	}

	@Override
	public void link( Char ch ) {
		super.link( ch );

		if (cloud == null) {
			cloud = emitter();
			cloud.pour( Speck.factory( Speck.DUST ), 1.25f );
		}
	}

}
