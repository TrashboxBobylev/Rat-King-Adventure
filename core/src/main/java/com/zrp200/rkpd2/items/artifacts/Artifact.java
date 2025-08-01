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

package com.zrp200.rkpd2.items.artifacts;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.buffs.PowerfulDegrade;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.spells.GuidingLight;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.KindofMisc;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Artifact extends KindofMisc {

	protected Buff passiveBuff;
	protected Buff activeBuff;

	//level is used internally to track upgrades to artifacts, size/logic varies per artifact.
	//already inherited from item superclass
	//exp is used to count progress towards levels for some artifacts
	protected int exp = 0;
	//levelCap is the artifact's maximum level
	protected int levelCap = 0;

	//the current artifact charge
	protected int charge = 0;
	//the build towards next charge, usually rolls over at 1.
	//better to keep charge as an int and use a separate float than casting.
	protected float partialCharge = 0;
	//the maximum charge, varies per artifact, not all artifacts use this.
	protected int chargeCap = 0;

	//used by some artifacts to keep track of duration of effects or cooldowns to use.
	protected int cooldown = 0;

	@Override
	public boolean doEquip( final Hero hero ) {

		if ((hero.belongings.artifact != null && hero.belongings.artifact.getClass() == this.getClass())
				|| (hero.belongings.misc != null && hero.belongings.misc.getClass() == this.getClass())){

			GLog.w( Messages.get(Artifact.class, "cannot_wear_two") );
			return false;

		} else {

			if (super.doEquip( hero )){

				identify();
				return true;

			} else {

				return false;

			}

		}

	}

	public void activate( Char ch ) {
		if (passiveBuff != null){
			if (passiveBuff.target != null) passiveBuff.detach();
			passiveBuff = null;
		}
		passiveBuff = passiveBuff();
		passiveBuff.attachTo(ch);
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {

			if (passiveBuff != null) {
				if (passiveBuff.target != null) passiveBuff.detach();
				passiveBuff = null;
			}

			return true;

		} else {

			return false;

		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public int visiblyUpgraded() {
		return levelKnown ? Math.round((level()*10)/(float)levelCap): 0;
	}

	@Override
	public int buffedVisiblyUpgraded() {
		return visiblyUpgraded();
	}

	@Override
	public int buffedLvl() {
		//level isn't affected by buffs/debuffs
		if (Dungeon.hero != null && Dungeon.hero.buff(PowerfulDegrade.class) != null) return 0;
		return level();
	}

	//transfers upgrades from another artifact, transfer level will equal the displayed level
	public void transferUpgrade(int transferLvl) {
		upgrade(Math.round((transferLvl*levelCap)/10f));
	}

	public void resetForTrinity(int visibleLevel){
		level(Math.round((visibleLevel*levelCap)/10f));
		exp = Integer.MIN_VALUE; //ensures no levelling
		charge = chargeCap;
		cooldown = 0;
	}

	public static void artifactProc(Char target, int artifLevel, int chargesUsed){
		GuidingLight.Illuminated.proc(target);

		if (target.alignment != Char.Alignment.ALLY
				&& !Dungeon.hero.heroClass.is(HeroClass.CLERIC)
				&& Dungeon.hero.canHaveTalent(Talent.SEARING_LIGHT)
				&& Dungeon.hero.buff(Talent.SearingLightCooldown.class) == null){
			Buff.affect(target, GuidingLight.Illuminated.class);
			Buff.affect(Dungeon.hero, Talent.SearingLightCooldown.class, 20f);
		}

		if (target.alignment != Char.Alignment.ALLY
				&& !Dungeon.hero.heroClass.is(HeroClass.CLERIC)
				&& Dungeon.hero.hasTalent(Talent.SUNRAY)){
			// 15/25% chance
			if (Random.Int(20) < 1 + 2*Dungeon.hero.pointsInTalent(Talent.SUNRAY)){
				Buff.prolong(target, Blindness.class, 4f);
			}
		}
	}

	@Override
	public String info() {
		if (cursed && cursedKnown && !isEquipped( Dungeon.hero )) {
			return super.info() + "\n\n" + Messages.get(Artifact.class, "curse_known");
			
		} else if (!isIdentified() && cursedKnown && !isEquipped( Dungeon.hero)) {
			return super.info() + "\n\n" + Messages.get(Artifact.class, "not_cursed");
			
		} else {
			return super.info();
			
		}
	}

	@Override
	public String status() {
		
		//if the artifact isn't IDed, or is cursed, don't display anything
		if (!isIdentified() || cursed){
			return null;
		}

		//display the current cooldown
		if (cooldown != 0)
			return Messages.format( "%d", cooldown );

		//display as percent
		if (chargeCap == 100)
			return Messages.format( "%d%%", charge );

		//display as #/#
		if (chargeCap > 0)
			return Messages.format( "%d/%d", charge, chargeCap );

		//if there's no cap -
		//- but there is charge anyway, display that charge
		if (charge != 0)
			return Messages.format( "%d", charge );

		//otherwise, if there's no charge, return null.
		return null;
	}

	@Override
	public Item random() {
		//always +0
		
		//30% chance to be cursed
		if (Random.Float() < 0.3f) {
			cursed = true;
		}
		return this;
	}

	@Override
	public int value() {
		int price = 100;
		if (level() > 0)
			price += 20*visiblyUpgraded();
		if (cursed && cursedKnown) {
			price /= 2;
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}


	protected ArtifactBuff passiveBuff() {
		return null;
	}

	protected ArtifactBuff activeBuff() {return null; }
	
	public void charge(Hero target, float amount){
		//do nothing by default;
	}

	public class ArtifactBuff extends Buff {

		@Override
		public boolean attachTo( Char target ) {
			if (super.attachTo( target )) {
				//if we're loading in and the hero has partially spent a turn, delay for 1 turn
				if (target instanceof Hero && Dungeon.hero == null && cooldown() == 0 && target.cooldown() > 0) {
					spend(TICK);
				}
				return true;
			}
			return false;
		}

		public int itemLevel() {
			return level();
		}

		public boolean isCursed() {
			return target.buff(MagicImmune.class) == null && cursed;
		}

		public void charge(Hero target, float amount){
			Artifact.this.charge(target, amount);
		}

		public Class<?extends Artifact> artifactClass() {
			return Artifact.this.getClass();
		}

	}
	
	private static final String EXP = "exp";
	private static final String CHARGE = "charge";
	private static final String PARTIALCHARGE = "partialcharge";
	private static final String CHARGECAP = "chargecap";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put( EXP , exp );
		bundle.put( CHARGE , charge );
		bundle.put( PARTIALCHARGE , partialCharge );
		bundle.put( CHARGECAP, chargeCap );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		exp = bundle.getInt( EXP );
		if (bundle.contains(CHARGECAP)) chargeCap = bundle.getInt(CHARGECAP);
		if (chargeCap > 0)  charge = Math.min( chargeCap, bundle.getInt( CHARGE ));
		else                charge = bundle.getInt( CHARGE );
		partialCharge = bundle.getFloat( PARTIALCHARGE );
	}
}
