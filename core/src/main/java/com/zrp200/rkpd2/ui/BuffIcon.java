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

package com.zrp200.rkpd2.ui;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;

public class BuffIcon extends Image {

	private static TextureFilm smallFilm;
	private static final int SML_SIZE = 7;

	private static TextureFilm largeFilm;
	private static final int LRG_SIZE = 16;

	private final boolean large;

	public BuffIcon(Buff buff, boolean large){
		super( large ? Assets.Interfaces.BUFFS_LARGE : Assets.Interfaces.BUFFS_SMALL );
		this.large = large;
		refresh(buff);
	}

	public BuffIcon(int icon, boolean large){
		super( large ? Assets.Interfaces.BUFFS_LARGE : Assets.Interfaces.BUFFS_SMALL );
		this.large = large;
		refresh(icon);
	}

	public void refresh(Buff buff){
		refresh(buff.icon());
		buff.tintIcon(this);
	}

	public void refresh(int icon){
		if (large){
			if (largeFilm == null) largeFilm = new TextureFilm(texture, LRG_SIZE, LRG_SIZE);
			frame(largeFilm.get(actualIconID(icon, 16)));
		} else {
			if (smallFilm == null ) smallFilm = new TextureFilm(texture, SML_SIZE, SML_SIZE);
			frame(smallFilm.get(actualIconID(icon, 18)));
		}
	}

	public static int actualIconID(int icon, int filmWidth){
		if (icon > 99){
			return (icon / 100)-1 + (icon % 100) * filmWidth;
		} else {
			return icon;
		}
	}

}
