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

package com.zrp200.rkpd2.actors.hero.spells;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Enchanting;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.scrolls.Scroll;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTransmutation;
import com.zrp200.rkpd2.items.scrolls.exotic.ExoticScroll;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfEnchantment;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.zrp200.rkpd2.items.stones.InventoryStone;
import com.zrp200.rkpd2.items.stones.Runestone;
import com.zrp200.rkpd2.items.stones.StoneOfAugmentation;
import com.zrp200.rkpd2.items.stones.StoneOfEnchantment;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Reflection;
import com.zrp200.rkpd2.utils.GLog;

import java.util.Collections;
import java.util.List;

public class RecallInscription extends ClericSpell {

	public static RecallInscription INSTANCE = new RecallInscription();

	@Override
	public int icon() {
		return HeroIcon.RECALL_GLYPH;
	}

	@Override
	public void tintIcon(HeroIcon icon) {
		// todo should probably have unique icon
		if (SpellEmpower.isActive()) { icon.tint(0, .33f); }
	}

	@Override
	protected List<Object> getDescArgs() {
		return Collections.singletonList((int)UsedItemTracker.duration());
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {

		UsedItemTracker usedItem = hero.buff(UsedItemTracker.class);
		if (usedItem == null){
			return;
		}

		Item item = Reflection.newInstance(usedItem.item);

		item.setCurrent(hero);

		hero.sprite.operate(hero.pos);
		Enchanting.show(hero, item);

		if (item instanceof Scroll){
			((Scroll) item).anonymize();
			((Scroll) item).doRead();
		} else if (item instanceof Runestone){
			((Runestone) item).anonymize();
			if (item instanceof InventoryStone){
				((InventoryStone) item).directActivate();
			} else {
				//we're already on the render thread, but we want to delay this
				//as things like time freeze cancel can stop stone throwing from working
				ShatteredPixelDungeon.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						item.doThrow(hero);
					}
				});
			}
		}

		onSpellCast(tome, hero);

		if (SpellEmpower.isActive()) {
			// prevents spell cost from increasing
			UsedItemTracker.track(hero, item.getClass());
			// double it
			UsedItemTracker.prolong(hero, UsedItemTracker.class, 300f);
		} else {
			usedItem.used++;
			// remove if impossible to be used
			if (chargeUse(hero) > 20) usedItem.detach();
		}
	}

	protected float baseChargeUse(Hero hero) {
		if (hero.buff(UsedItemTracker.class) != null){
			Class<? extends Item> item = hero.buff(UsedItemTracker.class).item;
			if (ExoticScroll.class.isAssignableFrom(item)){
				if (item == ScrollOfMetamorphosis.class || item == ScrollOfEnchantment.class){
					return 8;
				} else {
					return 4;
				}
			} else if (Scroll.class.isAssignableFrom(item)){
				if (item == ScrollOfTransmutation.class){
					return 6;
				} else {
					return 3;
				}
			} else if (Runestone.class.isAssignableFrom(item)){
				if (item == StoneOfAugmentation.class || item == StoneOfEnchantment.class){
					return 4;
				} else {
					return 2;
				}
			}
		}
		return 0;
	}

	@Override
	public float chargeUse(Hero hero) {
		float chargeUse = baseChargeUse(hero);
		if (chargeUse == 0) return 0;
		// cost increases by +50% every use
		return (int)Math.ceil(chargeUse * (1 + hero.buff(UsedItemTracker.class).used/2f));
	}

	public static boolean canTrack(Hero hero) {
		return hero.hasTalent(Talent.RECALL_INSCRIPTION) || SpellEmpower.isActive();
	}

	@Override
	public boolean isVisible(Hero hero) {
		return super.isVisible(hero) || canTrack(hero);
	}

	@Override
	public boolean canCast(Hero hero) {
		return hero.virtualBuff(UsedItemTracker.class) != null;
	}

	public static class UsedItemTracker extends FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		public static void track(Hero hero, Class<?extends Item> item) {
			detach(hero, UsedItemTracker.class);
			prolong(hero, UsedItemTracker.class, duration()).item = item;
		}

		public Class<?extends Item> item;

		private int used = 0;

		@Override
		public int icon() {
			return BuffIndicator.GLYPH_RECALL;
		}

		public static float duration() {
			return Dungeon.hero.pointsInTalent(Talent.RECALL_INSCRIPTION) < 2 ? 10 : 300;
		}

		@Override
		public float iconFadePercent() {
			float duration = duration();
			return Math.max(0, (duration - visualcooldown()) / duration);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", Messages.titleCase(Reflection.newInstance(item).name()), dispTurns());
		}

		private static String ITEM = "item", USED = "used";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(ITEM, item);
			bundle.put(USED, used);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			item = bundle.getClass(ITEM);
			used = bundle.getInt(USED);
		}
	}

}
