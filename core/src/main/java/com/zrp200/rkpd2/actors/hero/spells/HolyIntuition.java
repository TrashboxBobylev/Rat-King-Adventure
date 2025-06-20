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

import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Identification;
import com.zrp200.rkpd2.items.EquipableItem;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.scrolls.ScrollOfIdentify;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRemoveCurse;
import com.zrp200.rkpd2.items.wands.CursedWand;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class HolyIntuition extends InventoryClericSpell {

	public static final HolyIntuition INSTANCE = new HolyIntuition();

	@Override
	public int icon() {
		return HeroIcon.HOLY_INTUITION;
	}

	@Override
	public void tintIcon(HeroIcon icon) {
		// todo make icon
		if (SpellEmpower.isActive()) icon.tint(0, .33f);
	}

	@Override
	protected boolean usableOnItem(Item item) {
		return !item.isIdentified() || ScrollOfRemoveCurse.uncursable(item);
	}

	@Override
	public float chargeUse(Hero hero) {
		return 4 - hero.pointsInTalent(Talent.HOLY_INTUITION);
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.hasTalent(Talent.HOLY_INTUITION);
	}

	@Override
	protected void onItemSelected(HolyTome tome, Hero hero, Item item) {
		if (item == null){
			return;
		}

		boolean cursedKnown = item.cursedKnown;
		affectItem(hero, item, SpellEmpower.isActive() ? 1 : cursedKnown ? 3 : 4);
		if (SpellEmpower.isActive() && usableOnItem(item)) {
			affectItem(hero, item, cursedKnown ? 2 : 3);
		}

		onSpellCast(tome, hero);

	}
	private void affectItem(Hero hero, Item item, int chance) {

		boolean cursedKnown = item.cursedKnown;
		item.cursedKnown = true;

		if (!item.cursed && Random.Int(chance) == 0) {
			new ScrollOfIdentify().onItemSelected(item);
		}
		else if (ScrollOfRemoveCurse.uncursable(item) && Random.Int(chance) == 0) {
			ScrollOfRemoveCurse.doEffect(hero, item);
		}
		else if (!cursedKnown) {
			if (item.cursed){
				GLog.w(Messages.get(this, "cursed"));
			} else {
				GLog.i(Messages.get(this, "uncursed"));
			}

			hero.sprite.parent.add( new Identification( hero.sprite.center().offset( 0, -16 ) ) );
		}
		else if (!SpellEmpower.isActive()){
			// Spell Empower always does something
			GLog.w(Messages.get(CursedWand.class, "nothing"));
		}
	}

	@Override
	public void onSpellCast(HolyTome tome, Hero hero) {
		hero.spend( 1f );
		hero.busy();
		hero.sprite.operate(hero.pos);

		Sample.INSTANCE.play( Assets.Sounds.READ );
		super.onSpellCast(tome, hero);

	}

}
