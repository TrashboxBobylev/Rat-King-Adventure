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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.AdrenalineSurge;
import com.zrp200.rkpd2.actors.buffs.ArcaneArmor;
import com.zrp200.rkpd2.actors.buffs.ArtifactRecharge;
import com.zrp200.rkpd2.actors.buffs.Barkskin;
import com.zrp200.rkpd2.actors.buffs.Bleeding;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.Corrosion;
import com.zrp200.rkpd2.actors.buffs.Dread;
import com.zrp200.rkpd2.actors.buffs.FireImbue;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.GreaterHaste;
import com.zrp200.rkpd2.actors.buffs.Healing;
import com.zrp200.rkpd2.actors.buffs.LifeLink;
import com.zrp200.rkpd2.actors.buffs.Ooze;
import com.zrp200.rkpd2.actors.buffs.Poison;
import com.zrp200.rkpd2.actors.buffs.ShieldBuff;
import com.zrp200.rkpd2.actors.buffs.ToxicImbue;
import com.zrp200.rkpd2.actors.buffs.WellFed;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.AscendedForm;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.PowerOfMany;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.armor.glyphs.Viscosity;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.potions.elixirs.ElixirOfAquaticRejuvenation;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfChallenge;
import com.zrp200.rkpd2.items.weapon.enchantments.Kinetic;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.plants.Sungrass;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class MnemonicPrayer extends TargetedClericSpell {

	public static MnemonicPrayer INSTANCE = new MnemonicPrayer();

	@Override
	public int icon() {
		return HeroIcon.MNEMONIC_PRAYER;
	}

	@Override
	public int targetingFlags() {
		return Ballistica.STOP_TARGET;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.hasTalent(Talent.MNEMONIC_PRAYER);
	}

	private static boolean multiCast = false;
	private static boolean multiCastedDebuff = false;

	@Override
	public boolean usesTargeting() {
		return !SpellEmpower.isActive();
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
		if (SpellEmpower.isActive()) {
			// cast on all valid targets
			multiCastedDebuff = false;
			multiCast = true;
			try {
				for (Char ch : Dungeon.level.mobs) {
					if (Dungeon.level.heroFOV[ch.pos]) {
						onTargetSelected(tome, hero, ch.pos);
					}
				}
			} finally {
				multiCast = multiCastedDebuff = false;
			}
			onTargetSelected(tome, hero, hero.pos);
		} else {
			super.onCast(tome, hero);
		}
	}


	@Override
	protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {

		if (target == null){
			return;
		}

		Char ch = Actor.findChar(target);
		if (ch == null || !Dungeon.level.heroFOV[target]){
			GLog.w(Messages.get(this, "no_target"));
			return;
		}

		QuickSlotButton.target(ch);

		float extension = 2 + hero.pointsInTalent(Talent.MNEMONIC_PRAYER);

		Char ally = PowerOfMany.getPoweredAlly();
		if (ally != null && ally.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null){
			if (ch == hero){
				if (multiCast) extension *= 2; // double cast
				affectChar(ally, extension); //if cast on hero, duplicate to ally
			} else if (ch == ally){
				affectChar(hero, extension); //if cast on ally, duplicate to hero
			}
		}
		affectChar(ch, extension);

		if (multiCast) return;

		if (ch == hero){
			hero.busy();
			hero.sprite.operate(ch.pos);
			hero.spend( 1f );
			BuffIndicator.refreshHero();
		} else {
			hero.sprite.zap(ch.pos);
			hero.spendAndNext( 1f );
		}

		onSpellCast(tome, hero);

	}

	private void affectChar( Char ch, float extension ){
		boolean affected = !multiCast;
		if (ch.alignment == Char.Alignment.ALLY){

			for (Buff b : ch.buffs()){
				if (b.type != Buff.buffType.POSITIVE || b.mnemonicExtended || b.icon() == BuffIndicator.NONE){
					continue;
				}

				//does not boost buffs from armor abilities or T4 spells
				if (b instanceof AscendedForm.AscendBuff
						|| b instanceof SpellEmpower.Buff
						|| b instanceof BodyForm.BodyFormBuff || b instanceof SpiritForm.SpiritFormBuff
						|| b instanceof PowerOfMany.PowerBuff || b instanceof BeamingRay.BeamingRayBoost || b instanceof LifeLink || b instanceof LifeLinkSpell.LifeLinkSpellBuff){
					continue;
				}

				//should consider some buffs that may be OP here, e.g. invuln
				if (b instanceof FlavourBuff)           Buff.affect(ch, (Class<?extends FlavourBuff>)b.getClass(), extension);
				else if (b instanceof AdrenalineSurge)  ((AdrenalineSurge) b).delay(extension);
				else if (b instanceof ArcaneArmor)      ((ArcaneArmor) b).delay(extension);
				else if (b instanceof ArtifactRecharge) ((ArtifactRecharge) b).extend(extension);
				else if (b instanceof Barkskin)         ((Barkskin) b).delay(extension);
				else if (b instanceof FireImbue)        ((FireImbue) b).extend(extension);
				else if (b instanceof GreaterHaste)     ((GreaterHaste) b).extend(extension);
				else if (b instanceof Healing)          ((Healing) b).increaseHeal((int)extension);
				else if (b instanceof ToxicImbue)       ((ToxicImbue) b).extend(extension);
				else if (b instanceof WellFed)          ((WellFed) b).extend(extension);
				else if (b instanceof ElixirOfAquaticRejuvenation.AquaHealing)  ((ElixirOfAquaticRejuvenation.AquaHealing) b).extend(extension);
				else if (b instanceof ScrollOfChallenge.ChallengeArena)         ((ScrollOfChallenge.ChallengeArena) b).extend(extension);
				else if (b instanceof ShieldBuff)               ((ShieldBuff) b).delay(extension);
				else if (b instanceof Kinetic.ConservedDamage)  ((Kinetic.ConservedDamage) b).delay(extension);
				else if (b instanceof Sungrass.Health)          ((Sungrass.Health) b).boost((int) extension);

				b.mnemonicExtended = affected = true;

			}
			if (affected) {
				// empowered plays sound on the hero's self-target
				if (!multiCast) Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
				ch.sprite.emitter().start(Speck.factory(Speck.UP), 0.15f, 4);
			}
		} else {
			for (Buff b : ch.buffs()){
				if (b instanceof GuidingLight.WasIlluminatedTracker){
					Buff.affect(ch, GuidingLight.Illuminated.class);
					continue;
				}

				if (b.type != Buff.buffType.NEGATIVE || b.mnemonicExtended){
					continue;
				}

				//this might need a nerf of aggression vs bosses. (perhaps nerf the extension?)
				if (b instanceof FlavourBuff)       Buff.affect(ch, (Class<?extends FlavourBuff>)b.getClass(), extension);
				else if (b instanceof Bleeding)     ((Bleeding) b).extend( extension );
				else if (b instanceof Burning)      ((Burning) b).extend( extension );
				else if (b instanceof Corrosion)    ((Corrosion) b).extend( extension );
				else if (b instanceof Dread)        ((Dread) b).extend( extension );
				else if (b instanceof Ooze)         ((Ooze) b).extend( extension );
				else if (b instanceof Poison)       ((Poison) b).extend( extension );
				else if (b instanceof Viscosity.DeferedDamage)  ((Viscosity.DeferedDamage) b).extend( extension );

				b.mnemonicExtended = affected = true;

			}
			if (affected) {
				if (!multiCast || !multiCastedDebuff) {
					multiCastedDebuff = multiCast;
					Sample.INSTANCE.play(Assets.Sounds.DEBUFF);
				}
				ch.sprite.emitter().start(Speck.factory(Speck.DOWN), 0.15f, 4);
			}

		}
	}

	public String desc(){
		return Messages.get(this, "desc", 2 + Dungeon.hero.pointsInTalent(Talent.MNEMONIC_PRAYER)) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

}
