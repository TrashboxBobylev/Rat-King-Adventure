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

import static com.zrp200.rkpd2.Dungeon.hero;

import com.watabou.noosa.TextureFilm;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.mobs.npcs.AbstractMirrorImage;
import com.watabou.utils.PointF;

public class MirrorSprite extends MobSprite {

	public MirrorSprite() {
		super();
		
		texture( hero != null ? hero.heroClass.spritesheet() : HeroClass.WARRIOR.spritesheet() );
		updateArmor( 0 );
		idle();
	}
	
	@Override
	public void link( Char ch ) {
		super.link( ch );
		updateArmor();
	}

	@Override
	public void bloodBurstA(PointF from, int damage) {
		//do nothing
	}

	public void updateArmor(){
		updateArmor( ((AbstractMirrorImage)ch).armTier );
	}
	
	public void updateArmor( int tier ) {
		if (hero != null) {
			CharSprite ref = hero.sprite;

			idle = ref.idle.clone();

			run = ref.run.clone();
			if (hero.heroClass == HeroClass.RAT_KING)
				run.delay = 0.1f; // this is the actual delay, and on another mob it doesn't make sense to not do this.

			attack = ref.attack.clone();

			// a hack to get the first frame, which should be the first idle frame.
			die = ref.idle.clone();
			die.frames(ref.idle.frames[0]);
			die.delay = ref.die.delay;
			die.looped = ref.die.looped;
		} else {
			// yaaay duplication
			TextureFilm film = new TextureFilm( HeroSprite.defaultTiers(), tier, HeroSprite.FRAME_WIDTH, HeroSprite.FRAME_HEIGHT );

			idle = new Animation( 1, true );
			idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );

			run = new Animation( 20, true );
			run.frames( film, 2, 3, 4, 5, 6, 7 );

			die = new Animation( 20, false );
			die.frames( film, 0 );

			attack = new Animation( 15, false );
			attack.frames( film, 13, 14, 15, 0 );
		}
		idle();
	}
}
