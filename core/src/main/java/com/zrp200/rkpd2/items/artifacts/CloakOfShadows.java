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

package com.zrp200.rkpd2.items.artifacts;


import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.*;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.TargetedCell;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.bags.Bag;
import com.zrp200.rkpd2.items.rings.RingOfEnergy;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.BArray;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;

public class CloakOfShadows extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_CLOAK;

		exp = 0;
		levelCap = 10;

		charge = Math.min(level()+3, 10);
		partialCharge = 0;
		chargeCap = Math.min(level()+3, 10);

		defaultAction = AC_STEALTH;

		unique = true;
		bones = false;
	}

	public static final float ROGUE_BOOST = 1.5f;

	public static final String AC_STEALTH = "STEALTH";
	public static final String AC_TELEPORT = "TELEPORT";

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if ((isEquipped( hero ) || hero.hasTalent(Talent.LIGHT_CLOAK,Talent.RK_FREERUNNER)) && !cursed) {
			if ((charge > 0 || activeBuff != null)) {

				actions.add(AC_STEALTH);
			}
			if (charge > 0 && hero.hasTalent(Talent.ASSASSINS_REACH)){
				actions.add(AC_TELEPORT);
			}
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute(hero, action);

		if (action.equals( AC_STEALTH )) {

			if (activeBuff == null){
				if (!isEquipped(hero) && !hero.hasTalent(Talent.LIGHT_CLOAK,Talent.RK_FREERUNNER)) GLog.i( Messages.get(Artifact.class, "need_to_equip") );
				else if (cursed)       GLog.i( Messages.get(this, "cursed") );
				else if (charge <= 0)  GLog.i( Messages.get(this, "no_charge") );
				else {
					hero.spend( 1f );
					hero.busy();
					Sample.INSTANCE.play(Assets.Sounds.MELD);
					activeBuff = activeBuff();
					activeBuff.attachTo(hero);
					Talent.onArtifactUsed(Dungeon.hero);
					hero.sprite.operate(hero.pos);
				}
			} else {
				activeBuff.detach();
				activeBuff = null;
				if (hero.invisible <= 0 && hero.buff(Preparation.class) != null){
					hero.buff(Preparation.class).detach();
				}
				hero.sprite.operate( hero.pos );
			}

		}

		if (action.equals(AC_TELEPORT)){
			GameScene.selectCell(caster);
		}
	}

	private CellSelector.Listener caster = new CellSelector.Listener() {

		@Override
		public void onSelect(Integer target) {
			if (target != null && (Dungeon.level.visited[target] || Dungeon.level.mapped[target]) && Dungeon.level.passable[target]){
				int maxDistance = (int) (charge * (getChargeEfficiency()));
				if (Dungeon.level.distance(target, Dungeon.hero.pos) > maxDistance){
					GLog.w( Messages.get(CloakOfShadows.class, "cant_reach") );
				} else {
					float chargeCost = Dungeon.level.distance(target, Dungeon.hero.pos) / (getChargeEfficiency());
					CloakOfShadows.this.charge -= chargeCost;
					if (Dungeon.hero.invisible > 0){
						Preparation prep = Dungeon.hero.buff(Preparation.class);
						if (prep != null){
							prep.turnsInvis += (stealthDuration() / 2 * chargeCost);
							prep.act();
							BuffIndicator.refreshHero();
						}
					}
					ActionIndicator.clearAction((ActionIndicator.Action) passiveBuff);
					ScrollOfTeleportation.teleportToLocation(Dungeon.hero, target);
					//target hero level is 1 + 2*cloak level
					int lvlDiffFromTarget = Dungeon.hero.lvl - (1+level()*2);
					//plus an extra one for each level after 6
					if (level() >= 7){
						lvlDiffFromTarget -= level()-6;
					}
					if (lvlDiffFromTarget >= 0){
						exp += Math.round(10f * Math.pow(1.1f, lvlDiffFromTarget))*chargeCost;
					} else {
						exp += Math.round(10f * Math.pow(0.75f, -lvlDiffFromTarget))*chargeCost;
					}

					if (exp >= (level() + 1) * 50 && level() < levelCap) {
						upgrade();
						exp -= level() * 50;
						GLog.p(Messages.get(cloakStealth.class, "levelup"));
					}
					updateQuickslot();
				}
			}
		}

		@Override
		public String prompt() {
			int maxDistance = (int) (charge * (getChargeEfficiency()));
			PathFinder.buildDistanceMap( Dungeon.hero.pos, BArray.not(Dungeon.level.solid, null), maxDistance);
			for (int i = 0; i < PathFinder.distance.length; i++) {
				if (PathFinder.distance[i] < Integer.MAX_VALUE && !Dungeon.level.solid[i]) {
					Dungeon.hero.sprite.parent.addToBack(new TargetedCell(i, 0xb47ffe));
				}
			}
			return Messages.get(CloakOfShadows.class, "prompt");
		}
	};

	@Override
	public void activate(Char ch){
		super.activate(ch);
		if (activeBuff != null && activeBuff.target == null){
			activeBuff.attachTo(ch);
		}
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){
			if (!collect || !hero.hasTalent(Talent.LIGHT_CLOAK,Talent.RK_FREERUNNER)){
				if (activeBuff != null){
					activeBuff.detach();
					activeBuff = null;
				}
			} else {
				activate(hero);
			}

			return true;
		} else
			return false;
	}

	@Override
	public boolean collect( Bag container ) {
		if (super.collect(container)){
			if (container.owner instanceof Hero
					&& passiveBuff == null
					&& ((Hero) container.owner).hasTalent(Talent.LIGHT_CLOAK,Talent.RK_FREERUNNER)){
				activate((Hero) container.owner);
			}
			return true;
		} else{
			return false;
		}
	}

	@Override
	protected void onDetach() {
		if (passiveBuff != null){
			passiveBuff.detach();
			passiveBuff = null;
		}
		if (activeBuff != null && !isEquipped((Hero) activeBuff.target)){
			activeBuff.detach();
			activeBuff = null;
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new cloakRecharge();
	}

	@Override
	protected ArtifactBuff activeBuff( ) {
		return new cloakStealth();
	}
public static final float LC_FACTOR =.2f, LC_FACTOR_RK =.13f;
	@Override
	public void charge(Hero target, float amount) {
		if (charge < chargeCap) {
			// moved previous equip for free mechanic to light cloak
			if (!isEquipped(target)) amount *= target.byTalent(
					Talent.LIGHT_CLOAK, LC_FACTOR,
					Talent.RK_FREERUNNER, LC_FACTOR_RK);
			if(target.heroClass == HeroClass.ROGUE) amount *= ROGUE_BOOST;
			partialCharge += 0.25f*amount;
			if (partialCharge >= 1){
				partialCharge--;
				charge++;
				updateQuickslot();
			}
		}
	}

	public void overCharge(int amount){
		charge = Math.min(charge+amount, chargeCap+amount);
		updateQuickslot();
	}

	@Override
	public Item upgrade() {
		chargeCap = Math.min(chargeCap + 1, 10);
		return super.upgrade();
	}

	private static final String STEALTHED = "stealthed";
	private static final String BUFF = "buff";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		if (activeBuff != null) bundle.put(BUFF, activeBuff);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(BUFF)){
			activeBuff = new cloakStealth();
			activeBuff.restoreFromBundle(bundle.getBundle(BUFF));
		}
	}

	@Override
	public int value() {
		return 0;
	}

	public class cloakRecharge extends ArtifactBuff implements ActionIndicator.Action {
		@Override
		public boolean act() {
			if (charge < chargeCap) {
				LockedFloor lock = target.buff(LockedFloor.class);
				if (activeBuff == null && (lock == null || lock.regenOn())) {
					float missing = (chargeCap - charge);
					if (level() > 7) missing += 5*(level() - 7)/3f;
					float turnsToCharge = (45 - missing);
					if(((Hero)target).heroClass == HeroClass.ROGUE
						&& !((Hero) target).hasTalent(Talent.EFFICIENT_SHADOWS)) turnsToCharge /= ROGUE_BOOST;
					turnsToCharge /= RingOfEnergy.artifactChargeMultiplier(target);
					float chargeToGain = (1f / turnsToCharge);
					if (!isEquipped(Dungeon.hero)){
						chargeToGain *= Dungeon.hero.byTalent(
								Talent.LIGHT_CLOAK, LC_FACTOR,
								Talent.RK_FREERUNNER, LC_FACTOR_RK);
					}
					partialCharge += chargeToGain;
				}

				if (partialCharge >= 1) {
					charge++;
					partialCharge -= 1;
					if (charge == chargeCap){
						partialCharge = 0;
					}

				}
			} else
				partialCharge = 0;

			if (cooldown > 0)
				cooldown --;

			updateQuickslot();
			if ((int) (charge * getChargeEfficiency()) >= 1
					&& Dungeon.hero.hasTalent(Talent.ASSASSINS_REACH)){
				ActionIndicator.setAction(this);
			} else {
				ActionIndicator.clearAction(this);
			}

			spend( TICK );

			return true;
		}

		@Override
		public Image getIcon() {
			Image actionIco = new Image(Assets.Sprites.ITEM_ICONS);
			actionIco.frame(ItemSpriteSheet.Icons.film.get(ItemSpriteSheet.Icons.SCROLL_TELEPORT));
			actionIco.scale.set(2f);
			actionIco.hardlight(0xc44dd6);
			return actionIco;
		}

		@Override
		public void doAction() {
			execute(Dungeon.hero, AC_TELEPORT);
		}

		@Override
		public boolean usable() {
			return (int) (charge * getChargeEfficiency()) >= 1
					&& Dungeon.hero.hasTalent(Talent.ASSASSINS_REACH);
		}

	}

	public float getChargeEfficiency() {
		switch (Dungeon.hero.pointsInTalent(Talent.ASSASSINS_REACH)){
			case 1: default:
				return 0.75f;
			case 2:
				return 0.87f;
			case 3:
				return 1f;
		}
	}

	public static float stealthDuration(){
		return 4f +
				(Dungeon.hero.hasTalent(Talent.EFFICIENT_SHADOWS) ? 1f : 0) +
				Dungeon.hero.pointsInTalent(Talent.EFFICIENT_SHADOWS);
	}

	public class cloakStealth extends ArtifactBuff{

		{
			type = buffType.POSITIVE;
		}

		int turnsToCost = 0;

		@Override
		public int icon() {
			return BuffIndicator.INVISIBLE;
		}

		@Override
		public float iconFadePercent() {
			return (stealthDuration() - turnsToCost) / stealthDuration();
		}

		@Override
		public boolean attachTo( Char target ) {
			if (super.attachTo(target)) {
				target.invisible++;
				if (target instanceof Hero
						&& (((Hero) target).subClass == HeroSubClass.ASSASSIN
						|| ((Hero) target).subClass == HeroSubClass.KING)) {
					Buff.affect(target, Preparation.class);
				}
				return true;
			} else {
				return false;
			}
		}

		protected float incHeal = 1, incShield = 1;

		@Override
		public boolean act(){
			turnsToCost--;
			Hero target = (Hero)this.target;
			if(target.hasTalent(Talent.MENDING_SHADOWS)
					&& !Buff.affect(target, Hunger.class).isStarving()) {
				// heal every 4/2 turns when not starving. effectively a 1.5x boost to standard protective shadows, plus it doesn't go away.
				incHeal += target.pointsInTalent(Talent.MENDING_SHADOWS)/4f;
				if (incHeal >= 1 && target.HP < target.HT){
					incHeal = 0;
					target.HP++;
					target.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
				}
			}
			//barrier every 2/1 turns, to a max of 3/5
			if (target.hasTalent(Talent.MENDING_SHADOWS, Talent.NOBLE_CAUSE)){
				Barrier barrier = Buff.affect(target, Barrier.class);
				int points = target.pointsInTalent(Talent.MENDING_SHADOWS, Talent.NOBLE_CAUSE);
				if (barrier.shielding() < 1 + 2*points) {
					incShield += 0.5f*points;
				}
				if (incShield >= 1 ){
					incShield = 0;
					barrier.incShield(1);
				}
			}

			if (turnsToCost <= 0){
				charge--;
				if (charge < 0) {
					charge = 0;
					detach();
					GLog.w(Messages.get(this, "no_charge"));
					target.interrupt();
				} else {
					//target hero level is 1 + 2*cloak level
					int lvlDiffFromTarget = target.lvl - (1+level()*2);
					//plus an extra one for each level after 6
					if (level() >= 7){
						lvlDiffFromTarget -= level()-6;
					}
					if (lvlDiffFromTarget >= 0){
						exp += Math.round(10f * Math.pow(1.1f, lvlDiffFromTarget));
					} else {
						exp += Math.round(10f * Math.pow(0.75f, -lvlDiffFromTarget));
					}

					int expPerLevel = 50;
					if (exp >= (level() + 1) * expPerLevel && level() < levelCap) {
						upgrade();
						exp -= level() * expPerLevel;
						GLog.p(Messages.get(this, "levelup"));

					}
					turnsToCost = (int) stealthDuration();
				}
				updateQuickslot();
			}

			spend( TICK );

			return true;
		}

		public void dispel(){
			detach();
		}

		@Override
		public void fx(boolean on) {
			if (on) target.sprite.add( CharSprite.State.INVISIBLE );
			else if (target.invisible == 0) target.sprite.remove( CharSprite.State.INVISIBLE );
		}

		@Override
		public String toString() {
			return Messages.get(this, "name");
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc");
		}

		@Override
		public void detach() {
			activeBuff = null;

			if (target.invisible > 0)   target.invisible--;

			super.detach();
			updateQuickslot();
		}

		private static final String TURNSTOCOST = "turnsToCost";
		private static final String BARRIER_INC = "barrier_inc",
INC_HEAL="incHeal";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);

			bundle.put( TURNSTOCOST , turnsToCost);
			bundle.put( BARRIER_INC, incShield);
			bundle.put( INC_HEAL, incHeal);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);

			turnsToCost = bundle.getInt( TURNSTOCOST );
			incShield = bundle.getFloat( BARRIER_INC );
			incHeal = Math.max(incHeal, bundle.getFloat(INC_HEAL));
		}
	}
}
