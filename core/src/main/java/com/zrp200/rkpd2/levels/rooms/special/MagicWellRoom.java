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

package com.zrp200.rkpd2.levels.rooms.special;

import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.actors.blobs.WaterOfAwareness;
import com.zrp200.rkpd2.actors.blobs.WaterOfHealth;
import com.zrp200.rkpd2.actors.blobs.WellWater;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.painters.Painter;

public class MagicWellRoom extends SpecialRoom {

	private static final Class<?>[] WATERS =
		{WaterOfAwareness.class, WaterOfHealth.class};
	
	public Class<?extends WellWater> overrideWater = null;
	
	public void paint( Level level ) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );
		
		Point c = center();
		Painter.set( level, c.x, c.y, Terrain.WELL );
		
		@SuppressWarnings("unchecked")
		Class<? extends WellWater> waterClass =
			overrideWater != null ?
			overrideWater :
			(Class<? extends WellWater>)Random.element( WATERS );
			
		
		WellWater.seed(c.x + level.width() * c.y, 1, waterClass, level);
		
		entrance().set( Door.Type.REGULAR );
	}
}
