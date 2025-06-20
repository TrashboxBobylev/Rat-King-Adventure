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

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.items.Ankh;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.bags.Bag;
import com.zrp200.rkpd2.journal.Catalog;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.InterlevelScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.ui.Icons;
import com.zrp200.rkpd2.ui.ItemButton;
import com.zrp200.rkpd2.ui.RedButton;
import com.zrp200.rkpd2.ui.RenderedTextBlock;
import com.zrp200.rkpd2.ui.Window;
import com.watabou.noosa.Game;

public class WndResurrect extends Window {
	
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 20;
	private static final float GAP		= 2;
	private static final float BTN_GAP  = 10;

	private static final int BTN_SIZE	= 36;

	public static Object instance;

	private ItemButton btnItem1;
	private ItemButton btnItem2;
	private ItemButton btnPressed;

	RedButton btnContinue;

	public WndResurrect( final Ankh ankh ) {
		
		super();
		
		instance = this;

		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( ankh.image(), null ) );
		titlebar.label( Messages.titleCase(Messages.get(this, "title")) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );
		
		RenderedTextBlock message = PixelScene.renderTextBlock(Messages.get(this, "message"), 6 );
		message.maxWidth(WIDTH);
		message.setPos(0, titlebar.bottom() + GAP);
		add( message );

		btnItem1 = new ItemButton() {
			@Override
			protected void onClick() {
				btnPressed = btnItem1;
				GameScene.selectItem( itemSelector );
			}
		};
		btnItem1.item(Dungeon.hero.belongings.weapon());
		btnItem1.setRect( (WIDTH - BTN_GAP) / 2 - BTN_SIZE, message.bottom() + BTN_GAP, BTN_SIZE, BTN_SIZE );
		add( btnItem1 );

		btnItem2 = new ItemButton() {
			@Override
			protected void onClick() {
				btnPressed = btnItem2;
				GameScene.selectItem( itemSelector );
			}
		};
		btnItem2.item(Dungeon.hero.belongings.armor());
		btnItem2.setRect( btnItem1.right() + BTN_GAP, btnItem1.top(), BTN_SIZE, BTN_SIZE );
		add( btnItem2 );

		btnContinue = new RedButton( Messages.get(this, "confirm") ) {
			@Override
			protected void onClick() {
				if (btnItem1.item() == null || btnItem2.item() == null){
					GameScene.show(new WndOptions(Icons.WARNING.get(),
							Messages.get(WndResurrect.class, "warn_title"),
							Messages.get(WndResurrect.class, "warn_body"),
							Messages.get(WndResurrect.class, "warn_yes"),
							Messages.get(WndResurrect.class, "warn_no")){
						@Override
						protected void onSelect(int index) {
							if (index == 0){
								resurrect(ankh);
							}
						}
					});
				} else {
					resurrect( ankh );
				}
			}
		};
		btnContinue.setRect( 0, btnItem1.bottom() + BTN_GAP, WIDTH, BTN_HEIGHT );
		add( btnContinue );

		resize( WIDTH, (int)btnContinue.bottom() );
	}

	private void resurrect( final Ankh ankh ){
		hide();

		Statistics.ankhsUsed++;
		Catalog.countUse(Ankh.class);

		ankh.detach(Dungeon.hero.belongings.backpack);

		if (btnItem1.item() != null){
			btnItem1.item().keptThoughLostInvent = true;
		}
		if (btnItem2.item() != null){
			btnItem2.item().keptThoughLostInvent = true;
		}

		InterlevelScene.mode = InterlevelScene.Mode.RESURRECT;
		Game.switchScene( InterlevelScene.class );
	}

	protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(WndResurrect.class, "prompt");
		}

		@Override
		public boolean itemSelectable(Item item) {
			//cannot select ankhs or bags or equippable items that aren't equipped
			return !(item instanceof Ankh || item instanceof Bag);
		}

		@Override
		public void onSelect( Item item ) {
			if (item != null && btnPressed.parent != null) {
				btnPressed.item( item );

				if (btnItem1.item() == btnItem2.item()){
					if (btnPressed == btnItem1){
						btnItem2.clear();
					} else {
						btnItem1.clear();
					}
				}

			}
		}
	};

	@Override
	public void destroy() {
		super.destroy();
		instance = null;
	}
	
	@Override
	public void onBackPressed() {
	}
}
