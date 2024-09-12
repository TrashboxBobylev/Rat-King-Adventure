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
import com.zrp200.rkpd2.actors.mobs.Dragon;
import com.zrp200.rkpd2.effects.MagicMissile;

public class DragonSprite extends MobSprite {

	private Emitter particles;

	public DragonSprite() {
		super();
		
		texture( Assets.Sprites.DRAGON );
		
		TextureFilm frames = new TextureFilm( texture, 16, 16 );
		
		idle = new Animation( 10, true );
		idle.frames( frames, 0, 1 );
		
		run = new Animation( 20, true );
		run.frames( frames, 0, 2, 3 );
		
		attack = new Animation( 12, false );
		attack.frames( frames, 4, 5, 6, 7, 0);
		
		zap = attack.clone();
		
		die = new Animation( 15, false );
		die.frames( frames,  8, 9, 10, 11 );
		
		play( idle );
	}
	
	public void zap( int cell ) {
		
		turnTo( ch.pos , cell );
		play( zap );

		MagicMissile.boltFromChar( parent,
				MagicMissile.ABYSS,
				this,
				cell,
				new Callback() {
					@Override
					public void call() {
						((Dragon)ch).onZapComplete();
					}
				} );
		Sample.INSTANCE.play( Assets.Sounds.ZAP );
	}



	@Override
	public void link( Char ch ) {
		super.link( ch );

		if (particles == null) {
			particles = createEmitter();
		}
	}

	@Override
	public void update() {
		super.update();

		if (particles != null){
			particles.visible = visible;
		}
	}

	@Override
	public void kill() {
		super.kill();
		if (particles != null){
			particles.killAndErase();
		}
	}
	
	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();
		}
		super.onComplete( anim );
	}

	protected Emitter createEmitter() {
		Emitter emitter = emitter();
		emitter.pour( MagicMissile.ForceParticle.FACTORY, 0.04f );
		return emitter;
	}

	@Override
	public int blood() {
		return 0xFFffffff;
	}

	@Override
	public void die() {
		emitter().burst( MagicMissile.ForceParticle.FACTORY, 12 );
		if (particles != null){
			particles.on = false;
		}
		super.die();
	}
}
