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

import com.watabou.noosa.TextureFilm;
import com.watabou.utils.PointF;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.items.weapon.melee.AluminumSword;

public class InWorldWeaponSprite extends MobSprite {

	ItemSprite lol;

	public InWorldWeaponSprite() {
		super();
		
		texture( Assets.Sprites.RAT );
		
		TextureFilm frames = new TextureFilm( texture, 16, 16 );
		
		idle = new Animation( 2, true );
		idle.frames( frames, 15, 15, 15, 15 );
		
		run = new Animation( 10, true );
		run.frames( frames, 15, 15, 15, 15, 15 );
		
		attack = new Animation( 15, false );
		attack.frames( frames, 15, 15, 15, 15, 15 );
		
		die = new Animation( 10, false );
		die.frames( frames, 15, 15, 15, 15 );
		
		play( idle );
	}

	@Override
	public void place(int cell) {
		if (parent != null) parent.bringToFront(this);
		super.place(cell);
	}

	@Override
	public void link(Char ch) {
		super.link(ch);

		if (lol == null){
			lol = new ItemSprite();
			lol.view( new AluminumSword());
			lol.scale = new PointF(3.25f, 3.25f);
			lol.originToCenter();
			lol.point(point());
			lol.angularSpeed = 960;
			parent.add(lol);
		}
	}

	@Override
	public void update() {
		super.update();

		if (lol != null){
			PointF from = center();
			from.x -= lol.width()/8;
			from.y -= lol.height()/8;
			lol.point(from);
			lol.alpha(0.66f);
			lol.visible = visible;
		}
	}

	@Override
	public void die() {
		super.die();
		if (lol != null){
			lol.killAndErase();
		}
	}

	@Override
	public void kill() {
		super.kill();
		if (lol != null){
			lol.killAndErase();
		}
	}
}
