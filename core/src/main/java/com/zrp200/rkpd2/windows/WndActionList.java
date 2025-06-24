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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Chrome;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.RenderedTextBlock;
import com.zrp200.rkpd2.ui.ScrollPane;
import com.zrp200.rkpd2.ui.StyledButton;
import com.zrp200.rkpd2.ui.Window;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.Visual;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndActionList extends Window {

	private static final int WIDTH_P = 105;
	private static final int WIDTH_L = 220;

	private static final int MARGIN  = 2;

	private ScrollPane modeList;
	private ArrayList<ActionButton> slots = new ArrayList<>();

	public WndActionList(){
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float pos = MARGIN;
		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.titleCase(Messages.get(this, "title")), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((width-title.width())/2, pos);
		title.maxWidth(width - MARGIN * 2);
		add(title);
		pos += MARGIN + title.height();
		RenderedTextBlock desc = PixelScene.renderTextBlock(Messages.capitalize(Messages.get(this, "desc")), 6);
		desc.setPos((width-desc.width())/2, pos);
		desc.maxWidth(width - MARGIN * 2);
		add(desc);

		modeList = new ScrollPane( new Component());
		add(modeList);

		Component content = modeList.content();
		int positem = 0;

		ArrayList<ActionIndicator.Action> possibleActions = new ArrayList<>();
		for (Class<? extends Buff> possibleAction : ActionIndicator.actionBuffClasses){
			for(Buff b : Dungeon.hero.buffs(possibleAction)){
				if (((ActionIndicator.Action)b).usable())
					possibleActions.add((ActionIndicator.Action) b);
			}
		}

		for (ActionIndicator.Action possibleAction : possibleActions) {
			ActionButton actionBtn = new ActionButton(Messages.titleCase(possibleAction.actionName()), 6, possibleAction);
			actionBtn.icon(new Image(Assets.Sprites.ITEMS, 0, 976, 16, 16));
			actionBtn.leftJustify = true;
			actionBtn.multiline = true;
			actionBtn.hardlight(possibleAction.indicatorColor());
			actionBtn.setSize(width, actionBtn.reqHeight());
			actionBtn.setRect(0, positem, width, actionBtn.reqHeight());
			actionBtn.enable(true);
			content.add(actionBtn);
			slots.add(actionBtn);
			positem += actionBtn.height() + MARGIN;
		}
		content.setSize(width, positem+1);
		resize(width, PixelScene.uiCamera.height-80);
		modeList.setRect(0, desc.bottom()+MARGIN, width, height - MARGIN*4.5f);
	}

	public class ActionButton extends StyledButton {

		ActionIndicator.Action action;
		Visual primaryVis;
		Visual secondVis;

		public ActionButton(String label, int size, ActionIndicator.Action action){
			super(Chrome.Type.GREY_BUTTON, label, size);
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;

			this.action = action;
			primaryVis = action.primaryVisual();
			secondVis = action.secondaryVisual();
			add(primaryVis);
			if (secondVis != null)
				add(secondVis);
		}

		@Override
		protected void onClick() {
			super.onClick();
			action.doAction();
			WndActionList.this.hide();
		}

		@Override
		protected boolean onLongClick() {
			ActionIndicator.setAction(action);
			WndActionList.this.hide();
			return true;
		}

		public void hardlight(int color){
			bg.hardlight(color);
		}

		@Override
		public void update() {
			super.update();

			if (primaryVis != null){
				primaryVis.x = x + (20 - primaryVis.width()) / 2f + 1;
				primaryVis.y = y + (20 - primaryVis.height()) / 2f;
				PixelScene.align(primaryVis);
				if (secondVis != null){
					if (secondVis.width() > 16) secondVis.x = primaryVis.center().x - secondVis.width()/2f;
					else                        secondVis.x = primaryVis.center().x + 8 - secondVis.width();
					if (secondVis instanceof BitmapText){
						//need a special case here for text unfortunately
						secondVis.y = primaryVis.center().y + 8 - ((BitmapText) secondVis).baseLine();
					} else {
						secondVis.y = primaryVis.center().y + 8 - secondVis.height();
					}
					PixelScene.align(secondVis);
				}
			}
		}
	}
}
