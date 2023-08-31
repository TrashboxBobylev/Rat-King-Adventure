/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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

package com.zrp200.rkpd2.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.Arrays;

public class ExoParticle extends PixelParticle {

	public static final Factory FACTORY = new Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((ExoParticle)emitter.recycle( ExoParticle.class )).reset( x, y );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};

	public ExoParticle() {
		super();
		
		lifespan = 1f;
		color( Random.element(Arrays.asList(
				0xc73e3e, 0xf27049, 0xf27049, 0x37916a,
				0x392e73, 0x69f0dc, 0xa6f069, 0xe096a5,
				0x4965ba, 0x392e73, 0x872b56, 0x193e42,
				0x446140, 0xfffff0, 0xbac3d6)) );

		speed.polar( Random.Float( PointF.PI2*4 ), Random.Float( 34, 64 ) );
	}
	
	public void reset( float x, float y ) {
		revive();

		left = lifespan;

		size = 10;
		
		this.x = x - speed.x * lifespan;
		this.y = y - speed.y * lifespan;
		angularSpeed = Random.Float( 180 );
	}
	
	@Override
	public void update() {
		super.update();
		
		float p = left / lifespan;
		am = p < 0.5f ? p * p * 4 : (1 - p) * 2;
		size( Random.Float( 6 * (left / lifespan) ) );
	}
}