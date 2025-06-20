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
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.EnergyCrystal;
import com.zrp200.rkpd2.items.EquipableItem;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.AlchemyScene;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.RedButton;
import com.zrp200.rkpd2.utils.GLog;

public class WndEnergizeItem extends WndInfoItem {

	public static final float RKPD2_MULT = 1.5f;

	public static int energyVal(Item item) {
		return (int)Math.floor(item.energyVal() * 1.5f);
	}

	private static final int BTN_HEIGHT	= 18;

	private WndBag owner;

	public WndEnergizeItem(Item item, WndBag owner) {
		super(item);

		this.owner = owner;

		if (item.quantity() == 1) {
			addToBottom(new RedButton( Messages.get(this, "energize", energyVal(item)) ) {
				@Override
				protected void onClick() {
					energizeAll( item );
					hide();
				}
				{
					icon(new ItemSprite(ItemSpriteSheet.ENERGY));
					setSize(WndEnergizeItem.this.width, BTN_HEIGHT);
				}
			});
		} else {

			int energyAll = energyVal(item);
			RedButton first;
			addToBottom(
					first = new RedButton( Messages.get(this, "energize_1", energyAll / item.quantity()) ) {
						@Override
						protected void onClick() {
							energizeOne( item );
							hide();
						}
						{
							setSize(WndEnergizeItem.this.width, BTN_HEIGHT);
							icon(new ItemSprite(ItemSpriteSheet.ENERGY));
						}
					},
					new RedButton( Messages.get(this, "energize_all", energyAll ) ) {
						@Override
						protected void onClick() {
							energizeAll( item );
							hide();
						}
						{
							icon(new ItemSprite(ItemSpriteSheet.ENERGY));
							setRect(0, first.bottom() + GAP, first.width(), BTN_HEIGHT);
						}
					}
			);
		}
	}

	@Override
	public void hide() {

		super.hide();

		if (owner != null) {
			owner.hide();
			openItemSelector();
		}
	}

	public static void energizeAll(Item item ) {

		if (item.isEquipped( Dungeon.hero ) && !((EquipableItem)item).doUnequip( Dungeon.hero, false )) {
			return;
		}
		item.detachAll( Dungeon.hero.belongings.backpack );
		energize(item);
	}

	public static void energizeOne( Item item ) {

		if (item.quantity() <= 1) {
			energizeAll( item );
		} else {
			energize(item.detach( Dungeon.hero.belongings.backpack ));
		}
	}

	private static void energize(Item item){
		Hero hero = Dungeon.hero;

		if (ShatteredPixelDungeon.scene() instanceof AlchemyScene){

			Dungeon.energy += energyVal(item);
			((AlchemyScene) ShatteredPixelDungeon.scene()).createEnergy();
			if (!item.isIdentified()){
				((AlchemyScene) ShatteredPixelDungeon.scene()).showIdentify(item);
			}

		} else {

			//energizing items doesn't spend time
			hero.spend(-hero.cooldown());
			new EnergyCrystal(energyVal(item)).doPickUp(hero);
			item.identify();
			GLog.h("You energized: " + item.name());

		}
	}

	public static WndBag openItemSelector(){
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			return GameScene.selectItem( selector );
		} else {
			WndBag window = WndBag.getBag( selector );
			ShatteredPixelDungeon.scene().addToFront(window);
			return window;
		}
	}

	public static WndBag.ItemSelector selector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get(WndEnergizeItem.class, "prompt");
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item.energyVal() > 0;
		}

		@Override
		public void onSelect(Item item) {
			if (item != null) {
				WndBag parentWnd = openItemSelector();
				if (ShatteredPixelDungeon.scene() instanceof GameScene) {
					GameScene.show(new WndEnergizeItem(item, parentWnd));
				} else {
					ShatteredPixelDungeon.scene().addToFront(new WndEnergizeItem(item, parentWnd));
				}
			}
		}
	};

}
