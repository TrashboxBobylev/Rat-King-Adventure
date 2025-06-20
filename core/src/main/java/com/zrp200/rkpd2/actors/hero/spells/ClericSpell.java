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

import com.watabou.utils.GameMath;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.AscendedForm;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.PowerOfMany;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.HeroIcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ClericSpell {

	public abstract void onCast(HolyTome tome, Hero hero);

	public float chargeUse( Hero hero ){
		return 1;
	}

	public boolean ignoreChargeUse(HolyTome holyTome) {
		return SpellEmpower.isActive();
	}

	public boolean isVisible( Hero hero ) {
		return canCast(hero);
	}

	public boolean canCast( Hero hero ){
		return true;
	}

	public String name(){
		return checkEmpowerMsg("name");
	}

	public String shortDesc(){
		return checkEmpowerMsg("short_desc") + " " + chargeUseDesc();
	}

	public final String checkEmpowerMsg(String key, Object... args) {
		String res = Messages.NO_TEXT_FOUND;
		if (SpellEmpower.isActive()) {
			res = Messages.get(this, key + "_empower", args);
		}
        //noinspection StringEquality
        return res == Messages.NO_TEXT_FOUND ? Messages.get(this, key, args) : res;
	}

	public String desc(){
		return checkEmpowerMsg("desc", getDescArgs().toArray()) + "\n\n" + chargeUseDesc();
	}

	protected String chargeUseDesc() {
		float chargeUse = chargeUse(Dungeon.hero);
		String desc = chargeCostDesc(chargeUse);
		if (chargeUse > 0) {
			String chargeCostScale = checkEmpowerMsg("charge_cost_scale");
			if (!chargeCostScale.isEmpty() && !Messages.NO_TEXT_FOUND.equals(chargeCostScale)) desc += " " + chargeCostScale;
		}
		return desc;
	}

	protected String chargeCostDesc(float chargeUse) {
		return checkEmpowerMsg("charge_cost", Messages.decimalFormat("0.#", chargeUse));
	}

    protected List<Object> getDescArgs() {
		return Collections.emptyList();
	}

	public boolean usesTargeting(){
		return targetingFlags() != -1;
	}

	public int targetingFlags(){
		return -1; //-1 for no targeting
	}

	public int icon(){
		return HeroIcon.NONE;
	}

	public void tintIcon(HeroIcon icon) {/* do nothing */}

	public void onSpellCast(HolyTome tome, Hero hero){
		Invisibility.dispel();
		if (hero.buff(Talent.SatiatedSpellsTracker.class) != null){
			int amount = 3 * (hero.pointsInTalent(Talent.SATIATED_SPELLS) + 1) - 1;
			Buff.affect(hero, Barrier.class).setShield(amount);
			Char ally = PowerOfMany.getPoweredAlly();
			if (ally != null && ally.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null){
				Buff.affect(ally, Barrier.class).setShield(amount);
			}
			hero.buff(Talent.SatiatedSpellsTracker.class).detach();
		}
		{
			float empoweredChargeUse = chargeUse(hero);
			// these don't actually have an empowered effect
			// they still consume if ignoring charge use
			if (this == Smite.INSTANCE
					|| this == DivineIntervention.INSTANCE
//					|| this == Judgement.INSTANCE
					|| this == Flash.INSTANCE
					|| this == BodyForm.INSTANCE
					|| this == SpiritForm.INSTANCE
					|| this == BeamingRay.INSTANCE
					|| this == LifeLinkSpell.INSTANCE
					|| this == Stasis.INSTANCE
			) empoweredChargeUse -= Math.max(tome.getCharges(), empoweredChargeUse);
			SpellEmpower.useCharge(empoweredChargeUse);
		}
		tome.spendCharge(chargeUse(hero));
		Talent.onArtifactUsed(hero);
		if (hero.subClass.is(HeroSubClass.PALADIN)) {
			for (PaladinSpellExtendable buff : hero.buffs(PaladinSpellExtendable.class)) {
				if (this != buff.getSourceSpell()) {
					buff.extend( buff.getTurnsPerCharge() * chargeUse(hero));
				}
			}
		}

		if (hero.buff(AscendedForm.AscendBuff.class) != null){
			hero.buff(AscendedForm.AscendBuff.class).spellCasts++;
			hero.buff(AscendedForm.AscendBuff.class).incShield((int)(10*chargeUse(hero)));
		}
	}

	public static abstract class PaladinSpellExtendable extends FlavourBuff {
		abstract ClericSpell getSourceSpell();

		abstract protected float getDuration();
		abstract public float getTurnsPerCharge();

		/** keeps subclasses over the parent **/
		public static <T extends PaladinSpellExtendable> T virtualAffect(Hero hero, Class<T> parentClass, Class<? extends T> buffClass) {
			T toAdd = Buff.affect(hero, buffClass);
			toAdd.spend(toAdd.getDuration());
			T exist = hero.virtualBuff(parentClass);
			if (exist == toAdd) return toAdd;
			T base, keep;
			if (toAdd.getClass() == parentClass) {
				base = toAdd;
				keep = exist;
			} else {
				base = exist;
				keep = toAdd;
			}
			keep.spend(base.cooldown());
			base.detach();
			return keep;
		}

		public float getMaxDuration() { return 100; }

		public void extend(float extension) {
			postpone(Math.min(getMaxDuration(), cooldown() + extension));
		}

		public String getExtendableMessage() {
			return Messages.get(this, "extendable", name(), Math.round(getTurnsPerCharge()));
		}

		public float iconFadePercent() {
			return 1 - GameMath.gate(0, visualcooldown() / Math.min(getDuration(), getMaxDuration()), 1);
		}

	}

	public static ArrayList<ClericSpell> getSpellList(Hero cleric, int tier){
		ArrayList<ClericSpell> spells = new ArrayList<>();

		if (tier == 1) {

			spells.add(GuidingLight.INSTANCE);
			spells.add(HolyWeapon.INSTANCE);
			spells.add(HolyWard.INSTANCE);

			for (ClericSpell spell : new ClericSpell[]{
					HolyIntuition.INSTANCE, ShieldOfLight.INSTANCE
			}) if (spell.isVisible(cleric)) spells.add(spell);

		} else if (tier == 2) {

			for (ClericSpell spell : new ClericSpell[]{
					RecallInscription.INSTANCE,
					Sunray.INSTANCE,
					DivineSense.INSTANCE,
					BlessSpell.INSTANCE
			}) if (spell.isVisible(cleric)) spells.add(spell);

		} else if (tier == 3){

			for (ClericSpell spell : new ClericSpell[]{
				Radiance.INSTANCE,
				Smite.INSTANCE,
				Cleanse.INSTANCE,
				HolyLance.INSTANCE,
				HallowedGround.INSTANCE,
				MnemonicPrayer.INSTANCE,
				SpellEmpower.DivineAdvent.INSTANCE,
				LayOnHands.INSTANCE,
				AuraOfProtection.INSTANCE,
				WallOfLight.INSTANCE,
				SpellEmpower.LimitBreak.INSTANCE
			}){
				if (spell.isVisible(cleric)) spells.add(spell);
			}

		} else if (tier == 4){

			if (cleric.hasTalent(Talent.DIVINE_INTERVENTION)){
				spells.add(DivineIntervention.INSTANCE);
			}
			if (cleric.hasTalent(Talent.JUDGEMENT)){
				spells.add(Judgement.INSTANCE);
			}
			if (cleric.hasTalent(Talent.FLASH)){
				spells.add(Flash.INSTANCE);
			}

			if (cleric.hasTalent(Talent.BODY_FORM)){
				spells.add(BodyForm.INSTANCE);
			}
			if (cleric.hasTalent(Talent.MIND_FORM)){
				spells.add(MindForm.INSTANCE);
			}
			if (cleric.hasTalent(Talent.SPIRIT_FORM)){
				spells.add(SpiritForm.INSTANCE);
			}

			if (cleric.hasTalent(Talent.BEAMING_RAY)){
				spells.add(BeamingRay.INSTANCE);
			}
			if (cleric.hasTalent(Talent.LIFE_LINK)){
				spells.add(LifeLinkSpell.INSTANCE);
			}
			if (cleric.hasTalent(Talent.STASIS)){
				spells.add(Stasis.INSTANCE);
			}

		}

		return spells;
	}

	public static ArrayList<ClericSpell> getAllSpells() {
		ArrayList<ClericSpell> spells = new ArrayList<>();
		spells.add(GuidingLight.INSTANCE);
		spells.add(HolyWeapon.INSTANCE);
		spells.add(HolyWard.INSTANCE);
		spells.add(HolyIntuition.INSTANCE);
		spells.add(ShieldOfLight.INSTANCE);
		spells.add(RecallInscription.INSTANCE);
		spells.add(Sunray.INSTANCE);
		spells.add(DivineSense.INSTANCE);
		spells.add(BlessSpell.INSTANCE);
		spells.add(Cleanse.INSTANCE);
		spells.add(Radiance.INSTANCE);
		spells.add(Smite.INSTANCE);
		spells.add(LayOnHands.INSTANCE);
		spells.add(AuraOfProtection.INSTANCE);
		spells.add(SpellEmpower.DivineAdvent.INSTANCE);
		spells.add(WallOfLight.INSTANCE);
		spells.add(HolyLance.INSTANCE);
		spells.add(HallowedGround.INSTANCE);
		spells.add(MnemonicPrayer.INSTANCE);
		spells.add(SpellEmpower.DivineAdvent.INSTANCE);
		spells.add(DivineIntervention.INSTANCE);
		spells.add(Judgement.INSTANCE);
		spells.add(Flash.INSTANCE);
		spells.add(BodyForm.INSTANCE);
		spells.add(MindForm.INSTANCE);
		spells.add(SpiritForm.INSTANCE);
		spells.add(BeamingRay.INSTANCE);
		spells.add(LifeLinkSpell.INSTANCE);
		spells.add(Stasis.INSTANCE);
		return spells;
	}
}
