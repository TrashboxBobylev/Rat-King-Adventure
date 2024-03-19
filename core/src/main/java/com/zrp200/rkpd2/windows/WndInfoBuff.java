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

package com.zrp200.rkpd2.windows;

import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.ui.BuffIcon;
import com.zrp200.rkpd2.ui.RenderedTextBlock;
import com.zrp200.rkpd2.ui.Window;
import com.watabou.noosa.Image;

public class WndInfoBuff extends Window {

	private static final float GAP	= 2;

	private static final int WIDTH = 120;

	public WndInfoBuff(Buff buff){
		super();

		IconTitle titlebar = new IconTitle();

		Image buffIcon = new BuffIcon( buff, true );

		titlebar.icon( buffIcon );
		titlebar.label( Messages.titleCase(buff.name()), Window.TITLE_COLOR );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		RenderedTextBlock txtInfo = PixelScene.renderTextBlock(buff.desc(), 6);
		txtInfo.maxWidth(WIDTH);
		txtInfo.setPos(titlebar.left(), titlebar.bottom() + 2*GAP);
		add( txtInfo );

		resize( WIDTH, (int)txtInfo.bottom() + 2 );
	}
}
