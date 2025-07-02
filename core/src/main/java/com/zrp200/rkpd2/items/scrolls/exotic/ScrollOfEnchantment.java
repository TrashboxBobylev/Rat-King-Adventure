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

package com.zrp200.rkpd2.items.scrolls.exotic;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.hero.Belongings;
import com.zrp200.rkpd2.effects.Enchanting;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.items.bags.Bag;
import com.zrp200.rkpd2.items.scrolls.InventoryScroll;
import com.zrp200.rkpd2.items.scrolls.Scroll;
import com.zrp200.rkpd2.items.stones.StoneOfEnchantment;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.melee.MeleeWeapon;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.Icons;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndBag;
import com.zrp200.rkpd2.windows.WndOptions;
import com.zrp200.rkpd2.windows.WndTitledMessage;
import com.watabou.noosa.audio.Sample;

public class ScrollOfEnchantment extends ExoticScroll {
	
	{
		icon = ItemSpriteSheet.Icons.SCROLL_ENCHANT;

		unique = true;

		talentFactor = 2f;
	}

	protected static boolean identifiedByUse = false;

	@Override
	public void doRead() {
		if (!isKnown()) {
			identify();
			curItem = detach(curUser.belongings.backpack);
			identifiedByUse = true;
		} else {
			identifiedByUse = false;
		}
		GameScene.selectItem( itemSelector );
	}

