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

package com.zrp200.rkpd2.actors.mobs;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;

public class DarkestElfSprite extends MobSprite {

	private MovieClip.Animation charging;

	public DarkestElfSprite(){
		super();
		
		texture( Assets.Sprites.NECRO );
		TextureFilm film = new TextureFilm( texture, 16, 16 );
		
		idle = new MovieClip.Animation( 4, true );
		idle.frames( film, 0, 0, 0, 1, 0, 0, 0, 0, 1 );
		
		run = new MovieClip.Animation( 16, true );
		run.frames( film, 0, 0, 0, 2, 3, 4 );
		
		zap = new MovieClip.Animation( 20, false );
		zap.frames( film, 5, 6, 7, 8 );
		
		charging = new MovieClip.Animation( 10, true );
		charging.frames( film, 7, 8 );
		
		die = new MovieClip.Animation( 10, false );
		die.frames( film, 9, 10, 11, 12 );

		hardlight(0x8f8f8f);
		
		attack = zap.clone();
		
		idle();
	}
	
	public void charge(){
		play(charging);
	}

	@Override
	public void zap(int cell) {
		super.zap(cell);
		if (visible && ch instanceof DarkestElf && ((DarkestElf) ch).summoning){
			Sample.INSTANCE.play( Assets.Sounds.CHARGEUP, 3f, 0.8f );
		}
	}

	@Override
	public void onComplete(MovieClip.Animation anim) {
		super.onComplete(anim);
		if (anim == zap){
			if (ch instanceof DarkestElf){
				if (((DarkestElf) ch).summoning){
					charge();
				} else {
					((DarkestElf)ch).onZapComplete();
					idle();
				}
			} else {
				idle();
			}
		}
	}
}
