/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.RKChampionBuff;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.HeroSprite;
import com.zrp200.rkpd2.ui.RedButton;
import com.zrp200.rkpd2.ui.RenderedTextBlock;
import com.zrp200.rkpd2.ui.Window;

public class WndRkChampion extends Window {

	private static final int WIDTH_P = 145;
	private static final int WIDTH_L = 220;

	private static final int MARGIN  = 2;

	public WndRkChampion(RKChampionBuff handler){
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float pos = MARGIN;
		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.titleCase(Messages.get(this, "title")), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((width-title.width())/2, pos);
		title.maxWidth(width - MARGIN * 2);
		add(title);

		pos = title.bottom() + 3*MARGIN;

		Image rat0 = HeroSprite.avatar(Dungeon.hero.heroClass, 6);

		RedButton moveBtn0 = new RedButton(Messages.get(WndRkChampion.class, "lose_title"), 6){
			@Override
			protected void onClick() {
				super.onClick();
				handler.useTitle(null);
				hide();
			}
		};
		rat0.hardlight(0xc4c4c4);
		moveBtn0.icon(rat0);
		moveBtn0.leftJustify = true;
		moveBtn0.multiline = true;
		moveBtn0.setSize(width, moveBtn0.reqHeight());
		moveBtn0.setRect(0, pos, width, moveBtn0.reqHeight());
		moveBtn0.enable(true);
		add(moveBtn0);
		pos = moveBtn0.bottom() + MARGIN;

		for (Class<? extends ChampionEnemy> champTitle : ChampionEnemy.heroTitles) {
			Image rat = HeroSprite.avatar(Dungeon.hero.heroClass, 6);

			RedButton moveBtn = new RedButton(ChampionEnemy.getRKDesc(champTitle), 6){
				@Override
				protected void onClick() {
					super.onClick();
					handler.useTitle(champTitle);
					hide();
				}
			};
			rat.hardlight(ChampionEnemy.getTitleColor(champTitle));
			moveBtn.icon(rat);
			moveBtn.leftJustify = true;
			moveBtn.multiline = true;
			moveBtn.setSize(width, moveBtn.reqHeight());
			moveBtn.setRect(0, pos, width, moveBtn.reqHeight());
			moveBtn.enable(true);
			add(moveBtn);
			pos = moveBtn.bottom() + MARGIN;
		}

		resize(width, (int)pos);

	}


}
