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

package com.zrp200.rkpd2.actors.hero.abilities.mage;

import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.items.wands.CursedWand;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;

import static com.zrp200.rkpd2.actors.hero.abilities.rat_king.OmniAbility.markAbilityUsed;

public class WildMagic extends ArmorAbility {

	{
		baseChargeUse = 25f;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
    public void activate(ClassArmor armor, Hero hero, Integer target) {
		if (target == null){
			return;
		}

		if (target == hero.pos){
			GLog.w(Messages.get(this, "self_target"));
			return;
		}

		ArrayList<Wand> wands = hero.belongings.getAllItems(Wand.class);
		Random.shuffle(wands);

		float chargeUsePerShot = (float)Math.pow(0.67f, hero.pointsInTalent(Talent.CONSERVED_MAGIC));

		for (Wand w : wands.toArray(new Wand[0])){
			if (w.curCharges < w.getMinCharges() + 1 && w.partialCharge < chargeUsePerShot){
				wands.remove(w);
			}
		}

		int maxWands = 4 + Dungeon.hero.pointsInTalent(Talent.FIRE_EVERYTHING);
		if (hero.hasTalent(Talent.ELDRITCH_BLESSING)) maxWands += 2 + hero.pointsInTalent(Talent.ELDRITCH_BLESSING)/2;

		//second and third shots
		if (wands.size() < maxWands){
			ArrayList<Wand> seconds = new ArrayList<>(wands);
			ArrayList<Wand> thirds = new ArrayList<>(wands);

			for (Wand w : wands){
				float totalCharge = w.curCharges + w.partialCharge;
				if (totalCharge < 2*chargeUsePerShot - 2*hero.pointsInTalent(Talent.HEROIC_WIZARDRY)){
					seconds.remove(w);
				}
				if (totalCharge < 3*chargeUsePerShot - 3*hero.pointsInTalent(Talent.HEROIC_WIZARDRY)
					|| Dungeon.hero.hasTalent(Talent.ELDRITCH_BLESSING) || Random.Int(4) > Dungeon.hero.pointsInTalent(Talent.CONSERVED_MAGIC)){
					thirds.remove(w);
				}
			}

			Random.shuffle(seconds);
			while (!seconds.isEmpty() && wands.size() < maxWands){
				wands.add(seconds.remove(0));
			}

			Random.shuffle(thirds);
			while (!thirds.isEmpty() && wands.size() < maxWands){
				wands.add(thirds.remove(0));
			}
		}

		if (wands.size() == 0){
			GLog.w(Messages.get(this, "no_wands"));
			markAbilityUsed(this);
			return;
		}

		hero.busy();

		Random.shuffle(wands);

		Buff.affect(hero, WildMagicTracker.class, 0f);

		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
		if (hero.hasTalent(Talent.ELDRITCH_BLESSING)) {
			CursedWand.eldritchLevel = hero.pointsInTalent(Talent.ELDRITCH_BLESSING) - 1;
		}

		zapWand(wands, hero, target);

	}

	@Override
	public boolean isActive() {
		return Actor.containsClass(WildMagicTracker.class);
	}

	public static class WildMagicTracker extends FlavourBuff{
		@Override
		protected void onRemove() {
			markAbilityUsed(new WildMagic());
		}
	};

	public static void zapWand(ArrayList<Wand> wands, Hero hero, int target){
		Wand cur = wands.remove(0);

		Ballistica aim = new Ballistica(hero.pos, target, cur.collisionProperties(target));

		hero.sprite.zap(target);

		if (!hero.hasTalent(Talent.ELDRITCH_BLESSING) && !cur.cursed) {
			cur.fx(aim, new Callback() {
				@Override
				public void call() {
					cur.onZap(aim);
					afterZap(cur, wands, hero, target);
				}
			});
		} else {
			CursedWand.cursedZap(cur,
					hero,
					new Ballistica(hero.pos, target, Ballistica.MAGIC_BOLT),
					new Callback() {
						@Override
						public void call() {
							afterZap(cur, wands, hero, target);
						}
					});
		}
	}

	private static void afterZap( Wand cur, ArrayList<Wand> wands, Hero hero, int target){
		cur.partialCharge -= (float) Math.pow(0.67f, hero.pointsInTalent(Talent.CONSERVED_MAGIC));
		if (cur.partialCharge < 0) {
			cur.partialCharge++;
			cur.curCharges--;
		}
		if (!wands.isEmpty()) {
			zapWand(wands, hero, target);
		} else {
			if (hero.buff(WildMagicTracker.class) != null) {
				hero.buff(WildMagicTracker.class).detach();
			}
			CursedWand.eldritchLevel = 0;
			Item.updateQuickslot();
			Invisibility.dispel();
			hero.spendAndNext(Actor.TICK);
		}
	}

	@Override
	public int icon() {
		return HeroIcon.WILD_MAGIC;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.WILD_POWER, Talent.FIRE_EVERYTHING, Talent.CONSERVED_MAGIC, Talent.ELDRITCH_BLESSING, Talent.HEROIC_ENERGY};
	}
}
