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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.items.TengusMask;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.IconButton;
import com.zrp200.rkpd2.ui.Icons;
import com.zrp200.rkpd2.ui.RedButton;
import com.zrp200.rkpd2.ui.RenderedTextBlock;
import com.zrp200.rkpd2.ui.Window;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class WndChooseSubclass extends Window {
	
	private static final int WIDTH		= 130;
	private static final float GAP		= 2;

	public WndChooseSubclass(final TengusMask tome, final Hero hero) {
		this(tome, hero, hero.heroClass.subClasses());
	}
	public WndChooseSubclass(final TengusMask tome, final Hero hero, final ArrayList<HeroSubClass> subClasses) {
		
		super();

		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( tome.image(), null ) );
		titlebar.label( tome.name() );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		RenderedTextBlock message = PixelScene.renderTextBlock( 6 );
		message.text( Messages.get(tome.getClass(), "message"), WIDTH );
		message.setPos( titlebar.left(), titlebar.bottom() + GAP );
		add( message );

		float pos = message.bottom() + 3*GAP;

		for (HeroSubClass subCls : subClasses){
			RedButton btnCls = new RedButton( subCls.shortDesc(), 6 ) {
				private void resolve() {
					WndChooseSubclass.this.hide();
					tome.choose(subCls);
				}
				@Override
				protected void onClick() {
					if(hero.heroClass.subClasses().size() == 1) {
						// Oh no what a decision!!!
						resolve();
						return;
					}
					GameScene.show(new WndOptions(new HeroIcon(subCls),
							Messages.titleCase(subCls.title()),
							Messages.get(WndChooseSubclass.this, "are_you_sure"),
							Messages.get(WndChooseSubclass.this, "yes"),
							Messages.get(WndChooseSubclass.this, "no")){
						@Override
						protected void onSelect(int index) {
							hide();
							if (index == 0 && WndChooseSubclass.this.parent != null){
								resolve();
							}
						}
					});
				}
			};
			btnCls.leftJustify = true;
			btnCls.multiline = true;
			btnCls.setSize(WIDTH-20, btnCls.reqHeight()+2);
			btnCls.setRect( 0, pos, WIDTH-20, btnCls.reqHeight()+2);
			add( btnCls );

			IconButton clsInfo = new IconButton(Icons.get(Icons.INFO)){
				@Override
				protected void onClick() {
					GameScene.show(new WndInfoSubclass(Dungeon.hero.heroClass, subCls));
				}
			};
			clsInfo.setRect(WIDTH-20, btnCls.top() + (btnCls.height()-20)/2, 20, 20);
			add(clsInfo);

			pos = btnCls.bottom() + GAP;
		}
		if (Dungeon.hero.subClass == HeroSubClass.NONE) {
			RedButton btnCancel = new RedButton(Messages.get(this, "cancel")) {
				@Override
				protected void onClick() {
					hide();
				}

				@Override
				protected boolean onLongClick() {
					if (subClasses.size() == 2) {
						// this is how you access hidden subclasses, for now.
						for (HeroSubClass subClass : subClasses) {
							if (subClass == hero.heroClass.secretSub()) return false;
						}
						hide();
						ArrayList<HeroSubClass> subs = subClasses;
						subs.add(hero.heroClass.secretSub());
						// fixme maybe I should just get a designated secret pitch and volume?
						Sample.INSTANCE.play(Assets.Sounds.SECRET, 0.5f);
						GameScene.show(new WndChooseSubclass(tome, hero, subs));
						return true;
					}
					return true;
				}
			};
			btnCancel.setRect(0, pos, WIDTH, 18);
			add(btnCancel);

			resize(WIDTH, (int) btnCancel.bottom());
		} else {
			resize(WIDTH, (int) (pos + 1));
		}
	}

	@Override
	public void onBackPressed() {
		if (Dungeon.hero.subClass == HeroSubClass.NONE)
			super.onBackPressed();
		else return;
	}
}
