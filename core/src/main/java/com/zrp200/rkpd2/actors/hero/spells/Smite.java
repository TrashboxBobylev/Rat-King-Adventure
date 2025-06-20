/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

import static com.zrp200.rkpd2.Dungeon.hero;
import static com.zrp200.rkpd2.Dungeon.level;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Combo;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.AttackIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.ui.RedButton;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.windows.WndTitledMessage;

import java.util.HashMap;

public class Smite extends TargetedClericSpell {

	public static Smite INSTANCE = new Smite();

	/** makes it act as omnismite if omnismite is usable **/
	private boolean override = false;
	static {
		INSTANCE.override = true;
	}

	@Override
	public int icon() {
		if (override && SpellEmpower.isActive()) return OmniSmite.INSTANCE.icon();
		return HeroIcon.SMITE;
	}

	@Override
	public int targetingFlags() {
		return Ballistica.STOP_TARGET; //no auto-aim
	}

	@Override
	public String shortDesc() {
		String shortDesc = super.shortDesc();
		if (!override || !SpellEmpower.isActive()) return shortDesc;
		// display both variants
		return shortDesc + "\n\n"
				+ "_" + Messages.titleCase(OmniSmite.INSTANCE.name()) + "_" + "\n"
				+ OmniSmite.INSTANCE.shortDesc();
	}

	@Override
	public String desc() {
		int min = 5 + Dungeon.hero.lvl/2;
		int max = 10 + Dungeon.hero.lvl;
		String desc = Messages.get(this, "desc", min, max)
				+ "\n\n"
				+ chargeUseDesc();
		if (SpellEmpower.isActive() && override) {
			desc += "\n"
					+ Messages.get(this, "desc_override")
					+ "\n\n"
					+ OmniSmite.INSTANCE.desc();
		}
		return desc;
	}

	@Override
	public float chargeUse(Hero hero) {
		return 2f;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.subClass == HeroSubClass.PALADIN;
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
        if (override && tome.canCast(hero, new OmniSmite())) {
            GameScene.show(new WndSmiteSelect(tome, hero));
        }
        else {
            super.onCast(tome, hero);
        }
    }

	@Override
	protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
		if (target == null) {
			return;
		}

		Char enemy = Actor.findChar(target);
		if (enemy == null || enemy == hero) {
			GLog.w(Messages.get(this, "no_target"));
			return;
		}