	public static boolean enchantable( Item item ){
		return (item instanceof MeleeWeapon || item instanceof SpiritBow || item instanceof Armor ||
				(Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.ENCHANTED_WORLD) && item instanceof MissileWeapon));
	}

	private void confirmCancelation() {
		GameScene.show( new WndOptions(new ItemSprite(this),
				Messages.titleCase(name()),
				Messages.get(InventoryScroll.class, "warning"),
				Messages.get(InventoryScroll.class, "yes"),
				Messages.get(InventoryScroll.class, "no") ) {
			@Override
			protected void onSelect( int index ) {
				switch (index) {
					case 0:
						curUser.spendAndNext( TIME_TO_READ );
						identifiedByUse = false;
						break;
					case 1:
						GameScene.selectItem(itemSelector);
						break;
				}
			}
			public void onBackPressed() {}
		} );
	}

	@SuppressWarnings("unchecked")
	public void enchantWeapon(Weapon weapon) {
		final Weapon.Enchantment enchants[] = new Weapon.Enchantment[3];

		Class<? extends Weapon.Enchantment> existing = weapon.enchantment != null ? weapon.enchantment.getClass() : null;
		enchants[0] = Weapon.Enchantment.randomCommon(existing);
		enchants[1] = weapon instanceof SpiritBow
				? SpiritBow.randomUncommonEnchant(existing)
				: Weapon.Enchantment.randomUncommon(existing);
		Class[] toIgnore = {existing, enchants[0].getClass(), enchants[1].getClass()};
		enchants[2] = weapon instanceof SpiritBow
				? SpiritBow.randomEnchantment(toIgnore)
				: Weapon.Enchantment.random(toIgnore);

		GameScene.show(new WndEnchantSelect(weapon, enchants[0], enchants[1], enchants[2]));
	}

	protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(ScrollOfEnchantment.class, "inv_title");
		}

		@Override
		public Class<?extends Bag> preferredBag(){
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return enchantable(item);
		}

		@Override
		public void onSelect(final Item item) {

			if (item instanceof Weapon){
				if (!identifiedByUse) {
					curItem.detach(curUser.belongings.backpack);
				}
				identifiedByUse = false;
				
				enchantWeapon((Weapon)item);

			} if (item instanceof Armor) {
				if (!identifiedByUse) {
					curItem.detach(curUser.belongings.backpack);
				}
				identifiedByUse = false;
				
				final Armor.Glyph glyphs[] = new Armor.Glyph[3];
				
				Class<? extends Armor.Glyph> existing = ((Armor) item).glyph != null ? ((Armor) item).glyph.getClass() : null;
				glyphs[0] = Armor.Glyph.randomCommon( existing );
				glyphs[1] = Armor.Glyph.randomUncommon( existing );
				glyphs[2] = Armor.Glyph.random( existing, glyphs[0].getClass(), glyphs[1].getClass());

				GameScene.show(new WndGlyphSelect((Armor) item, glyphs[0], glyphs[1], glyphs[2]));
			} else if (identifiedByUse){
				ScrollOfEnchantment.this.confirmCancelation();
			}
		}
	};

	public static class WndEnchantSelect extends WndOptions {

		private static Weapon wep;
		private static Weapon.Enchantment[] enchantments;

		@SuppressWarnings("unused") //used in PixelScene.restoreWindows
		public WndEnchantSelect(){
			this(wep, enchantments[0], enchantments[1], enchantments[2]);
		}

		public WndEnchantSelect(Weapon wep, Weapon.Enchantment ench1,
		                           Weapon.Enchantment ench2, Weapon.Enchantment ench3){
			super(new ItemSprite(new ScrollOfEnchantment()),
					Messages.titleCase(new ScrollOfEnchantment().name()),
					Messages.get(ScrollOfEnchantment.class, "weapon"),
					ench1.name(),
					ench2.name(),
					ench3.name(),
					Messages.get(ScrollOfEnchantment.class, "cancel"));
			//noinspection AccessStaticViaInstance
			this.wep = wep;
			enchantments = new Weapon.Enchantment[3];
			enchantments[0] = ench1;
			enchantments[1] = ench2;
			enchantments[2] = ench3;

			WndGlyphSelect.arm = null;
		}

		@Override
		protected void onSelect(int index) {
			if (index < 3) {
				wep.enchant(enchantments[index]);
				GLog.p(Messages.get(StoneOfEnchantment.class, "weapon"));
				((Scroll)curItem).readAnimation();

				Sample.INSTANCE.play( Assets.Sounds.READ );
				Enchanting.show(curUser, wep);
			} else {
				GameScene.show(new WndConfirmCancel());
			}
		}

		@Override
		protected boolean hasInfo(int index) {
			return index < 3;
		}

		@Override
		protected void onInfo( int index ) {
			GameScene.show(new WndTitledMessage(
					Icons.get(Icons.INFO),
					Messages.titleCase(enchantments[index].name()),
					enchantments[index].desc()));
		}

		@Override
		public void onBackPressed() {
			//do nothing, reader has to cancel
		}

	}

	public static class WndGlyphSelect extends WndOptions {

		private static Armor arm;
		private static Armor.Glyph[] glyphs;

		@SuppressWarnings("unused") //used in PixelScene.restoreWindows
		public WndGlyphSelect() {
			this(arm, glyphs[0], glyphs[1], glyphs[2]);
		}

		public WndGlyphSelect(Armor arm, Armor.Glyph glyph1,
		                      Armor.Glyph glyph2, Armor.Glyph glyph3) {
			super(new ItemSprite(new ScrollOfEnchantment()),
					Messages.titleCase(new ScrollOfEnchantment().name()),
					Messages.get(ScrollOfEnchantment.class, "armor"),
					glyph1.name(),
					glyph2.name(),
					glyph3.name(),
					Messages.get(ScrollOfEnchantment.class, "cancel"));
			this.arm = arm;
			glyphs = new Armor.Glyph[3];
			glyphs[0] = glyph1;
			glyphs[1] = glyph2;
			glyphs[2] = glyph3;

			WndEnchantSelect.wep = null;
		}

		@Override
		protected void onSelect(int index) {
			if (index < 3) {
				arm.inscribe(glyphs[index]);
				GLog.p(Messages.get(StoneOfEnchantment.class, "armor"));
				((Scroll) curItem).readAnimation();

				Sample.INSTANCE.play(Assets.Sounds.READ);
				Enchanting.show(curUser, arm);
			} else {
				GameScene.show(new WndConfirmCancel());
			}
		}

		@Override
		protected boolean hasInfo(int index) {
			return index < 3;
		}

		@Override
		protected void onInfo(int index) {
			GameScene.show(new WndTitledMessage(
					Icons.get(Icons.INFO),
					Messages.titleCase(glyphs[index].name()),
					glyphs[index].desc()));
		}

		@Override
		public void onBackPressed() {
			//do nothing, reader has to cancel
		}

	}

	public static class WndConfirmCancel extends WndOptions{

		public WndConfirmCancel(){
			super(new ItemSprite(new ScrollOfEnchantment()),
					Messages.titleCase(new ScrollOfEnchantment().name()),
					Messages.get(ScrollOfEnchantment.class, "cancel_warn"),
					Messages.get(ScrollOfEnchantment.class, "cancel_warn_yes"),
					Messages.get(ScrollOfEnchantment.class, "cancel_warn_no"));
		}

		@Override
		protected void onSelect(int index) {
			super.onSelect(index);
			if (index == 1){
				if (WndEnchantSelect.wep != null) {
					GameScene.show(new WndEnchantSelect());
				} else {
					GameScene.show(new WndGlyphSelect());
				}
			} else {
				WndEnchantSelect.wep = null;
				WndEnchantSelect.enchantments = null;
				WndGlyphSelect.arm = null;
				WndGlyphSelect.glyphs = null;
			}
		}

		@Override
		public void onBackPressed() {
			//do nothing
		}
	}
}
