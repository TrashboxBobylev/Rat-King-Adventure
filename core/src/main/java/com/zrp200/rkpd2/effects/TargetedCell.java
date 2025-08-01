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

package com.zrp200.rkpd2.effects;

import com.zrp200.rkpd2.tiles.DungeonTilemap;
import com.zrp200.rkpd2.ui.Icons;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

public class TargetedCell extends Image {

	protected float alpha;

	public TargetedCell( int pos, int color ) {
		super(Icons.get(Icons.TARGET));
		hardlight(color);

		origin.set( width/2f );

		point( DungeonTilemap.tileToWorld( pos ) );

		alpha = 1f;
	}

	@Override
	public void update() {
		if ((alpha -= Game.elapsed/2f) > 0) {
			alpha( Math.min(1f, alpha) );
			scale.set( Math.min(1f, alpha) );
		} else {
			killAndErase();
		}
	}

	public static class SixthSense extends TargetedCell {
		public SixthSense(int pos, int color) {
			super(pos, color);
			alpha = 1.6f;
		}
	}
}
