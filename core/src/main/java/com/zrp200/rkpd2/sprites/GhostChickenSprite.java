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
import com.watabou.noosa.TextureFilm;

public class GhostChickenSprite extends MobSprite {

	public GhostChickenSprite() {
		super();
		
		texture( Assets.Sprites.CHICKEN );
		
		TextureFilm frames = new TextureFilm( texture, 15, 15 );
		
		idle = new Animation( 20, true );
		idle.frames( frames, 0, 1 );
		
		run = new Animation( 32, true );
		run.frames( frames, 0, 1 );
		
		attack = new Animation( 26, false );
		attack.frames( frames, 2, 3, 2, 1 );
		
		die = new Animation( 20, false );
		die.frames( frames, 4, 5, 6 );
		
		play( idle );

		alpha(0.3f);
	}
}
