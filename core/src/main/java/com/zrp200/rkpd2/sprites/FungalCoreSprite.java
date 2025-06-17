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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.tiles.DungeonWallsTilemap;
import com.watabou.noosa.TextureFilm;

public class FungalCoreSprite extends MobSprite {

	public FungalCoreSprite(){
		super();

		texture( Assets.Sprites.FUNGAL_CORE );

		TextureFilm frames = new TextureFilm( texture, 27, 27 );

		idle = new Animation( 0, true );
		idle.frames( frames, 0);

		run = new Animation( 0, true );
		run.frames( frames, 0);

		attack = new Animation( 24, false );
		attack.frames( frames, 0 );

		zap = attack.clone();

		die = new Animation( 12, false );
		die.frames( frames, 0 );

		play( idle );

	}

	boolean wasVisible = false;

	@Override
	public void update() {
		super.update();
		if (curAnim != die && ch != null && visible != wasVisible){
			if (visible){
				DungeonWallsTilemap.skipCells.add(ch.pos - 2* Dungeon.level.width());
				DungeonWallsTilemap.skipCells.add(ch.pos - Dungeon.level.width());
			} else {
				DungeonWallsTilemap.skipCells.remove(ch.pos - 2*Dungeon.level.width());
				DungeonWallsTilemap.skipCells.remove(ch.pos - Dungeon.level.width());
			}
			GameScene.updateMap(ch.pos-2*Dungeon.level.width());
			GameScene.updateMap(ch.pos-Dungeon.level.width());
			wasVisible = visible;
		}
	}

	@Override
	public void die() {
		super.die();
		if (ch != null && visible){
			DungeonWallsTilemap.skipCells.remove(ch.pos - 2*Dungeon.level.width());
			DungeonWallsTilemap.skipCells.remove(ch.pos - Dungeon.level.width());
			GameScene.updateMap(ch.pos-2*Dungeon.level.width());
			GameScene.updateMap(ch.pos-Dungeon.level.width());
		}
	}

	@Override
	public void turnTo(int from, int to) {
		//do nothing
	}

	@Override
	public int blood() {
		return 0xFF88CC44;
	}

}
