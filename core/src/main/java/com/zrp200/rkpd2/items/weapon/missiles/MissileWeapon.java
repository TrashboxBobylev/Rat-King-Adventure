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

package com.zrp200.rkpd2.items.weapon.missiles;

import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Adrenaline;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.buffs.Momentum;
import com.zrp200.rkpd2.actors.buffs.MonkEnergy;
import com.zrp200.rkpd2.actors.buffs.PinCushion;
import com.zrp200.rkpd2.actors.buffs.RevealedArea;
import com.zrp200.rkpd2.actors.buffs.Roots;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.LiquidMetal;
import com.zrp200.rkpd2.items.armor.ScoutArmor;
import com.zrp200.rkpd2.items.artifacts.CloakOfShadows;
import com.zrp200.rkpd2.items.bags.Bag;
import com.zrp200.rkpd2.items.bags.MagicalHolster;
import com.zrp200.rkpd2.items.rings.RingOfSharpshooting;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.items.wands.WandOfDisintegration;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.enchantments.Projecting;
import com.zrp200.rkpd2.items.weapon.melee.Crossbow;
import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
import com.zrp200.rkpd2.items.weapon.melee.MeleeWeapon;
import com.zrp200.rkpd2.items.weapon.melee.RunicBlade;
import com.zrp200.rkpd2.items.weapon.missiles.darts.Dart;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.utils.SafeCast;

import java.util.ArrayList;

abstract public class MissileWeapon extends Weapon {

	{
		stackable = true;
		levelKnown = true;

		bones = true;

		defaultAction = AC_THROW;
		usesTargeting = true;
	}
	
	protected boolean sticky = true;
	
	public static final float MAX_DURABILITY = 100;
	protected float durability = MAX_DURABILITY;
	protected float baseUses = 10;
	
	public boolean holster;
	
	//used to reduce durability from the source weapon stack, rather than the one being thrown.
	protected MissileWeapon parent;

