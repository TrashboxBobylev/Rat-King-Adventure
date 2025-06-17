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

package com.zrp200.rkpd2.scenes;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Chrome;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.messages.Languages;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.Archs;
import com.zrp200.rkpd2.ui.ExitButton;
import com.zrp200.rkpd2.ui.Icons;
import com.zrp200.rkpd2.ui.RenderedTextBlock;
import com.zrp200.rkpd2.ui.ScrollPane;
import com.zrp200.rkpd2.ui.StyledButton;
import com.zrp200.rkpd2.ui.Window;
import com.zrp200.rkpd2.ui.changelist.ChangeInfo;
import com.zrp200.rkpd2.ui.changelist.RKPD2Changes;
import com.zrp200.rkpd2.ui.changelist.WndChanges;
import com.zrp200.rkpd2.ui.changelist.WndChangesTabbed;
import com.zrp200.rkpd2.ui.changelist.v1_X_Changes;
import com.zrp200.rkpd2.ui.changelist.v2_X_Changes;
import com.zrp200.rkpd2.windows.IconTitle;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.Scene;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class ChangesScene extends PixelScene {

	public static int changesSelected = 0;

	private NinePatch rightPanel;
	private ScrollPane rightScroll;
	private IconTitle changeTitle;
	private RenderedTextBlock changeBody;
final static int TOTAL_BUTTONS = 4;
	@Override
	public void create() {
		super.create();

		Music.INSTANCE.playTracks(
				new String[]{Assets.Music.THEME_1, Assets.Music.THEME_2},
				new float[]{1, 1},
				false);

		int w = Camera.main.width;
		int h = Camera.main.height;

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 9 );
		title.hardlight(Window.TITLE_COLOR);
		title.setPos(
				(w - title.width()) / 2f,
				(20 - title.height()) / 2f
		);
		align(title);
		add(title);

		ExitButton btnExit = new ExitButton();
		btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
		add( btnExit );

		NinePatch panel = Chrome.get(Chrome.Type.TOAST);

		int pw = 135 + panel.marginLeft() + panel.marginRight() - 2;
		int ph = h - 36;

		if (h >= PixelScene.MIN_HEIGHT_FULL && w >= PixelScene.MIN_WIDTH_FULL) {
			panel.size( pw, ph );
			panel.x = (w - pw) / 2f - pw/2 - 1;
			panel.y = title.bottom() + 5;

			rightPanel = Chrome.get(Chrome.Type.TOAST);
			rightPanel.size( pw, ph );
			rightPanel.x = (w - pw) / 2f + pw/2 + 1;
			rightPanel.y = title.bottom() + 5;
			add(rightPanel);
// todo why doesn't this work?!
//			rightScroll = new ScrollPane(new Component());
//			add(rightScroll);
//			rightScroll.setRect(
//					rightPanel.x + rightPanel.marginLeft(),
//					rightPanel.y + rightPanel.marginTop()-1,
//					rightPanel.innerWidth() + 2,
//					rightPanel.innerHeight() + 2);
//			rightScroll.scrollTo(0, 0);

			changeTitle = new IconTitle(Icons.get(Icons.CHANGES), Messages.get(this, "right_title"));
			changeTitle.setPos(/*0*/rightPanel.x + rightPanel.marginLeft(), /*1*/rightPanel.y + rightPanel.marginTop());
			changeTitle.setSize(pw, 20);
//			rightScroll.content().add(changeTitle);

			add(changeTitle);

			String body = Messages.get(this, "right_body");

			changeBody = PixelScene.renderTextBlock(body, 6);
			changeBody.maxWidth(pw - panel.marginHor());
			changeBody.setPos(/*0*/rightPanel.x + rightPanel.marginLeft(), changeTitle.bottom()+2);
			/*rightScroll.content().*/add(changeBody);

		} else {
			panel.size( pw, ph );
			panel.x = (w - pw) / 2f;
			panel.y = title.bottom() + 5;
		}
		align( panel );
		add( panel );
		
		final ArrayList<ChangeInfo> changeInfos = new ArrayList<>();

		if (Messages.lang() != Languages.ENGLISH){
			ChangeInfo langWarn = new ChangeInfo("", true, Messages.get(this, "lang_warn"));
			langWarn.hardlight(CharSprite.WARNING);
			changeInfos.add(langWarn);
		}

		switch (changesSelected){
			case 0: case 1: default:
				RKPD2Changes.values()[changesSelected].addAllChanges(changeInfos);
				break;
			case 2:
				v2_X_Changes.addAllChanges(changeInfos);
				break;
			case 3:
				v1_X_Changes.addAllChanges(changeInfos);
				break;
		}

		ScrollPane list = new ScrollPane( new Component() ){

			@Override
			public void onClick(float x, float y) {
				for (ChangeInfo info : changeInfos){
					if (info.onClick( x, y )){
						return;
					}
				}
			}

		};
		add( list );

		Component content = list.content();
		content.clear();

		float posY = 0;
		float nextPosY = 0;
		boolean second = false;
		for (ChangeInfo info : changeInfos){
			if (info.major) {
				posY = nextPosY;
				second = false;
				info.setRect(0, posY, panel.innerWidth(), 0);
				content.add(info);
				posY = nextPosY = info.bottom();
			} else {
				if (!second){
					second = true;
					info.setRect(0, posY, panel.innerWidth()/2f, 0);
					content.add(info);
					nextPosY = info.bottom();
				} else {
					second = false;
					info.setRect(panel.innerWidth()/2f, posY, panel.innerWidth()/2f, 0);
					content.add(info);
					nextPosY = Math.max(info.bottom(), nextPosY);
					posY = nextPosY;
				}
			}
		}

		content.setSize( panel.innerWidth(), (int)Math.ceil(posY) );

		list.setRect(
				panel.x + panel.marginLeft(),
				panel.y + panel.marginTop() - 1,
				panel.innerWidth() + 2,
				panel.innerHeight() + 2);
		list.scrollTo(0, 0);

		String[] labels = {"1-2.X", "0.X"/*, "SHPD 2.X", "SHPD 1.X"*/};
		float bx = list.left()-4f;
		for (int n=0; n < labels.length; n++) {
			final int N = n;
			StyledButton btn = new StyledButton(Chrome.Type.GREY_BUTTON_TR, labels[n]) {
				@Override
				protected void onClick() {
					super.onClick();
					if (changesSelected != N) {
						changesSelected = N;
						ShatteredPixelDungeon.seamlessResetScene();
					}
				}
			};
			if (changesSelected != n) btn.textColor( 0xBBBBBB );
			btn.setRect(bx, list.bottom(), btn.reqWidth()+2, changesSelected == n ? 19 : 15);
			bx = btn.right()-2;
			addToBack(btn);
		}
		Archs archs = new Archs();
		archs.setSize( Camera.main.width, Camera.main.height );
		addToBack( archs );

		fadeIn();
	}

	private void updateChangesText(Image icon, String title, String... messages) {
		if (changeTitle != null) {
			changeTitle.icon(icon);
			changeTitle.label(title);
			changeTitle.setPos(changeTitle.left(), changeTitle.top());

			String message = "";
			for (int i = 0; i < messages.length; i++) {
				message += messages[i].trim();
				if (i != messages.length - 1) {
					message += "\n\n";
				}
			}
			int pw = 135 + rightPanel.marginHor() - 2;
			changeBody.text(message, pw - rightPanel.marginHor());
			if(rightScroll == null) add(rightScroll = new ScrollPane(changeBody));
			//rightScroll.content().setSize(rightScroll.width(), changeBody.bottom() + 2);
			//rightScroll.setSize(rightScroll.width(), rightScroll.height());
			rightScroll.setRect(
					rightPanel.x + rightPanel.marginLeft(),
					changeTitle.bottom() + 2,
					rightPanel.innerWidth() + 2,
					rightPanel.innerHeight() - changeTitle.height() - 1);
			rightScroll.scrollTo(0, 0);

		} else {
			if (messages.length == 1) {
				addToFront(new WndChanges(icon, title, messages[0]));
			} else {
				addToFront(new WndChangesTabbed(icon, title, messages));
			}
		}
	}

	public static void showChangeInfo(Image icon, String title, String... messages){
		Scene s = ShatteredPixelDungeon.scene();
		if (s instanceof ChangesScene){
			((ChangesScene) s).updateChangesText(icon, title, messages);
			return;
		}
		if (messages.length == 1) {
			s.addToFront(new WndChanges(icon, title, messages[0]));
		} else {
			s.addToFront(new WndChangesTabbed(icon, title, messages));
		}
	}

	@Override
	protected void onBackPressed() {
		ShatteredPixelDungeon.switchNoFade(TitleScene.class);
	}

}
