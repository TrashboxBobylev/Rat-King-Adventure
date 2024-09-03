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

package com.zrp200.rkpd2.items.wands;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.BrawlerBuff;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Degrade;
import com.zrp200.rkpd2.actors.buffs.Hunger;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.buffs.Momentum;
import com.zrp200.rkpd2.actors.buffs.PowerfulDegrade;
import com.zrp200.rkpd2.actors.buffs.Regeneration;
import com.zrp200.rkpd2.actors.buffs.ScrollEmpower;
import com.zrp200.rkpd2.actors.buffs.SoulMark;
import com.zrp200.rkpd2.actors.buffs.SpiritBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.mage.WildMagic;
import com.zrp200.rkpd2.actors.mobs.Wraith;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.particles.ShadowParticle;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.artifacts.TalismanOfForesight;
import com.zrp200.rkpd2.items.bags.Bag;
import com.zrp200.rkpd2.items.bags.MagicalHolster;
import com.zrp200.rkpd2.items.rings.RingOfEnergy;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRecharging;
import com.zrp200.rkpd2.items.spells.CurseInfusion;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.melee.KromerStaff;
import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.ui.TargetHealthIndicator;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;

import static com.zrp200.rkpd2.Dungeon.hero;

public abstract class Wand extends Item {

	public static final String AC_ZAP	= "ZAP";

	private static final float TIME_TO_ZAP	= 1f;
	
	public int maxCharges = initialCharges();
	public int curCharges = maxCharges;
	public float partialCharge = 0f;
	
	protected Charger charger;
	
	public boolean curChargeKnown = false;
	
	public boolean curseInfusionBonus = false;
	public int resinBonus = 0;

	private static final int USES_TO_ID = 10;
	private float usesLeftToID = USES_TO_ID;
	private float availableUsesToID = USES_TO_ID/2f;

	protected int collisionProperties = Ballistica.FRIENDLY_MAGIC_BOLT;
	