	@Override
	public int min() {
		return Math.max(0, min( buffedLvl() + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) )) + Dungeon.hero.pointsInTalent(Talent.WEAPON_MASTERY);
	}
	
	@Override
	public int min(int lvl) {
		return  2 * tier +                      //base
				(tier == 1 ? lvl : 2*lvl);      //level scaling
	}
	
	@Override
	public int max() {
		return Math.max(0, max( buffedLvl() + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) ));
	}
	
	@Override
	public int max(int lvl) {
		return  5 * tier +                      //base
				(tier == 1 ? 2*lvl : tier*lvl); //level scaling
	}
	
	public int STRReq(int lvl){
		return STRReq(tier, lvl) - 1; //1 less str than normal for their tier
	}

	//use the parent item if this has been thrown from a parent
	public int buffedLvl(){
		int lvl;
		if (parent != null) {
			lvl = parent.buffedLvl();
		} else {
			lvl = super.buffedLvl();
		}
		if (MonkEnergy.isFeelingEmpowered(Level.Feeling.CHASM)){
			float cells = 0;
			for (int i : PathFinder.NEIGHBOURS8){
				if (Dungeon.level.map[Dungeon.hero.pos+i] == Terrain.CHASM)
					cells += 0.5f;
			}
			lvl += (int) Math.ceil(cells);
		}
		return lvl +
				(Dungeon.hero.buff(Talent.AutoReloadBuff.class) != null && !(this instanceof SpiritBow.SpiritArrow) ? 1 : 0);
	}

	@Override
	//FIXME some logic here assumes the items are in the player's inventory. Might need to adjust
	public Item upgrade() {
		if (!bundleRestoring) {
			durability = MAX_DURABILITY;
			if (quantity > 1) {
				MissileWeapon upgraded = (MissileWeapon) split(1);
				upgraded.parent = null;
				
				upgraded = (MissileWeapon) upgraded.upgrade();
				
				//try to put the upgraded into inventory, if it didn't already merge
				if (upgraded.quantity() == 1 && !upgraded.collect()) {
					Dungeon.level.drop(upgraded, Dungeon.hero.pos);
				}
				updateQuickslot();
				return upgraded;
			} else {
				super.upgrade();
				
				Item similar = Dungeon.hero.belongings.getSimilar(this);
				if (similar != null){
					detach(Dungeon.hero.belongings.backpack);
					Item result = similar.merge(this);
					updateQuickslot();
					return result;
				}
				updateQuickslot();
				return this;
			}
			
		} else {
			return super.upgrade();
		}
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (!hero.hasTalent(Talent.ELITE_DEXTERITY)) actions.remove( AC_EQUIP );
		return actions;
	}
	
	@Override
	public boolean collect(Bag container) {
		if (container instanceof MagicalHolster) holster = true;
		return super.collect(container);
	}

	@Override
	public int throwPos(Hero user, int dst) {

		boolean projecting = hasEnchant(Projecting.class, user);
		if (!projecting && Random.Int(3) < user.pointsInTalent(Talent.RK_SNIPER)
				|| user.hasTalent(Talent.SHARED_ENCHANTMENT) && Random.Int(4) <= user.pointsInTalent(Talent.SHARED_ENCHANTMENT)) {
			if (((this instanceof Dart && ((Dart) this).crossbowHasEnchant(Dungeon.hero)) ||
					(this instanceof SteelAxe && ((SteelAxe) this).crossbowHasEnchant(Dungeon.hero))) && !user.hasTalent(Talent.SHARED_ENCHANTMENT)){ // how DARE evan crush huntress synergies???
				//do nothing
			} else {
				SpiritBow bow = Dungeon.hero.belongings.getItem(SpiritBow.class);
				if (bow != null && bow.hasEnchant(Projecting.class, user)) {
					projecting = true;
				}
			}
		}
		if(!projecting && Random.Int(3) < Dungeon.hero.pointsInTalent(Talent.SORCERY)) { // just like shared enchant... yay.
			MagesStaff staff = Dungeon.hero.belongings.getItem(MagesStaff.class);
			projecting = staff != null && staff.wand() instanceof WandOfDisintegration;
		}
		if (!projecting && Dungeon.hero.buff(ChampionEnemy.Projecting.class) != null){
			projecting = true;
		}

		RunicBlade.RunicSlashTracker tracker = Dungeon.hero.buff(RunicBlade.RunicSlashTracker.class);
		boolean ignoreTracker = tracker != null;
		if (MeleeWeapon.activeAbility instanceof MeleeWeapon.MeleeAbility && ((MeleeWeapon.MeleeAbility)MeleeWeapon.activeAbility).weapon().hasEnchant(Projecting.class, Dungeon.hero)) {
			projecting = true;
			ignoreTracker = false;
		}
		if (ignoreTracker) tracker.detach();
		int throwPos;
		if (projecting
				&& (Dungeon.level.passable[dst] || Dungeon.level.avoid[dst] || Actor.findChar(dst) != null)
				&& Dungeon.level.distance(user.pos, dst) <= Math.round((4 + Dungeon.hero.pointsInTalent(Talent.RK_PROJECT)) * Enchantment.genericProcChanceMultiplier(user))){
			throwPos = dst;
		} else {
			throwPos = super.throwPos(user, dst);
		}
		if (ignoreTracker) tracker.attachTo(Dungeon.hero);
		return throwPos;
	}

	@Override
	public float accuracyFactor(Char owner, Char target) {
		float accFactor = super.accuracyFactor(owner, target);
		if (owner instanceof Hero && owner.buff(Momentum.class) != null && owner.buff(Momentum.class).freerunning()){
			accFactor *= 1f + 0.2f*((Hero) owner).pointsInTalent(Talent.PROJECTILE_MOMENTUM,Talent.RK_FREERUNNER);
		}

		accFactor *= adjacentAccFactor(owner, target);

		return accFactor;
	}

	protected float rangedAccFactor(Char owner) {
		float factor = .5f;
		if(owner instanceof Hero) {
			// +50% / +70% / +90% / +110%
			factor += 0.2f*((Hero)owner).pointsInTalent(Talent.POINT_BLANK);
		};
		return 1 + factor;
	}

	protected float adjacentAccFactor(Char owner, Char target){
		if (Dungeon.level.adjacent( owner.pos, target.pos )) {
			float factor = 0.5f;
			if (owner instanceof Hero){
				// -50% / -30% / -10% / +10%
				int points = ((Hero)owner).pointsInTalent(Talent.POINT_BLANK,Talent.RK_SNIPER);
				factor += 0.2f*points;
			}
			return factor;
		} else {
			return !Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.HUNTRESS) ? rangedAccFactor(owner) : 1f;
		}
	}

	@Override
	public void doThrow(Hero hero) {
		parent = null; //reset parent before throwing, just incase
		super.doThrow(hero);
	}

	@Override
    public void onThrow(int cell) {
		Char enemy = Actor.findChar( cell );
		if (enemy == null || enemy == curUser) {
			parent = null;
			//metamorphed seer shot logic
			if (curUser.hasTalent(Talent.SEER_SHOT)
					&& !curUser.heroClass.isExact(HeroClass.HUNTRESS)
					&& curUser.buff(Talent.SeerShotCooldown.class) == null){
				if (Actor.findChar(cell) == null) {
					// manually made consistent with rkpd2 logic
					new RevealedArea(cell, Dungeon.depth).attachTo(curUser);
				}
			}

			super.onThrow( cell );
		} else curUser.shoot(enemy, this);
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {

		RunicBlade.RunicSlashTracker tracker = Dungeon.hero.buff(RunicBlade.RunicSlashTracker.class);
		if (tracker != null) tracker.detach(); // don't use it for built in effects
		if (attacker == Dungeon.hero && Random.Int(3) < Dungeon.hero.pointsInTalent(Talent.RK_SNIPER)
				|| Dungeon.hero.hasTalent(Talent.SHARED_ENCHANTMENT) && Random.Int(4) <= Dungeon.hero.pointsInTalent(Talent.SHARED_ENCHANTMENT)){
			if (((this instanceof Dart && ((Dart) this).crossbowHasEnchant(Dungeon.hero)) ||
					(this instanceof SteelAxe && ((SteelAxe) this).crossbowHasEnchant(Dungeon.hero))) && !Dungeon.hero.hasTalent(Talent.SHARED_ENCHANTMENT)){ // HUNTRESS MUST BE BUFFED
 				//do nothing
			} else {
				SpiritBow bow = Dungeon.hero.belongings.getItem(SpiritBow.class);
				if (bow != null && bow.enchantment != null && Dungeon.hero.buff(MagicImmune.class) == null) {
					damage = bow.enchantment.proc(this, attacker, defender, damage);
				}
			}
		}
		if (Random.Int(8) < Dungeon.hero.pointsInTalent(Talent.POINT_BLANK) && Dungeon.level.adjacent(attacker.pos, defender.pos)){
			Ballistica trajectory = new Ballistica(attacker.pos, defender.pos, Ballistica.STOP_TARGET);
			trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
			WandOfBlastWave.throwChar(defender, trajectory, 2, false, true, getClass());
		}
		if (!Dungeon.hero.heroClass.is(HeroClass.HUNTRESS)
				&& Dungeon.hero.buff(ScoutArmor.ScoutCooldown.class) == null){
			boolean super_shot_talent = false;
			if (Dungeon.hero.hasTalent(Talent.SCOUTS_BARRIER)){
				Buff.affect(attacker, Barrier.class).incShield(2 + Dungeon.hero.pointsInTalent(Talent.SCOUTS_BARRIER)*2);
				super_shot_talent = true;
			}
			if (Dungeon.hero.hasTalent(Talent.IVYLASH)){
				Buff.affect(defender, Roots.class, Dungeon.hero.pointsInTalent(Talent.IVYLASH) + baseDelay(attacker));
				super_shot_talent = true;
			}
			if (Dungeon.hero.hasTalent(Talent.LIKE_A_BULLET)){
				int distance = Dungeon.level.distance(attacker.pos, defender.pos) - 1;
				float multiplier = Math.min(3f, 1.2f * (float) Math.pow(1.125f, distance)) *
						(0.5f + 0.5f * Dungeon.hero.pointsInTalent(Talent.LIKE_A_BULLET));
				damage = Math.round(damage * multiplier);
				super_shot_talent = true;
			}
			if (super_shot_talent)
				Buff.affect(attacker, ScoutArmor.ScoutCooldown.class, ScoutArmor.ScoutCooldown.DURATION*1.33f);
		}
		if (tracker != null) tracker.attachTo(Dungeon.hero); // reapply it
		if (Dungeon.hero.hasTalent(Talent.RK_PROJECT)){
			int distance = Dungeon.level.distance(attacker.pos, defender.pos) - 1;
			int points = Dungeon.hero.pointsInTalent(Talent.RK_PROJECT);
			float multiplier = 1f + Math.min(0.25f*points, 0.02f*(points+1)*distance);
			damage = Math.round(damage * multiplier);
		}

		MeleeWeapon.MeleeAbility abilityOverride = SafeCast.cast(MeleeWeapon.activeAbility, MeleeWeapon.MeleeAbility.class);
		if (abilityOverride != null) {
			MeleeWeapon wep = abilityOverride.weapon();
			// so balanced
			damage = wep.proc(attacker, defender, damage);
		}

		return super.proc(attacker, defender, damage);
	}

	@Override
	public Item random() {
		if (!stackable) return this;
		
		//2: 66.67% (2/3)
		//3: 26.67% (4/15)
		//4: 6.67%  (1/15)
		quantity = 2;
		if (Random.Int(3) == 0) {
			quantity++;
			if (Random.Int(5) == 0) {
				quantity++;
			}
		}
		return this;
	}

	@Override
	public void cast(Hero user, int dst) {
		super.cast(user, dst);
		if (Dungeon.hero.buff(Crossbow.DartSpent.class) != null && this instanceof Dart){
			MissileWeapon thing = Reflection.newInstance(getClass());
			thing.collect();
		}
	}


	public String status() {
		//show quantity even when it is 1
		String status = Integer.toString( quantity );

		if(Dungeon.hero.belongings.thirdWep() == this) {
			// show charges since it matters for the talent interactions
			MeleeWeapon.Charger charger = Buff.affect(Dungeon.hero, MeleeWeapon.Charger.class);
			status += " (" + (int)charger.charges[2] + "/" + charger.chargeCap(2) + ")";
		}
		return status;
	}

	@Override
	public float castDelay(Char user, int dst) {
		if (Dungeon.hero.pointsInTalent(Talent.DUAL_WIELDING) > 1){
			Buff.affect(Dungeon.hero, Talent.DualWieldingWandTracker.class, 1f);
		}
		if(user.buff(Talent.DualWieldingMissileTracker.class) != null) {
			Buff.detach(user, Talent.DualWieldingWandTracker.class);
			Buff.detach(user, Talent.DualWieldingMissileTracker.class);
			return 0;
		}
		if(Talent.LethalMomentumTracker.apply(user)) return 0;
		float speedFactor = delayFactor( user );
		if(user instanceof Hero && ((Hero)user).hasTalent(Talent.ONE_MAN_ARMY)) {
			Hero hero = (Hero)user;
			int targets = 0;
			Char enemy = Actor.findChar(dst);
			for(Char c : Dungeon.level.mobs) if(c.alignment == Char.Alignment.ENEMY && (c == enemy || hero.canAttack(c) || c.canAttack(hero) || throwPos(hero,c.pos) == c.pos)) targets++;
			speedFactor /= 1 + .11f * hero.pointsInTalent(Talent.ONE_MAN_ARMY) * Math.max(0,targets-1);
		}
		return speedFactor / (user.buff(Adrenaline.class) != null?1.5f:1);
	}
	public void onRangedAttack(Char enemy, int cell, boolean hit) {
		if(hit) rangedHit(enemy, cell);
		else rangedMiss(cell);
		MeleeWeapon.markAbilityUsed();
	}

	protected void rangedHit( Char enemy, int cell ){
		if (Dungeon.hero.buff(Crossbow.DartSpent.class) == null || !(this instanceof Dart)) {
			if (Random.Int(7) < Dungeon.hero.pointsInTalent(Talent.HEROIC_ARCHERY)){
				//do nothing
			}
			else decrementDurability();
			if (durability > 0 && !(this instanceof PhantomSpear)) {
				//attempt to stick the missile weapon to the enemy, just drop it if we can't.
				if (sticky && enemy != null && enemy.isAlive() && enemy.alignment != Char.Alignment.ALLY) {
					PinCushion p = Buff.affect(enemy, PinCushion.class);
					if (p.target == enemy) {
						p.stick(this);
						return;
					}
				}
				Dungeon.level.drop(this, cell).sprite.drop();
			}
		} else if (Dungeon.hero.buff(Crossbow.DartSpent.class) != null){
			Dungeon.hero.buff(Crossbow.DartSpent.class).detach();
		}
	}
	
	protected void rangedMiss( int cell ) {
		parent = null;
		if (Dungeon.hero.buff(Crossbow.DartSpent.class) == null || !(this instanceof Dart)) {
			if (!(this instanceof PhantomSpear))
				super.onThrow(cell);
		}
		else if (Dungeon.hero.buff(Crossbow.DartSpent.class) != null){
			Dungeon.hero.buff(Crossbow.DartSpent.class).detach();
		}

	}

	public float durabilityLeft(){
		return durability;
	}

	public void repair( float amount ){
		durability += amount;
		durability = Math.min(durability, MAX_DURABILITY);
	}

	public float durabilityPerUse(){
		//classes that override durabilityPerUse can turn rounding off, to do their own rounding after more logic
		return durabilityPerUse(true);
	}

	protected final float durabilityPerUse( boolean rounded){
		int level = level();
		if(Dungeon.hero.heroClass.isExact(HeroClass.ROGUE) && Dungeon.hero.buff(CloakOfShadows.cloakStealth.class, false) != null) level++;
		float usages = baseUses * (float)(Math.pow(3, level()));

		final float[] u = {usages};
		Dungeon.hero.byTalent(
				(talent, points) -> {
					float boost = 0.25f * (1 + points); 					// +50% / +75%
					if(talent == Talent.DURABLE_PROJECTILES) boost *= 2.5f; 	// +125% / +188%
					u[0] *= 1 + boost;
				}, Talent.DURABLE_PROJECTILES, Talent.PURSUIT);
		usages = u[0];
		if (holster) {
			usages *= MagicalHolster.HOLSTER_DURABILITY_FACTOR;
		}

		usages *= RingOfSharpshooting.durabilityMultiplier( Dungeon.hero );

		//at 100 uses, items just last forever.
		if (usages >= 100f && !Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.HUNTRESS)) return 0;

		if (rounded){usages = Math.round(usages);

		//add a tiny amount to account for rounding error for calculations like 1/3
		return (MAX_DURABILITY/usages) + 0.001f;} else {
			//rounding can be disabled for classes that override durability per use
			return MAX_DURABILITY/usages;
		}
	}
	
	protected void decrementDurability(){
		//if this weapon was thrown from a source stack, degrade that stack.
		//unless a weapon is about to break, then break the one being thrown
		if (parent != null){
			if (parent.durability <= parent.durabilityPerUse()){
				if (Dungeon.hero.hasTalent(Talent.AUTO_RELOAD)){
					LiquidMetal metal = Dungeon.hero.belongings.getItem(LiquidMetal.class);
					if (metal != null){
						metal.useToRepair(parent);
						if (Dungeon.hero.pointsInTalent(Talent.AUTO_RELOAD) > 1)
							Buff.affect(Dungeon.hero, Talent.AutoReloadBuff.class, 3f);
					} else {
						durability = 0;
						parent.durability = MAX_DURABILITY;
					}
				} else {
					durability = 0;
					parent.durability = MAX_DURABILITY;
				}
			} else {
				parent.durability -= parent.durabilityPerUse();
				if (parent.durability > 0 && parent.durability <= parent.durabilityPerUse()){
					if (level() <= 0)GLog.w(Messages.get(this, "about_to_break"));
					else             GLog.n(Messages.get(this, "about_to_break"));
				}
			}
			parent = null;
		} else {
			durability -= durabilityPerUse();
			if (durability > 0 && durability <= durabilityPerUse()){
				if (level() <= 0)GLog.w(Messages.get(this, "about_to_break"));
				else             GLog.n(Messages.get(this, "about_to_break"));
			}
			if (Dungeon.hero.hasTalent(Talent.AUTO_RELOAD) && durability <= 0){
				LiquidMetal metal = Dungeon.hero.belongings.getItem(LiquidMetal.class);
				if (metal != null){
					metal.useToRepair(this);
					if (Dungeon.hero.pointsInTalent(Talent.AUTO_RELOAD) > 1)
						Buff.affect(Dungeon.hero, Talent.AutoReloadBuff.class, 3f);
				}
			}
		}
	}
	
	@Override
	public int damageRoll(Char owner) {
		int damage = augment.damageFactor(super.damageRoll( owner ));
		
		if (owner instanceof Hero) {
			int exStr = ((Hero)owner).STR() - STRReq();
			if (exStr > 0) {
				damage += Random.IntRange( 0, exStr );
			}
			if (owner.buff(Momentum.class) != null && owner.buff(Momentum.class).freerunning()) {
				damage = Math.round(damage * (1f + 0.15f * ((Hero) owner).pointsInTalent(Talent.PROJECTILE_MOMENTUM,Talent.RK_FREERUNNER)));
			}
		}
		
		return damage;
	}
	
	@Override
	public void reset() {
		super.reset();
		durability = MAX_DURABILITY;
	}
	
	@Override
	public Item merge(Item other) {
		super.merge(other);
		if (isSimilar(other)) {
			durability += ((MissileWeapon)other).durability;
			durability -= MAX_DURABILITY;
			while (durability <= 0){
				quantity -= 1;
				durability += MAX_DURABILITY;
			}
		}
		return this;
	}
	
	@Override
	public Item split(int amount) {
		bundleRestoring = true;
		Item split = super.split(amount);
		bundleRestoring = false;
		
		//unless the thrown weapon will break, split off a max durability item and
		//have it reduce the durability of the main stack. Cleaner to the player this way
		if (split != null){
			MissileWeapon m = (MissileWeapon)split;
			m.durability = MAX_DURABILITY;
			m.parent = this;
		}
		
		return split;
	}
	
	@Override
	public boolean doPickUp(Hero hero, int pos) {
		parent = null;
		return super.doPickUp(hero, pos);
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public String info() {

		String info = desc();
		
		info += "\n\n" + Messages.get( MissileWeapon.class, "stats",
				tier,
				Math.round(augment.damageFactor(min())),
				Math.round(augment.damageFactor(max())),
				STRReq());

		if (STRReq() > Dungeon.hero.STR()) {
			info += " " + Messages.get(Weapon.class, "too_heavy");
		} else if (Dungeon.hero.STR() > STRReq()){
			info += " " + Messages.get(Weapon.class, "excess_str", Dungeon.hero.STR() - STRReq());
		}

		if (enchantment != null && (cursedKnown || !enchantment.curse())){
			info += "\n\n" + Messages.get(Weapon.class, "enchanted", enchantment.name());
			info += " " + Messages.get(enchantment, "desc");
		}

		if (cursed && isEquipped( Dungeon.hero )) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed_worn");
		} else if (cursedKnown && cursed) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed");
		} else if (!isIdentified() && cursedKnown){
			info += "\n\n" + Messages.get(Weapon.class, "not_cursed");
		}

		info += "\n\n" + Messages.get(MissileWeapon.class, "distance");
		
		info += "\n\n" + Messages.get(this, "durability");
		
		if (durabilityPerUse() > 0){
			info += " " + Messages.get(this, "uses_left",
					(int)Math.ceil(durability/durabilityPerUse()),
					(int)Math.ceil(MAX_DURABILITY/durabilityPerUse()));
		} else {
			info += " " + Messages.get(this, "unlimited_uses");
		}
		
		
		return info;
	}
	
	@Override
	public int value() {
		return 6 * tier * quantity * (level() + 1);
	}
	
	private static final String DURABILITY = "durability";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(DURABILITY, durability);
	}
	
	private static boolean bundleRestoring = false;
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		bundleRestoring = true;
		super.restoreFromBundle(bundle);
		bundleRestoring = false;
		durability = bundle.getFloat(DURABILITY);
	}

	public static class PlaceHolder extends MissileWeapon {

		{
			image = ItemSpriteSheet.MISSILE_HOLDER;
		}

		@Override
		public boolean isSimilar(Item item) {
			return item instanceof MissileWeapon;
		}

		@Override
		public String info() {
			return "";
		}
	}
}
