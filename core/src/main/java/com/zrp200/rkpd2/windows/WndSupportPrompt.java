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

import com.watabou.noosa.Image;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.SPDSettings;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.scenes.SupporterScene;
import com.zrp200.rkpd2.ui.Icons;
import com.zrp200.rkpd2.ui.RedButton;
import com.zrp200.rkpd2.ui.RenderedTextBlock;
import com.zrp200.rkpd2.ui.Window;

public class WndSupportPrompt extends Window {

	protected static final int WIDTH_P    = 120;
	protected static final int WIDTH_L    = 200;

	public WndSupportPrompt(){

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		IconTitle title = new IconTitle(new Image(Assets.Interfaces.ZRP200), Messages.get(WndSupportPrompt.class, "title"));
		title.setRect( 0, 0, width, 0 );
		add(title);

		String message = Messages.get(WndSupportPrompt.class, "intro");
		message += "\n\n" + Messages.get(SupporterScene.class, "support_msg");

		RenderedTextBlock text = PixelScene.renderTextBlock( 6 );
		text.text( message, width );
		text.setPos( title.left(), title.bottom() + 4 );
		add( text );

		RedButton link = new RedButton(Messages.get(SupporterScene.class, "supporter_link")){
			@Override
			protected void onClick() {
				super.onClick();
				String link = "https://www.buymeacoffee.com/zrp200";
				//tracking codes, so that the website knows where this pageview came from
				link += "?utm_source=rkpd2";
				link += "&utm_medium=supporter_prompt";
				link += "&utm_campaign=ingame_link";
				ShatteredPixelDungeon.platform.openURI(link);
				SPDSettings.supportNagged(true);
				WndSupportPrompt.super.hide();
			}
		};
		link.setRect(0, text.bottom() + 4, width, 18);
		add(link);

		RedButton close = new RedButton(Messages.get(this, "close")){
			@Override
			protected void onClick() {
				super.onClick();
				SPDSettings.supportNagged(true);
				WndSupportPrompt.super.hide();
			}
		};
		close.setRect(0, link.bottom() + 2, width, 18);
		add(close);

		resize(width, (int)close.bottom());

	}

	@Override
	public void hide() {
		//do nothing, have to close via the close button
	}
}