		//we apply here because of projecting
		SmiteTracker tracker = Buff.affect(hero, SmiteTracker.class);
		int leapPos = Combo.Leap.findLeapPos(hero, enemy, hero.pointsInTalent(Talent.TRIAGE));
		if (leapPos == -1) {
			GLog.w(Messages.get(this, "invalid_enemy"));
			tracker.detach();
			return;
		}
		Combo.Leap.execute(hero, enemy, leapPos, () -> {
			AttackIndicator.target(enemy);
			doSmite(tome, hero, enemy, 1);
			tracker.detach();
			hero.spendAndNext(hero.attackDelay());
		});
	}

	public void doSmite(HolyTome tome, Hero hero, Char enemy, float dmgMulti) {

		float accMult = 1;
		if (!(hero.belongings.attackingWeapon() instanceof Weapon)
				|| ((Weapon) hero.belongings.attackingWeapon()).STRReq() <= hero.STR()) {
			accMult = Char.INFINITE_ACCURACY;
		}
		if (hero.attack(enemy, 1, 0, accMult)) {
			Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
			enemy.sprite.burst(0xFFFFFFFF, Random.round(10 * dmgMulti));
		}

		onSpellCast(tome, hero);
	}

	public static class SmiteTracker extends FlavourBuff {
		public int bonusDmg( Hero attacker, Char defender){
			int min = 5 + attacker.lvl/2;
			int max = 10 + attacker.lvl;
			if (Char.hasProp(defender, Char.Property.UNDEAD) || Char.hasProp(defender, Char.Property.DEMONIC)){
				return max;
			} else {
				return Random.NormalIntRange(min, max);
			}
		}
		public float enchMulti() {
			return 3f;
		}
	};

	// should this really be a smite subclass? the only method it actually takes from it is doSmite
	public static class OmniSmite extends Smite {

		public static final OmniSmite INSTANCE = new OmniSmite();

		@Override
		public int icon() { return HeroIcon.OMNISMITE; }

		@Override
		public boolean usesTargeting() { return false; }

		// this is basically a fancy way of killing anything.
		// todo revisit, it might be nice to have the last hit be a regular smite
		//   could also have the damage reduction be after armor is applied

		private static int used = 0;

		private static final int
				MIN_USES = 4,
				MIN_COST = 2;
		public static final float MULTI = 0.6f;

		@Override
		public boolean canCast(Hero hero) {
			return used > 0 ||
					super.canCast(hero) && SpellEmpower.isActive();
		}

		private boolean forceCheckCharges = false;

		private int leapRange;
		private int getLeapRange() {
			return hero.pointsInTalent(Talent.TRIAGE);
		}

		@Override
		public String desc() {
			HolyTome tome = hero.belongings.getItem(HolyTome.class);
			String desc = Messages.get(this, "desc",
					MULTI + "x",
					// max attacks
					Math.max(MIN_USES, (int)Math.max(tome.getCharges(), SpellEmpower.left()))
			);
			if (getLeapRange() > 0) desc += "\n\n" + Messages.get(this, "leap", getLeapRange());
			return desc + "\n\n" + chargeUseDesc();
		}

		@Override
		public String shortDesc() {
			return Messages.get(this, "short_desc", Math.max(
					MIN_USES,
					(int)Math.max(hero.belongings.getItem(HolyTome.class).getCharges(),
							SpellEmpower.left())
			)) + " " + chargeUseDesc();
		}

		@Override
		protected String chargeUseDesc() {
			int maxCost = Math.max(
					hero.isNearDeath() ? 0 : MIN_COST,
					(int)hero.belongings.getItem(HolyTome.class).getCharges()
			);
			return Messages.get(ClericSpell.class, "charge_cost",
					maxCost == 0 ? 0 :
							Messages.get(this, "charge_cost", (int)chargeUse(hero), maxCost)
			);
		}

		@Override
		public boolean ignoreChargeUse(HolyTome holyTome) {
			// used when checking if smite can attack again
			return !forceCheckCharges && (used < MIN_USES || super.ignoreChargeUse(holyTome));
		}

		@Override
		public float chargeUse(Hero hero) {
			return 1f;
		}

		@Override
		public void onSpellCast(HolyTome tome, Hero hero) {
			if ((used >= MIN_COST || hero.isNearDeath()) && SpellEmpower.isActive()) {
				// refunds charge use if it's still attacking with no charges after breaking even with Smite's charge cost.
				forceCheckCharges = true;
				if (!tome.canCast(hero, this)) tome.directCharge(chargeUse(hero));
				forceCheckCharges = false;
			}
			super.onSpellCast(tome, hero);
		}

		@Override
		public void onCast(HolyTome tome, Hero hero) {
			if (used == 0) leapRange = getLeapRange();

			SmiteTracker tracker = Buff.affect(hero, OmniSmiteTracker.class);

			HashMap<Char, Integer> validTargets = new HashMap<>();
			int minDist = Integer.MAX_VALUE;
			for (Char m : level.mobs) {
				int leapPos = Combo.Leap.findLeapPos(hero, m, leapRange);
				if (leapPos == -1) continue;
				int distance = level.distance(hero.pos, leapPos);
				if (distance < minDist) {
					validTargets.clear();
					minDist = distance;
				}
				validTargets.put(m, leapPos);
			}

			Char target = Random.element(validTargets.keySet());
			if (tome.canCast(hero, this) && target != null) {
				Combo.Leap.execute(hero, target, validTargets.get(target), () -> {
					used++;
					doSmite(tome, hero, target, MULTI);
					onCast(tome, hero);
				});
				return;
			}

			tracker.detach();
			if (used > 0) {
				used = 0;
				SpellEmpower.useCharge(SpellEmpower.limit()); // use remaining charge
				hero.spendAndNext(hero.attackDelay());
			} else {
				assert target == null;
				GLog.w(Messages.get(this, "no_targets"));
			}
		}

		public static class OmniSmiteTracker extends SmiteTracker {
			@Override
			public int bonusDmg(Hero attacker, Char defender) {
				return Random.round(MULTI * super.bonusDmg(attacker, defender));
			}
			// see Weapon#genericProcChanceMultiplier
		}
	}

	// this is an admittedly obtuse way of preventing misclicks on omnismite.
	//  todo either drastically improve this or make omnismite's initial target selection manual
	private static class WndSmiteSelect extends WndTitledMessage {
        WndSmiteSelect(HolyTome tome, Hero hero) {
			super(new HeroIcon(INSTANCE), "Smite", "Choose variant of Smite to use:");
			INSTANCE.override = false;
			RedButton[] buttons = new RedButton[2];
			int i = 0;
			for (ClericSpell smite : new ClericSpell[]{Smite.INSTANCE, OmniSmite.INSTANCE}) {
				float top = i > 0 ? buttons[i-1].bottom() + GAP : 0;
				buttons[i++] = new RedButton("_" + Messages.titleCase(smite.name()) + "_:\n" + smite.desc(), 6) {
					{
						multiline = true;
						setWidth(WndSmiteSelect.this.width);
						setHeight(reqHeight());
						setY(top);
					}

					@Override
					protected void onClick() {
						if (!smite.usesTargeting()) QuickSlotButton.cancel();
						smite.onCast(tome, hero);
						hide();
					}
				};
			}
            addToBottom(buttons);
        }

		@Override
		public void hide() {
			INSTANCE.override = true;
			super.hide();
		}

		@Override
		public void onBackPressed() {
			super.onBackPressed();
			QuickSlotButton.cancel();
		}

	}

}
