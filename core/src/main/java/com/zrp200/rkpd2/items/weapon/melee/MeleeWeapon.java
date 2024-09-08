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

package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Bundle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.ArtifactRecharge;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.BrawlerBuff;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.HighnessBuff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.buffs.MonkEnergy;
import com.zrp200.rkpd2.actors.buffs.Recharging;
import com.zrp200.rkpd2.actors.buffs.Regeneration;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.particles.LeafParticle;
import com.zrp200.rkpd2.items.DuelistGrass;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.KindOfWeapon;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.features.HighGrass;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.AttackIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.utils.SafeCast;

import java.util.ArrayList;

import static com.zrp200.rkpd2.Dungeon.hero;

public class MeleeWeapon extends Weapon implements BrawlerBuff.BrawlerWeapon {

	public boolean trollers = false;
	public int grass = 0;
	protected static boolean cutGrass = false;

    public static String AC_ABILITY = "ABILITY";

    @Override
	public void activate(Char ch) {
		super.activate(ch);
		if (hasAbility() && ch == hero){
			Buff.affect(ch, Charger.class);
		}
	}

	protected boolean hasAbility() {
		return hero != null && hero.heroClass.is(HeroClass.DUELIST);
	} @Override public String defaultAction() {
		if (Dungeon.hero.subClass.is(HeroSubClass.BRAWLER) && Dungeon.hero.belongings.weapon != this)
			return AC_EQUIP;
		if (hasAbility() || hero.hasTalent(Talent.SWIFT_EQUIP)){
			return AC_ABILITY;
		} else {
			return super.defaultAction();
		}
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (hasAbility()){
			actions.add(AC_ABILITY);
		}
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (action.equals(AC_ABILITY)){
			return Messages.upperCase(Messages.get(this, "ability_name"));
		} else {
			return super.actionName(action, hero);
		}
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_ABILITY)){
			usesTargeting = false;
			if (!isEquipped(hero)) {
				if (hero.hasTalent(Talent.SWIFT_EQUIP)){
					if (hero.buff(Talent.SwiftEquipCooldown.class) == null
						|| hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()){
						execute(hero, AC_EQUIP);
					} else if (hero.heroClass == HeroClass.DUELIST) {
						GLog.w(Messages.get(this, "ability_need_equip"));
					}
				} else if (hero.heroClass == HeroClass.DUELIST) {
					GLog.w(Messages.get(this, "ability_need_equip"));
				}
			} else if (hero.heroClass != HeroClass.DUELIST){
				//do nothing
			} else if (STRReq() > hero.STR()){
				GLog.w(Messages.get(this, "ability_low_str"));
				usesTargeting = false;
				// fixme fix +3 effect of elite dexterity, current charges is undefined behavior
			} else {
				int slot = hero.belongings.findWeapon(this);
				Charger charger = Buff.affect(hero, Charger.class);
				if (slot != -1 && charger.charges[slot] < abilityChargeUse(hero, null)) {
					GLog.w(Messages.get(this, "ability_no_charge"));
					usesTargeting = false;
				} else {

					if (targetingPrompt() == null){
						duelistAbility(hero, hero.pos);
						updateQuickslot();
					} else {
						usesTargeting = useTargeting();
						GameScene.selectCell(new CellSelector.Listener() {
							@Override
							public void onSelect(Integer cell) {
								if (cell != null) {
									if (Dungeon.level.map[cell] == Terrain.HIGH_GRASS && KindOfWeapon.canReach(hero, cell, reachFactor(hero))){
											hero.sprite.attack(cell, () -> {
												Sample.INSTANCE.play(Assets.Sounds.HIT_SLASH, 1.2f, 0.75f);
												Level.set(cell, Terrain.GRASS);
												GameScene.updateMap(cell);

												HighGrass.playVFX(cell);
												if (Dungeon.level.heroFOV[cell]) Dungeon.observe();

												Item grass = new DuelistGrass();
												if (MeleeWeapon.this instanceof Sickle || MeleeWeapon.this instanceof WarScythe)
													grass.quantity(Random.Int(1, 2 + level()/3));

												if (grass.doPickUp(hero, cell)) {
													hero.spend(-Item.TIME_TO_PICK_UP); //casting the spell already takes a turn
													GLog.i( Messages.capitalize(Messages.get(hero, "you_now_have", grass.name())) );

												} else {
													GLog.w(Messages.get(this, "cant_grab"));
													Dungeon.level.drop(grass, cell).sprite.drop();
													return;
												}

												cutGrass = true;
												beforeAbilityUsed(hero, null);
												cutGrass = false;
												afterAbilityUsed(hero);
												Invisibility.dispel();
												hero.spendAndNext(hero.attackDelay());
											});
									} else {
										duelistAbility(hero, cell);
									}
									updateQuickslot();
								}
							}

							@Override
							public String prompt() {
								return targetingPrompt();
							}
						});
					}
				}
			}
		}
	}

	@Override
	public boolean doEquip(Hero hero, int slot) {
		if (super.doEquip(hero, slot)){
			ActionIndicator.refresh();
			return true;
		}
		return false;
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){
			ActionIndicator.refresh();
			return true;
		}
		return false;
	}

	//leave null for no targeting
	public String targetingPrompt(){
		return null;
	}

	public boolean useTargeting(){
		return targetingPrompt() != null;
	}

	@Override
	public int targetingPos(Hero user, int dst) {
		return dst; //weapon abilities do not use projectile logic, no autoaim
	}

	protected void duelistAbility( Hero hero, Integer target ){
		DuelistAbility ability = duelistAbility();
		if (ability != null) ability.execute(hero, target, this);
	}

	protected DuelistAbility duelistAbility() { return null; };

	// I really hope this works!
	public static DuelistAbility activeAbility;
	public static void markAbilityUsed() {
		if (activeAbility instanceof MeleeAbility) ((MeleeAbility)activeAbility).afterAbilityUsed();
	}


	protected interface DuelistAbility {
		/** @return whether the ability was used **/
		boolean execute(Hero hero, Integer target, MeleeWeapon weapon);
	}

	/** This removes basically all the duplication. anything implementing this will work with Elite Dexterity **/
	public static class MeleeAbility implements DuelistAbility {

		@Override
		public boolean execute(Hero hero, Integer target, MeleeWeapon wep) {
			if (target == null) return false;

			Char enemy = Actor.findChar(target);

			if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[target]) {
				GLog.w(Messages.get(wep, "ability_no_target"));
				return false;
			}

			hero.belongings.abilityWeapon = this.abilityWeapon = wep;
			if (!canAttack(hero, enemy)){
				hero.belongings.abilityWeapon = null;
				MissileWeapon thrown = SafeCast.cast(hero.belongings.thirdWep(), MissileWeapon.class);
				if (thrown != null) {
					// check charges of third slot, it should use the same amount of charges as the main slot
					if (Buff.affect(hero, Charger.class).charges[2] >= wep.abilityChargeUse(hero, enemy)
							&& enemy.pos == QuickSlotButton.autoAim(enemy, hero.belongings.thirdWep)) {
						// fake throw = cool
						beforeAbilityUsed(hero, enemy);
						// see Item.cast, Hero.shoot
						thrown.cast(hero, enemy.pos);
						return true;
					}
				}
				GLog.w(Messages.get(wep, "ability_bad_position"));

				return false;
			}
			if (!(wep instanceof ExoKnife))
				hero.belongings.abilityWeapon = null;

			hero.sprite.attack(enemy.pos, () -> {
				beforeAbilityUsed(hero, enemy);
				AttackIndicator.target(enemy);
				boolean hit = hero.attack(enemy, dmgMulti(enemy), 0, accMulti());
				if (hit) {
					onHit(hero, enemy);
				}
				afterHit(enemy, hit);
				afterAbilityUsed();
				Invisibility.dispel();
				hero.spendAndNext(hero.attackDelay());
			});
			return true;
		}

		private final float dmgMulti;
		public MeleeAbility(float dmgMulti) {
			this.dmgMulti = dmgMulti;
		}
		public MeleeAbility() {
			this(1f);
		}

		/** damage multiplier passed to Hero.attack **/
		public float dmgMulti(Char enemy) { return dmgMulti; }

		/** acc modifier passed to Hero.attack **/
		public float accMulti() { return Char.INFINITE_ACCURACY; }

		public final void onHit(Hero hero, Char enemy) {
			playSFX();
			if (!enemy.isAlive()) {
				onKill(hero);
				abilityWeapon.onAbilityKill(hero, enemy);
			} else proc(hero, enemy);
		}

		private MeleeWeapon abilityWeapon;
		public MeleeWeapon weapon() {
			return abilityWeapon;
		}

		public void afterAbilityUsed() {
			abilityWeapon.afterAbilityUsed(hero);
			activeAbility = null;
			if ((abilityWeapon instanceof ExoKnife))
				hero.belongings.abilityWeapon = null;
		}

		protected boolean canAttack(Hero hero, Char enemy) {
			return hero.canAttack(enemy) && !hero.isCharmedBy(enemy);
		}

		protected void beforeAbilityUsed(Hero hero, Char target) {
			activeAbility = this;
			abilityWeapon.beforeAbilityUsed(hero, target);
		}

		protected void playSFX() {
			Sample.INSTANCE.play(Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG));
		}

		/** effect applied when the enemy survives a hit **/
		protected void proc(Hero hero, Char enemy) {/* nothing by default */}

		protected void onKill(Hero hero) {/* nothing by default */}
		public void afterHit(Char enemy, boolean hit) {/* nothing by default */}
	}

	protected void beforeAbilityUsed(Hero hero, Char target){
		hero.belongings.abilityWeapon = this;
		Charger charger = Buff.affect(hero, Charger.class);
		float charge = -abilityChargeUse(hero, target);
		charger.gainCharge(
				charge,
				hero.belongings.findWeapon(this)
		);
		if (activeAbility != null) {
			// use charge from thrown weapon as well
			charger.gainCharge(charge, 2);
		}
		if (charge == 0)
			grass -= DuelistGrass.getAbilityGrassCost();

		if (hero.heroClass == HeroClass.DUELIST
				&& hero.hasTalent(Talent.AGGRESSIVE_BARRIER)
				&& (hero.HP / (float)hero.HT) < 0.20f*(1+hero.pointsInTalent(Talent.AGGRESSIVE_BARRIER))){
			Buff.affect(hero, Barrier.class).setShield(5);
			hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, "5", FloatingText.SHIELDING);
		}

		if (hero.buff(Talent.CombinedLethalityAbilityTracker.class) != null
				&& hero.buff(Talent.CombinedLethalityAbilityTracker.class).weapon != null
				&& hero.buff(Talent.CombinedLethalityAbilityTracker.class).weapon != this){
			Buff.affect(hero, Talent.CombinedLethalityTriggerTracker.class, 5f);
		}

		updateQuickslot();
	}

	protected void afterAbilityUsed( Hero hero ){
		hero.belongings.abilityWeapon = null;
		if (hero.hasTalent(Talent.PRECISE_ASSAULT)){
			Buff.prolong(hero, Talent.PreciseAssaultTracker.class, hero.cooldown()+4f);
		}
		if (hero.hasTalent(Talent.COMBINED_LETHALITY)) {
			Talent.CombinedLethalityAbilityTracker tracker = hero.buff(Talent.CombinedLethalityAbilityTracker.class);
			if (tracker == null || tracker.weapon == this || tracker.weapon == null){
				Buff.affect(hero, Talent.CombinedLethalityAbilityTracker.class, hero.cooldown()).weapon = this;
			} else {
				//we triggered the talent, so remove the tracker
				tracker.detach();
			}
		}
		if (hero.hasTalent(Talent.COMBINED_ENERGY)){
			Talent.CombinedEnergyAbilityTracker tracker = hero.buff(Talent.CombinedEnergyAbilityTracker.class);
			if (tracker == null || tracker.energySpent == -1){
				Buff.prolong(hero, Talent.CombinedEnergyAbilityTracker.class, hero.cooldown()).wepAbilUsed = true;
			} else {
				tracker.wepAbilUsed = true;
				Buff.affect(hero, MonkEnergy.class).processCombinedEnergy(tracker);
			}
		}
		if (hero.buff(Talent.CounterAbilityTacker.class) != null){
			hero.buff(Talent.CounterAbilityTacker.class).detach();
		}
		HighnessBuff.agreenalineProc();
	}

	public static void onAbilityKill( Hero hero, Char killed ){
		if(killed.alignment == Char.Alignment.ENEMY) {
			Talent.LethalHasteCooldown.applyLethalHaste(hero, true);
			if (HighnessBuff.isEnergized() && hero.pointsInTalent(Talent.SLASH_RUNNER) > 2){
				HighnessBuff beingHigh = hero.buff(HighnessBuff.class);
				beingHigh.currentPower = Math.min(beingHigh.grassValue(), beingHigh.currentPower + 2);
			}
		}
	}

	protected int baseChargeUse(Hero hero, Char target){
		return 1; //abilities use 1 charge by default
	}

	public final float abilityChargeUse(Hero hero, Char target){
		float chargeUse = baseChargeUse(hero, target);
		if (cutGrass)
			chargeUse = 1;
		if (hero.buff(Talent.CounterAbilityTacker.class) != null){
			chargeUse = Math.max(0, chargeUse-0.5f*hero.pointsInTalent(Talent.COUNTER_ABILITY));
		}
		if (grass > DuelistGrass.getAbilityGrassCost())
			chargeUse = 0;
		return chargeUse;
	}

	@Override
	public int min(int lvl) {
		return  tier +  //base
				lvl;    //level scaling
	}

	@Override
	public int max(int lvl) {
		return  5*(tier+1) +    //base
				lvl*(tier+1);   //level scaling
	}

	@Override
	public int min() {
		if (trollers)
			return super.min()*2;
		return super.min();
	}

	@Override
	public int max() {
		if (trollers)
			return super.max()*2;
		return super.max();
	}

	public int STRReq(int lvl){
		return STRReq(tier, lvl);
	}

	@Override
	public float accuracyFactor(Char owner, Char target) {
		float ACC = super.accuracyFactor(owner, target);

		if (owner instanceof Hero
				&& ((Hero) owner).hasTalent(Talent.PRECISE_ASSAULT)
				//does not trigger on ability attacks
				&& ((Hero) owner).belongings.abilityWeapon != this) {
			if (((Hero) owner).heroClass != HeroClass.DUELIST) {
				//persistent +10%/20%/30% ACC for other heroes
				ACC *= 1f + 0.1f * ((Hero) owner).pointsInTalent(Talent.PRECISE_ASSAULT);
			} else if (this instanceof Flail && owner.buff(Flail.SpinAbilityTracker.class) != null){
				//do nothing, this is not a regular attack so don't consume preciase assault
			} else if (owner.buff(Talent.PreciseAssaultTracker.class) != null) {
				// 2x/4x/8x ACC for duelist if she just used a weapon ability
				ACC *= Math.pow(2, ((Hero) owner).pointsInTalent(Talent.PRECISE_ASSAULT));
				owner.buff(Talent.PreciseAssaultTracker.class).detach();
			}
		}

		return ACC;
	}

	@Override
	public int damageRoll(Char owner) {
		int damage = augment.damageFactor(super.damageRoll( owner ));

		if (owner instanceof Hero) {
			int exStr = ((Hero)owner).STR() - STRReq();
			if (exStr > 0) {
				damage += Random.IntRange( 0, exStr );
			}
		}
		
		return damage;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (trollers)
			Warp.inflict(10f, 4f);

		int dmg = super.proc(attacker, defender, damage);
		if (attacker.alignment == Char.Alignment.ALLY && hero.hasTalent(Talent.SPELLBLADE_FORGERY) && this instanceof Talent.SpellbladeForgeryWeapon){
			int[] targets = new int[2];
			int direction = -1;
			int direction1 = -1, direction2 = -1;
			for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++){
				if (Actor.findChar(attacker.pos + PathFinder.NEIGHBOURS8[i]) == defender){
					direction = i;
				}
			}
			if (direction != -1) {
				switch (direction) {
					case 0:
						direction1 = 4;
						direction2 = 6;
						break;
					case 1:
					case 6:
						direction1 = 3;
						direction2 = 4;
						break;
					case 2:
						direction1 = 3;
						direction2 = 6;
						break;
					case 3:
					case 4:
						direction1 = 1;
						direction2 = 6;
						break;
					case 5:
						direction1 = 1;
						direction2 = 4;
						break;
					case 7:
						direction1 = 1;
						direction2 = 3;
						break;
				}
				targets[0] = defender.pos + PathFinder.NEIGHBOURS8[direction1];
				targets[1] = defender.pos + PathFinder.NEIGHBOURS8[direction2];
				Talent.SpellbladeForgeryWound.hit(defender.pos, 315, 0xCC33FF);
				for (int pos: targets){
					Talent.SpellbladeForgeryWound.hit(pos, 45, 0xCC33FF);
					if (Actor.findChar(pos) != null){
						Char ch = Actor.findChar(pos);
						if (ch.alignment != attacker.alignment){
							int spellDamage = Math.round(dmg*0.2f*(1+hero.pointsInTalent(Talent.SPELLBLADE_FORGERY)));
							Sample.INSTANCE.play(Assets.Sounds.HIT_STAB, 1f, 0.75f);
							Sample.INSTANCE.play(Assets.Sounds.HIT_ARROW, 0.8f, 1.25f);
							if (enchantment != null && attacker.buff(MagicImmune.class) == null) {
								spellDamage = enchantment.proc( this, attacker, defender, damage );
							}
							ch.damage(spellDamage, this);
						}
					}
				}
			}
		}

		if (grass > 0){
			Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
			Sample.INSTANCE.play(Assets.Sounds.PLANT, 2f, 1f);
			defender.sprite.emitter().burst(LeafParticle.LEVEL_SPECIFIC, 10);
			Talent.SpellbladeForgeryWound.hit(defender.pos, 315, ColorMath.random( Dungeon.level.color1, Dungeon.level.color2 ));
			dmg += Random.NormalIntRange(min() / (8 - hero.pointsInTalent(Talent.GRASSY_OFFENSE)*2), max() / (8 - hero.pointsInTalent(Talent.GRASSY_OFFENSE)*2));
			if (hero.pointsInTalent(Talent.GRASSY_OFFENSE) > 2){
				int heal = Math.min(2, attacker.HT-attacker.HP);
				if(heal > 0) {
					attacker.HP += heal;
					if (attacker.sprite != null) {
						attacker.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);
					}
				}
			}
			grass -= 1;
		}

		return dmg;
	}

	@Override
	public String info() {

		String info = desc();

		if (levelKnown) {
			info += "\n\n" + Messages.get(MeleeWeapon.class, "stats_known", tier, augment.damageFactor(min()), augment.damageFactor(max()), STRReq());
			if (STRReq() > hero.STR()) {
				info += " " + Messages.get(Weapon.class, "too_heavy");
			} else if (hero.STR() > STRReq()){
				info += " " + Messages.get(Weapon.class, "excess_str", hero.STR() - STRReq());
			}
		} else {
			info += "\n\n" + Messages.get(MeleeWeapon.class, "stats_unknown", tier, min(hero.getBonus(this)), max(hero.getBonus(this)), STRReq(0));
			if (STRReq(0) > hero.STR()) {
				info += " " + Messages.get(MeleeWeapon.class, "probably_too_heavy");
			}
		}

		String statsInfo = statsInfo();
		if (!statsInfo.equals("")) info += "\n\n" + statsInfo;

		switch (augment) {
			case SPEED:
				info += " " + Messages.get(Weapon.class, "faster");
				break;
			case DAMAGE:
				info += " " + Messages.get(Weapon.class, "stronger");
				break;
			case NONE:
		}

		if (enchantment != null && (cursedKnown || !enchantment.curse())){
			info += "\n\n" + Messages.capitalize(Messages.get(Weapon.class, "enchanted", enchantment.name()));
			if (enchantHardened) info += " " + Messages.get(Weapon.class, "enchant_hardened");
			info += " " + enchantment.desc();
		} else if (enchantHardened){
			info += "\n\n" + Messages.get(Weapon.class, "hardened_no_enchant");
		}

		if ((Dungeon.hero.subClass.is(HeroSubClass.BRAWLER)) && !Messages.get(this, "warrior_spec").equals("")) info += "\n\n" + Messages.get(MeleeWeapon.class, "warrior") + Messages.get(this, "warrior_spec");


		if (cursed && isEquipped( hero )) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed_worn");
		} else if (cursedKnown && cursed) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed");
		} else if (!isIdentified() && cursedKnown){
			if (enchantment != null && enchantment.curse()) {
				info += "\n\n" + Messages.get(Weapon.class, "weak_cursed");
			} else {
				info += "\n\n" + Messages.get(Weapon.class, "not_cursed");
			}
		}

		if (trollers)
			info += "\n\n" + Messages.get(MeleeWeapon.class, "cursed_kromer");


		//the mage's staff has no ability as it can only be gained by the mage
		if (hero.heroClass == HeroClass.DUELIST && !(this instanceof MagesStaff)){
			if (this instanceof RoyalBrand){
				info += "\n\n" + Messages.get(this, "ability_desc",
						Messages.decimalFormat("#.#", RoyalBrand.DuelistInfo.swordDanceDuration(hero)),
						Messages.decimalFormat("#", RoyalBrand.DuelistInfo.powerModifier(hero)),
						RoyalBrand.DuelistInfo.sneakDistance(hero),
						Messages.decimalFormat("#.#", 100f * RoyalBrand.DuelistInfo.spearBoost(hero)),
						Messages.decimalFormat("#.#", 100f * RoyalBrand.DuelistInfo.greataxeBoost(hero)),
						Messages.decimalFormat("#.#", 100f * RoyalBrand.DuelistInfo.maceBoost(hero)),
						Messages.decimalFormat("#.#", 100f * RoyalBrand.DuelistInfo.pickaxeBoost(hero)),
						Messages.decimalFormat("#.#", 100f * RoyalBrand.DuelistInfo.comboStrikeBoost(hero)),
						Messages.decimalFormat("#.#", RoyalBrand.DuelistInfo.dazeDuration(hero)),
						Messages.decimalFormat("#.#", 100f * RoyalBrand.DuelistInfo.bleedDamage(hero))
				);
			} else {
				info += "\n\n" + Messages.get(this, "ability_desc");
			}
		}

		if (grass > 0){
			info += "\n\n" + Messages.get(MeleeWeapon.class, "grass_desc", grass, DuelistGrass.getAbilityGrassCost());
		}

		return info;
	}
	
	public String statsInfo(){
		return Messages.get(this, "stats_desc");
	}

	@Override
	public String status() {
		Charger buff = hero.buff(Charger.class);
		if (buff != null) {
			int slot = hero.belongings.findWeapon(this);
			if (slot >= 0) {
				return (int) buff.charges[slot] + "/" + buff.chargeCap(slot);
			}
		}
		return super.status();
	}

	@Override
	public int value() {
		int price = 20 * tier;
		if (hasGoodEnchant()) {
			price *= 1.5;
		}
		if (cursedKnown && (cursed || hasCurseEnchant())) {
			price /= 2;
		}
		if (levelKnown && level() > 0) {
			price *= (level() + 1);
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}

	public static class Charger extends Buff implements ActionIndicator.Action {

		public float[] charges = {3,3,3};

		@Override
		public boolean act() {
			if (Regeneration.regenOn()) for (int i = 0; i < nSlots(); i++) {
				gainCharge(chargeMultiplier(i) / (getBaseRecharge(i) - (chargeCap(i) - charges[i])), i); // 40 to 30 turns per charge
				// secondary: 80 to 60 turns per charge without talent
				// up to 53.333 to 40 turns per charge at max talent level
			}

			int points = ((Hero)target).pointsInTalent(Talent.WEAPON_RECHARGING);
			if (points > 0 && target.buff(Recharging.class) != null || target.buff(ArtifactRecharge.class) != null){
				//shpd: 1 every 10 turns at +1, 6 turns at +2
				//rkpd2: 1 every 5 turns at +1, 3 turns at +2
				gainCharge(1/(7f - 2f*points));
			}

			// todo might have to check if I need to remove this check.
			if (ActionIndicator.action != this && hero.subClass == HeroSubClass.CHAMPION) {
				ActionIndicator.setAction(this);
			}

			spend(TICK);
			return true;
		}

		private float getBaseRecharge(int slot) {
			Hero hero = SafeCast.cast(target, Hero.class);
			if (hero.hasTalent(Talent.ADVENTUROUS_SNOOZING) && hero.resting){
				float boostMod = hero.heroClass == HeroClass.DUELIST ? 2f : 1f;
				return 40f * (1f - ((hero.pointsInTalent(Talent.ADVENTUROUS_SNOOZING))*boostMod / (float) chargeCap(slot)));
			}
			return 40f;
		}

		@Override
		public void fx(boolean on) {
			if (on) ActionIndicator.setAction(this);
		}

		@Override
		public void detach() {
			super.detach();
			ActionIndicator.clearAction(this);
		}

		public int chargeCap(){
			return Math.min(10, 3 + (hero.lvl-1)/3)
					* (hero.heroClass == HeroClass.DUELIST ? 2 : 1);
		}

		public static int nSlots() {
			int slots = 1;
			if (hero.subClass.is(HeroSubClass.CHAMPION)) slots++;
			if (hero.hasTalent(Talent.ELITE_DEXTERITY)) slots++;
			return slots;
		}

		public int chargeCap(int n) {
			return Math.round(chargeCap() * chargeMultiplier(n));
		}

		public float chargeMultiplier(int i) {
			//50% - 75%, depending on talent
			return (float)Math.pow(0.5f + 0.0834f* hero.pointsInTalent(Talent.SECONDARY_CHARGE), i);
		}

		public final void gainCharge( float charge ) { gainCharge(charge, 0); }
		public void gainCharge( float charge, int slot ){
			charges[slot] = Math.max(Math.min(charges[slot] + charge, chargeCap(slot)), 0);
			updateQuickslot();
		}

		public static final String CHARGES          = "charges";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(CHARGES, charges);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			charges = bundle.getFloatArray(CHARGES);
		}

		@Override
		public String actionName() {
			return Messages.get(MeleeWeapon.class, "swap");
		}

		@Override
		public int actionIcon() {
			return HeroIcon.WEAPON_SWAP;
		}

		@Override
		public Visual primaryVisual() {
			Image ico;
			if (hero.belongings.weapon == null){
				ico = new HeroIcon(this);
 			} else {
				ico = new ItemSprite(Dungeon.hero.belongings.weapon);
			}
			ico.width += 4; //shift slightly to the left to separate from smaller icon
			return ico;
		}

		@Override
		public Visual secondaryVisual() {
			Image ico;
			if (Dungeon.hero.belongings.secondWep == null){
				ico = new HeroIcon(this);
			} else {
				ico = new ItemSprite(hero.belongings.secondWep);
			}
			ico.scale.set(PixelScene.align(0.51f));
			ico.brightness(0.6f);
			return ico;
		}

		@Override
		public int indicatorColor() {
			return 0x5500BB;
		}

		@Override
		public boolean usable() {
			return target == hero && hero.subClass == HeroSubClass.CHAMPION
					// disable if elite dexterity +1 is being used.
					&& hero.belongings.thirdWep() == null || hero.pointsInTalent(Talent.ELITE_DEXTERITY) > 1;
		}

		@Override
		public void doAction() {
			if ( !usable() ) return;

			if (hero.belongings.secondWep == null && hero.belongings.backpack.items.size() >= hero.belongings.backpack.capacity()){
				GLog.w(Messages.get(MeleeWeapon.class, "swap_full"));
				return;
			}

			KindOfWeapon temp = hero.belongings.weapon;
			if (hero.belongings.thirdWep instanceof MeleeWeapon) {
				hero.belongings.weapon = hero.belongings.thirdWep; // 2 -> 0
				hero.belongings.thirdWep = hero.belongings.secondWep; // 1 -> 2
			} else {
				hero.belongings.weapon = hero.belongings.secondWep;
			}
			hero.belongings.secondWep = temp; // 0 -> 1

			hero.sprite.operate(hero.pos);
			Sample.INSTANCE.play(Assets.Sounds.UNLOCK);

			ActionIndicator.setAction(this);
			Item.updateQuickslot();
			AttackIndicator.updateState();
		}
	}

	private static final String KROMER	        = "pipisfusion";
	private static final String GRASS	        = "grasscharge";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(KROMER, trollers);
		bundle.put(GRASS, grass);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		trollers = bundle.getBoolean(KROMER);
		if (bundle.contains(GRASS))
			grass = bundle.getInt(GRASS);
	}

	public float warriorMod(){
		return 1f;
	}

	@Override
	public Emitter emitter() {
		if (grass <= 0) return null;
		Emitter emitter = new Emitter();
		emitter.pos(6f, 6f);
		emitter.fillTarget = false;
		emitter.pour(StaffParticleFactory, 0.033f);
		return emitter;
	}

	public final Emitter.Factory StaffParticleFactory = new Emitter.Factory() {
		@Override
		//reimplementing this is needed as instance creation of new staff particles must be within this class.
		public void emit(Emitter emitter, int index, float x, float y ) {
			StaffParticle c = emitter.getFirstAvailable(StaffParticle.class);
			if (c == null) {
				c = new StaffParticle();
				emitter.add(c);
			}
			c.reset(x, y);
		}

		@Override
		//some particles need light mode, others don't
		public boolean lightMode() {
			return true;
		}
	};

	//determines particle effects to use based on wand the staff owns.
	public class StaffParticle extends PixelParticle {

		private float minSize;
		private float maxSize;
		public float sizeJitter = 0;

		public StaffParticle(){
			super();
		}

		public void reset( float x, float y ) {
			revive();

			speed.set(0);

			this.x = x;
			this.y = y;

			color( ColorMath.random(0x004400, 0x88CC44) );
			am = 1f;
			setLifespan(1.5f);
			setSize( Random.Float(0.75f, 1.5f), Random.Float(1f, 2f));
			shuffleXY(8f);
		}

		public void setSize( float minSize, float maxSize ){
			this.minSize = minSize;
			this.maxSize = maxSize;
		}

		public void setLifespan( float life ){
			lifespan = left = life;
		}

		public void shuffleXY(float amt){
			x += Random.Float(-amt, amt);
			y += Random.Float(-amt, amt);
		}

		@Override
		public void update() {
			super.update();
			size(minSize + (left / lifespan)*(maxSize-minSize) + Random.Float(sizeJitter));
		}
	}

}