	{
		defaultAction = AC_ZAP;
		usesTargeting = true;
		bones = true;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (curCharges > getMinCharges() || !curChargeKnown) {
			actions.add( AC_ZAP );
		}

		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_ZAP )) {
			
			curUser = hero;
			curItem = this;
			GameScene.selectCell( zapper );
			
		}
	}

	@Override
	public int targetingPos(Hero user, int dst) {
		return new Ballistica( user.pos, dst, collisionProperties ).collisionPos;
	}

	public abstract void onZap(Ballistica attack);

	public abstract void onHit(Weapon staff, Char attacker, Char defender, int damage);

	//not affected by arcana
	public static float procChanceMultiplier( Char attacker ){
		float multi = Weapon.Enchantment.genericProcChanceMultiplier(attacker, false);
		if (attacker.buff(Talent.EmpoweredStrikeTracker.class) != null) {
			// todo fix so that empowered strike is correctly handled.
			multi *= 1f + ((Hero)attacker).byTalent(
					Talent.EMPOWERED_STRIKE, 1/3f, // 67 133 200
					Talent.RK_BATTLEMAGE, 1/2f // 50 100 150
			);
		}
		return multi;
	}

	public boolean tryToZap( Hero owner, int target ){

		if (owner.buff(WildMagic.WildMagicTracker.class) == null && owner.buff(MagicImmune.class) != null){
			GLog.w( Messages.get(this, "no_magic") );
			return false;
		}

		//if we're using wild magic, then assume we have charges
		if ( owner.buff(WildMagic.WildMagicTracker.class) != null || curCharges >= chargesPerCast()){
			return true;
		} else {
			GLog.w(Messages.get(this, "fizzles"));
			return false;
		}
	}

	@Override
	public boolean collect( Bag container ) {
		if (super.collect( container )) {
			if (container.owner != null) {
				if (container instanceof MagicalHolster)
					charge( container.owner, ((MagicalHolster) container).HOLSTER_SCALE_FACTOR );
				else
					charge( container.owner );
			}
			return true;
		} else {
			return false;
		}
	}

	public void gainCharge( float amt ){
		gainCharge( amt, false );
	}

	public void gainCharge( float amt, boolean overcharge ){
		partialCharge += amt;
		while (partialCharge >= 1) {
			if (overcharge) curCharges = Math.min(maxCharges+(int)amt, curCharges+1);
			else curCharges = Math.min(maxCharges, curCharges+1);
			partialCharge--;
			updateQuickslot();
		}
	}
	
	public void charge( Char owner ) {
		if (charger == null) charger = new Charger();
		charger.attachTo( owner );
	}

	public void charge( Char owner, float chargeScaleFactor ){
		charge( owner );
		charger.setScaleFactor( chargeScaleFactor );
	}

	protected void wandProc(Char target, int chargesUsed) {
		wandProc(target, chargesUsed, 0);
	}
	protected void wandProc(Char target, int chargesUsed, int damage){
		// staff logic is handled here actually.
		MagesStaff staff = Dungeon.hero.belongings.getItem(MagesStaff.class);
		wandProc(target, buffedLvl(), chargesUsed, this instanceof DamageWand, damage, staff != null && staff.wand() == this);
	}

	//TODO Consider externalizing char awareness buff
	public static void wandProc(Char target, int wandLevel, int chargesUsed, boolean delay, int damage, boolean isStaff){
		if (Dungeon.hero.hasTalent(Talent.ARCANE_VISION,Talent.KINGS_VISION)) {
			float dur = 5*Dungeon.hero.byTalent(
					true, true,
					Talent.KINGS_VISION, 1,
					Talent.ARCANE_VISION,2);
			Buff.append(Dungeon.hero, TalismanOfForesight.CharAwareness.class, dur).charID = target.id();
		}
		int sorcery = Dungeon.hero.pointsInTalent(Talent.SORCERY);
		if(sorcery > 0) {
			MagesStaff staff = Dungeon.hero.belongings.getItem(MagesStaff.class);
			if(staff != null) {
				if (Random.Int(isStaff ? 20 : 10) < sorcery) staff.procBM();
				if (Random.Int(isStaff ? 12 :  6) < sorcery) staff.procWand(target, damage);
				if (staff instanceof KromerStaff) ((KromerStaff) staff).kromerProc();
			}
		}
		if (Dungeon.hero.hasTalent(Talent.POWER_IN_NUMBERS)){
			for (Char ch : Actor.chars()){
				if (ch instanceof Wraith && ch.alignment == Char.Alignment.ALLY){
					((Wraith) ch).beckon(target.pos);
					((Wraith) ch).aggro(target);
				}
			}
		}
		if (Dungeon.hero.hasTalent(Talent.BANISHED) && hero.buff(Talent.BanishedCooldown.class) == null){
			if (!target.properties().contains(Char.Property.BOSS)
					&& !target.properties().contains(Char.Property.MINIBOSS)
					&& target.buff(SoulMark.class) != null){
				target.sprite.emitter().burst(ShadowParticle.UP, 50);
				Buff.affect(hero, Hunger.class)
						.affectHunger( target.HP*hero.byTalent(Talent.SOUL_EATER,1/4f,Talent.RK_WARLOCK,1/6f) );
				hero.HP = (int) Math.ceil(Math.min(hero.HT, hero.HP + target.HP * 0.2f));
				hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
				Sample.INSTANCE.play(Assets.Sounds.DEATH, 1f, 0.75f);
				target.die(new CurseInfusion());
				target.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Wand.class, "banished"));
				TargetHealthIndicator.instance.target(null);
				Talent.Cooldown.affectHero(Talent.BanishedCooldown.class);
				return;
			}
		}
		SoulMark.process(target,wandLevel,chargesUsed,delay);
		if (Dungeon.hero.hasTalent(Talent.DEADLY_FOLLOWUP)) Buff.prolong(Dungeon.hero, Talent.DeadlyFollowupTracker.class, 5f).object = target.id();
	}

	@Override
	public void onDetach( ) {
		stopCharging();
	}

	public void stopCharging() {
		if (charger != null) {
			charger.detach();
			charger = null;
		}
	}
	
	public void level( int value) {
		super.level( value );
		updateLevel();
	}
	
	@Override
	public Item identify( boolean byHero ) {
		
		curChargeKnown = true;
		super.identify(byHero);
		
		updateQuickslot();
		
		return this;
	}
	
	public void onHeroGainExp( float levelPercent, Hero hero ){
		levelPercent *= Talent.itemIDSpeedFactor(hero, this);
		if (!isIdentified() && availableUsesToID <= USES_TO_ID/2f) {
			//gains enough uses to ID over 1 level
			availableUsesToID = Math.min(USES_TO_ID/2f, availableUsesToID + levelPercent * USES_TO_ID/2f);
		}
	}

	@Override
	public String info() {
		String desc = desc();

		desc += "\n\n" + statsDesc();

		if (resinBonus == 1){
			desc += "\n\n" + Messages.get(Wand.class, "resin_one");
		} else if (resinBonus > 1){
			desc += "\n\n" + Messages.get(Wand.class, "resin_many", resinBonus);
		}

		if (cursed && cursedKnown) {
			desc += "\n\n" + Messages.get(Wand.class, "cursed");
		} else if (!isIdentified() && cursedKnown){
			desc += "\n\n" + Messages.get(Wand.class, "not_cursed");
		}

		if (Dungeon.hero.subClass.is(HeroSubClass.BATTLEMAGE)){
			desc += "\n\n" + Messages.get(this, "bmage_desc", Messages.titleCase(Dungeon.hero.subClass.title()));
		}

		return desc;
	}

	public String statsDesc(){
		return Messages.get(this, "stats_desc");
	}
	
	@Override
	public boolean isIdentified() {
		return super.isIdentified() && curChargeKnown;
	}
	
	@Override
	public String status() {
		if (levelKnown) {
			return (curChargeKnown ? curCharges : "?") + "/" + maxCharges;
		} else {
			return null;
		}
	}
	
	@Override
	public int level() {
		if (!cursed && curseInfusionBonus){
			curseInfusionBonus = false;
			updateLevel();
		}
		int level = super.level();
		if (curseInfusionBonus) level += 1 + level/6;
		level += resinBonus;
		return level;
	}
	
	@Override
	public Item upgrade() {

		super.upgrade();

		if (Random.Int(3) == 0) {
			cursed = false;
		}

		if (resinBonus > 0){
			resinBonus--;
		}

		updateLevel();
		curCharges = Math.min( curCharges + 1, maxCharges );
		updateQuickslot();
		
		return this;
	}
	
	@Override
	public Item degrade() {
		super.degrade();
		
		updateLevel();
		updateQuickslot();
		
		return this;
	}

	protected int buffedLvl(boolean magicCharge) {
		int lvl = super.buffedLvl();
		MagesStaff staff = Dungeon.hero.belongings.getItem(MagesStaff.class);
		if(staff != null && staff.wand() == this && Dungeon.hero.buff(Degrade.class) != null) {
			int bonus = Dungeon.hero.getBonus(this);
			lvl = Degrade.reduceLevel(lvl-bonus)+bonus;
			if (Dungeon.hero.buff(PowerfulDegrade.class) != null) lvl = 0;
		}

		if (charger != null && charger.target != null) {

			//inside staff, still need to apply degradation
			if (charger.target == Dungeon.hero
					&& !Dungeon.hero.belongings.contains(this)
					&& Dungeon.hero.buff( Degrade.class ) != null){
				lvl = Degrade.reduceLevel(lvl);
			}

			if (charger.target.buff(ScrollEmpower.class) != null){
				lvl += ScrollEmpower.boost();
			}

			if (curCharges == 1 && charger.target instanceof Hero && ((Hero)charger.target).hasTalent(Talent.DESPERATE_POWER, Talent.RK_BATTLEMAGE)){
				lvl += ((Hero)charger.target).byTalent(Talent.DESPERATE_POWER, 2f, Talent.RK_BATTLEMAGE, 1f);
			}

			Momentum momentum = charger.target.buff(Momentum.class);
			if(momentum != null && momentum.freerunning() && Dungeon.hero.canHaveTalent(Talent.PROJECTILE_MOMENTUM)) {
				lvl += 1+Dungeon.hero.pointsInTalent(Talent.PROJECTILE_MOMENTUM);
			}

			if (charger.target.buff(WildMagic.WildMagicTracker.class) != null){
				int bonus = 4 + ((Hero)charger.target).pointsInTalent(Talent.WILD_POWER, Talent.ASTRAL_CHARGE);
				if (Random.Int(2) == 0) bonus++;
				bonus /= 2; // +2/+2.5/+3/+3.5/+4 at 0/1/2/3/4 talent points

				int maxBonusLevel = 3 + ((Hero)charger.target).pointsInTalent(Talent.WILD_POWER);
				if (lvl < maxBonusLevel) {
					lvl = Math.min(lvl + bonus, maxBonusLevel);
				}
			}

			WandOfMagicMissile.MagicCharge buff = charger.target.buff(WandOfMagicMissile.MagicCharge.class);
			if (buff != null && magicCharge && buff.appliesTo(this)){
				return buff.level();
			}
		}
		return lvl;
	}
	@Override
	public int buffedLvl() {
		return buffedLvl(true);
	}


	public void updateLevel() {
		maxCharges = Math.min( initialCharges() + level(), 10 );
		curCharges = Math.min( curCharges, maxCharges );
	}
	
	protected int initialCharges() {
		return 2;
	}

	protected int chargesPerCast() {
		return 1;
	}
	
	public void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar( curUser.sprite.parent,
				MagicMissile.MAGIC_MISSILE,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play( Assets.Sounds.ZAP );
	}

	public void staffFx( MagesStaff.StaffParticle particle ){
		particle.color(0xFFFFFF); particle.am = 0.3f;
		particle.setLifespan( 1f);
		particle.speed.polar( Random.Float(PointF.PI2), 2f );
		particle.setSize( 1f, 2f );
		particle.radiateXY(0.5f);
	}

	public int getMinCharges(){
		return Dungeon.hero.hasTalent(Talent.HEROIC_WIZARDRY) ? -Dungeon.hero.pointsInTalent(Talent.HEROIC_WIZARDRY) : 0;
	}

	protected void wandUsed() {
		if (!isIdentified()) {
			float uses = Math.min(availableUsesToID, Talent.itemIDSpeedFactor(Dungeon.hero, this));
			availableUsesToID -= uses;
			usesLeftToID -= uses;
			if (usesLeftToID <= 0 || Dungeon.hero.pointsInTalent(Talent.SCHOLARS_INTUITION, Talent.ROYAL_INTUITION) == 2) {
				identify();
				GLog.p(Messages.get(Wand.class, "identify"));
				Badges.validateItemLevelAquired(this);
			} else if (!levelKnown && Dungeon.hero.hasTalent(Talent.SCHOLARS_INTUITION)) {
				levelKnown = true;
				updateQuickslot();
				Badges.validateItemLevelAquired(this);
			}
		}

		//inside staff
		if (charger != null && charger.target == Dungeon.hero && !Dungeon.hero.belongings.contains(this)){
			if (Dungeon.hero.hasTalent(Talent.EXCESS_CHARGE) && curCharges >= maxCharges){
				int shieldToGive = Math.round(buffedLvl()*0.67f*Dungeon.hero.pointsInTalent(Talent.EXCESS_CHARGE));
				Buff.affect(Dungeon.hero, Barrier.class).setShield(shieldToGive);
				Dungeon.hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shieldToGive), FloatingText.SHIELDING);
			}
		}

		curCharges -= cursed ? 1 : chargesPerCast();

		//remove magic charge at a higher priority, if we are benefiting from it are and not the
		//wand that just applied it
		WandOfMagicMissile.MagicCharge buff = curUser.buff(WandOfMagicMissile.MagicCharge.class);
		// TODO make sure logic still works.
		if (buff != null
				&& buff.wandJustApplied() != this
				&& buff.level() == buffedLvl()
				&& buffedLvl() > super.buffedLvl()) {
			buff.detach();
		} else {
			ScrollEmpower empower = curUser.buff(ScrollEmpower.class);
			if (empower != null) {
				empower.use();
			}
		}
		if (Dungeon.hero.hasTalent(Talent.ARCANITY_ENSUES) && this instanceof DamageWand) {
			BrawlerBuff brawlerBuff = Dungeon.hero.buff(BrawlerBuff.class);
			brawlerBuff.useCharge();
			ActionIndicator.clearAction(brawlerBuff);
		}

		if (Dungeon.hero.hasTalent(Talent.ECTOTOUGHNESS)){
			hero.buff(SpiritBuff.class).countUp(Dungeon.hero.pointsInTalent(Talent.ECTOTOUGHNESS)+1);
		}

		if (Dungeon.hero.pointsInTalent(Talent.DUAL_WIELDING) > 0){
			Buff.affect(Dungeon.hero, Talent.DualWieldingMissileTracker.class, 1f);
		}

		//If hero owns wand but it isn't in belongings it must be in the staff
		if (Dungeon.hero.hasTalent(Talent.EMPOWERED_STRIKE, Talent.RK_BATTLEMAGE)
				&& charger != null && charger.target == Dungeon.hero
				&& !Dungeon.hero.belongings.contains(this)){
			Buff.prolong(Dungeon.hero, Talent.EmpoweredStrikeTracker.class, 10f);
		}
		Invisibility.dispel();
		updateQuickslot();

		if (curUser.buff(Talent.DualWieldingWandTracker.class) == null)
			curUser.spendAndNext( TIME_TO_ZAP );
		else {
			Buff.detach(curUser, Talent.DualWieldingWandTracker.class);
			Buff.detach(curUser, Talent.DualWieldingMissileTracker.class);
			curUser.ready();
		}
	}
	
	@Override
	public Item random() {
		//+0: 66.67% (2/3)
		//+1: 26.67% (4/15)
		//+2: 6.67%  (1/15)
		int n = 0;
		if (Random.Int(3) == 0) {
			n++;
			if (Random.Int(5) == 0){
				n++;
			}
		}
		if (!Dungeon.isChallenged(Challenges.REDUCED_POWER))
		level(n);
		curCharges += n;
		
		//30% chance to be cursed
		if (Random.Float() < 0.3f) {
			cursed = true;
		}

		return this;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		if (resinBonus == 0) return null;

		return new ItemSprite.Glowing(0xFFFFFF, 1f/(float)resinBonus);
	}

	@Override
	public int value() {
		int price = 75;
		if (cursed && cursedKnown) {
			price /= 2;
		}
		if (levelKnown) {
			if (level() > 0) {
				price *= (level() + 1);
			} else if (level() < 0) {
				price /= (1 - level());
			}
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}
	
	private static final String USES_LEFT_TO_ID     = "uses_left_to_id";
	private static final String AVAILABLE_USES      = "available_uses";
	private static final String CUR_CHARGES         = "curCharges";
	private static final String CUR_CHARGE_KNOWN    = "curChargeKnown";
	private static final String PARTIALCHARGE       = "partialCharge";
	private static final String CURSE_INFUSION_BONUS= "curse_infusion_bonus";
	private static final String RESIN_BONUS         = "resin_bonus";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( USES_LEFT_TO_ID, usesLeftToID );
		bundle.put( AVAILABLE_USES, availableUsesToID );
		bundle.put( CUR_CHARGES, curCharges );
		bundle.put( CUR_CHARGE_KNOWN, curChargeKnown );
		bundle.put( PARTIALCHARGE , partialCharge );
		bundle.put( CURSE_INFUSION_BONUS, curseInfusionBonus );
		bundle.put( RESIN_BONUS, resinBonus );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		usesLeftToID = bundle.getInt( USES_LEFT_TO_ID );
		availableUsesToID = bundle.getInt( AVAILABLE_USES );
		curseInfusionBonus = bundle.getBoolean(CURSE_INFUSION_BONUS);
		resinBonus = bundle.getInt(RESIN_BONUS);

		updateLevel();

		curCharges = bundle.getInt( CUR_CHARGES );
		curChargeKnown = bundle.getBoolean( CUR_CHARGE_KNOWN );
		partialCharge = bundle.getFloat( PARTIALCHARGE );
	}
	
	@Override
	public void reset() {
		super.reset();
		usesLeftToID = USES_TO_ID;
		availableUsesToID = USES_TO_ID/2f;
	}

	public int collisionProperties(int target){
		if (cursed)     return Ballistica.MAGIC_BOLT;
		else            return collisionProperties;
	}

	public static class PlaceHolder extends Wand {

		{
			image = ItemSpriteSheet.WAND_HOLDER;
		}

		@Override
		public boolean isSimilar(Item item) {
			return item instanceof Wand;
		}

		@Override
		public void onZap(Ballistica attack) {}
		public void onHit(Weapon staff, Char attacker, Char defender, int damage) {}

		@Override
		public String info() {
			return "";
		}
	}

	protected static CellSelector.Listener zapper = new  CellSelector.Listener() {
		
		@Override
		public void onSelect( Integer target ) {
			
			if (target != null) {
				
				//FIXME this safety check shouldn't be necessary
				//it would be better to eliminate the curItem static variable.
				final Wand curWand;
				if (curItem instanceof Wand) {
					curWand = (Wand) Wand.curItem;
				} else {
					return;
				}

				final Ballistica shot = new Ballistica( curUser.pos, target, curWand.collisionProperties(target),
						curUser.buff(ChampionEnemy.Projecting.class) != null && curUser.pointsInTalent(Talent.RK_PROJECT) == 3);
				int cell = shot.collisionPos;
				
				if (target == curUser.pos || cell == curUser.pos) {
					if (target == curUser.pos){

						if (curUser.buff(MagicImmune.class) != null){
							GLog.w( Messages.get(Wand.class, "no_magic") );
							return;
						}

						if (curWand.curCharges == 0){
							GLog.w( Messages.get(Wand.class, "fizzles") );
							return;
						}

						float shield = curUser.HT * (curWand.curCharges-curWand.getMinCharges()) *
								curUser.byTalent(Talent.SHIELD_BATTERY, 0.0625f, Talent.RESTORATION, 0.05f);
						shield *= Math.pow(1.5f, curUser.pointsInTalent(Talent.SHIELD_BATTERY, Talent.RESTORATION)-1);
						Buff.affect(curUser, Barrier.class).setShield(Math.round(shield));
						curUser.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(Math.round(shield)), FloatingText.SHIELDING);
						curWand.curCharges = curWand.getMinCharges();
						curUser.sprite.operate(curUser.pos);
						Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
						ScrollOfRecharging.charge(curUser);
						updateQuickslot();
						curUser.spendAndNext(Actor.TICK);
						return;
					}
					GLog.i( Messages.get(Wand.class, "self_target") );
					return;
				}

				curUser.sprite.zap(cell);

				//attempts to target the cell aimed at if something is there, otherwise targets the collision pos.
				if (Actor.findChar(target) != null)
					QuickSlotButton.target(Actor.findChar(target));
				else
					QuickSlotButton.target(Actor.findChar(cell));
				
				if (curWand.tryToZap(curUser, target)) {
					
					curUser.busy();

					//backup barrier logic, specifically managed so that they stack.
					//This triggers before the wand zap, mostly so the barrier helps vs skeletons
					if (curWand.curCharges == curWand.chargesPerCast()
							&& curWand.charger != null && curWand.charger.target == curUser){
						final int[] shieldToGive = {0};
						curUser.byTalent( (talent, points) -> {
							// grants 3-5 shielding
							int shielding = 1 + 2 * points;
							if (talent == Talent.BACKUP_BARRIER) {
								// 5-8
								shielding = Math.round(shielding * 1.5f);
							}
							if (curUser.heroClass == (
									talent == Talent.BACKUP_BARRIER ? HeroClass.MAGE :
									/*talent == Talent.NOBLE_CAUSE ?*/ HeroClass.RAT_KING
							)) {
								//regular. If hero owns wand but it isn't in belongings it must be in the staff
								if (!curUser.belongings.contains(curWand)) {
									shieldToGive[0] += shielding;
								}
							}
							//metamorphed. Triggers if wand is highest level hero has
							else {
								boolean highest = true;
								for (Item i : curUser.belongings.getAllItems(Wand.class)){
									if (i.level() > curWand.level()){
										highest = false;
									}
								}
								if (highest){
									//grants 3/5 shielding
									shieldToGive[0] += shielding;
								}
							}
						}, Talent.BACKUP_BARRIER, Talent.NOBLE_CAUSE);
						if (shieldToGive[0] > 0) Dungeon.hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shieldToGive[0]), FloatingText.SHIELDING);
					}

					if (curWand.cursed){
						if (!curWand.cursedKnown){
							GLog.n(Messages.get(Wand.class, "curse_discover", curWand.name()));
						}
						CursedWand.cursedZap(curWand,
								curUser,
								new Ballistica(curUser.pos, target, Ballistica.MAGIC_BOLT),
								new Callback() {
									@Override
									public void call() {
										curWand.wandUsed();
									}
								});
					} else {
						curWand.fx(shot, new Callback() {
							public void call() {
								curWand.onZap(shot);
								curWand.wandUsed();
							}
						});
					}
					curWand.cursedKnown = true;
					
				}
				
			}
		}
		
		@Override
		public String prompt() {
			return Messages.get(Wand.class, "prompt");
		}
	};

	public interface RechargeSource {
		float remainder();
	};
	
	public class Charger extends Buff {
		
		private static final float BASE_CHARGE_DELAY = 10f;
		private static final float SCALING_CHARGE_ADDITION = 40f;
		private static final float NORMAL_SCALE_FACTOR = 0.875f;

		private static final float CHARGE_BUFF_BONUS = 0.25f;

		float scalingFactor = NORMAL_SCALE_FACTOR;
private int energizeTime = 0;
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

		@Override
		public boolean act() {
			if (curCharges < maxCharges && target.buff(MagicImmune.class) == null)
				recharge();
			if (energizeTime > 0){
				energizeTime--;
				if (energizeTime == 0){
					curCharges = maxCharges;
					Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
					ScrollOfRecharging.charge(curUser);
					Talent.Cooldown.affectHero(Talent.EnergizingUpgradeCooldown.class);
					updateQuickslot();
					fx(false);
				}
			}

			while (partialCharge >= 1 && curCharges < maxCharges) {
				partialCharge--;
				curCharges++;
				updateQuickslot();
			}
			
			if (curCharges == maxCharges){
				partialCharge = 0;
			}
			
			spend( TICK );
			
			return true;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("energize", energizeTime);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			energizeTime = bundle.getInt("energize");
		}

		private void recharge(){
			int missingCharges = maxCharges - curCharges;
			missingCharges = Math.max(0, missingCharges);
			if(target instanceof Hero) {
				Hero hero = (Hero)target;
				missingCharges += ((Hero)target).getBonus(Wand.this);
				// this is because all wands are getting this boost, and it's not tied to 'zaps'. It's basically a visible intrinsic boost.
				Momentum m = hero.buff(Momentum.class);
				if(m != null && m.freerunning() && hero.canHaveTalent(Talent.PROJECTILE_MOMENTUM)) {
					missingCharges += 1+hero.pointsInTalent(Talent.PROJECTILE_MOMENTUM);
				}
			}

			float turnsToCharge = (float) (BASE_CHARGE_DELAY
					- ((Dungeon.hero.hasTalent(Talent.ARCANE_BOOST) && Dungeon.hero.belongings.contains(Wand.this)) ? 4 + Dungeon.hero.pointsInTalent(Talent.ARCANE_BOOST)*5 : 0)
					+ (SCALING_CHARGE_ADDITION * Math.pow(scalingFactor, missingCharges)));
			if (Wand.this instanceof WandOfUnstable || Wand.this instanceof WandOfUnstable2){
				turnsToCharge *= 0.67f;
			}

			if (Regeneration.regenOn())
				partialCharge += (1f/turnsToCharge) * RingOfEnergy.wandChargeMultiplier(target);

			for (RechargeSource bonus : target.buffs(RechargeSource.class, false)){
				if (bonus != null && bonus.remainder() > 0f) {
					partialCharge += CHARGE_BUFF_BONUS * bonus.remainder();
				}
			}
		}

		public void charge(Char owner, float charge){
			partialCharge += CHARGE_BUFF_BONUS*charge;
		}

		public Wand wand(){
			return Wand.this;
		}

		public void gainCharge(float charge, boolean overcharge){
			int maxCharges = Wand.this.maxCharges;
			if(overcharge) maxCharges += Math.ceil(charge);
			int cap = Math.max(curCharges,maxCharges); // this allows stacking of overcharging
			if (curCharges < cap) {
				partialCharge += charge;
				while (partialCharge >= 1f) {
					curCharges++;
					partialCharge--;
				}
				if(curCharges >= cap) {
					curCharges = cap;
					partialCharge = 0;
				}
				updateQuickslot();
			}
		}
		public void gainCharge(float charge) {
			gainCharge(charge,false);
		}

		private void setScaleFactor(float value){
			this.scalingFactor = value;
		}

		@Override
		public void fx(boolean on) {
			if (energizeTime > 0 && on) {
				target.sprite.add(CharSprite.State.SPIRIT);
			} else target.sprite.remove(CharSprite.State.SPIRIT);
		}
	}
}
