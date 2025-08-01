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

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.SPDAction;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.watabou.input.GameAction;

public class LootIndicator extends Tag {
	
	private ItemSlot slot;
	
	private Item lastItem = null;
	private int lastQuantity = 0;
	
	public LootIndicator() {
		super( 0x185898 );
		
		setSize( SIZE, SIZE );
		
		visible = false;
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		slot = new ItemSlot() {
			protected void onClick() {
				LootIndicator.this.onClick();
				if (Dungeon.hero.ready && Dungeon.hero.handle(Dungeon.hero.pos)){
					Dungeon.hero.next();
				}

			}

			@Override
			public GameAction keyAction() {
				return SPDAction.TAG_LOOT;
			}

			@Override
			public GameAction secondaryTooltipAction() {
				return SPDAction.WAIT_OR_PICKUP;
			}
		};
		slot.showExtraInfo( false );
		add( slot );
	}
	
	@Override
	protected void layout() {
		super.layout();

		if (!flipped) {
			slot.setRect( x, y, SIZE, height );
			slot.setMargins(2, 2, 0, 2);
		} else {
			slot.setRect( x+(width()-SIZE), y, SIZE, height );
			slot.setMargins(0, 2, 2, 2);
		}

	}
	
	@Override
	public void update() {
		
		if (Dungeon.hero.ready) {
			Heap heap = Dungeon.level.heaps.get( Dungeon.hero.pos );
			if (heap != null) {
				
				Item item =
					heap.type == Heap.Type.CHEST ? ItemSlot.CHEST :
					heap.type == Heap.Type.LOCKED_CHEST ? ItemSlot.LOCKED_CHEST :
					heap.type == Heap.Type.CRYSTAL_CHEST ? ItemSlot.CRYSTAL_CHEST :
					heap.type == Heap.Type.TOMB ? ItemSlot.TOMB :
					heap.type == Heap.Type.SKELETON ? ItemSlot.SKELETON :
					heap.type == Heap.Type.REMAINS ? ItemSlot.REMAINS :
					heap.type == Heap.Type.EBONY_CHEST ? ItemSlot.EBONY_CHEST :
					heap.peek();
				if (item != lastItem || item.quantity() != lastQuantity) {
					lastItem = item;
					lastQuantity = item.quantity();
					
					slot.item( item );
					flash();
				}
				visible = true;
				
			} else {
				
				lastItem = null;
				visible = false;
				
			}
		}
		
		slot.enable( visible && Dungeon.hero.ready );
		
		super.update();
	}
}
